package com.bossymr.rapid.ide.debugger;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.SubscriptionPriority;
import com.bossymr.network.client.NetworkEngine;
import com.bossymr.rapid.ide.debugger.breakpoints.RapidLineBreakpointHandler;
import com.bossymr.rapid.ide.debugger.frame.RapidSuspendContext;
import com.bossymr.rapid.ide.execution.configurations.TaskState;
import com.bossymr.rapid.language.symbol.RapidRobot;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.bossymr.rapid.robot.network.robotware.mastership.CloseableMastership;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipService;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import com.bossymr.rapid.robot.network.robotware.rapid.execution.*;
import com.bossymr.rapid.robot.network.robotware.rapid.task.StackFrame;
import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskActiveState;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ModuleEntity;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ModuleInfo;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.QueryMode;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ReplaceMode;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.Breakpoint;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XSuspendContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;

public class RapidDebugProcess extends XDebugProcess {
    public static final @NotNull Key<CompletableFuture<Breakpoint>> BREAKPOINT_KEY = Key.create("RapidBreakpointKey");
    private static final Logger logger = Logger.getInstance(RapidDebugProcess.class);
    private final @NotNull RapidDebuggerEditorsProvider editorsProvider = new RapidDebuggerEditorsProvider();

    private final @NotNull Set<XBreakpointHandler<?>> breakpointHandlers = Set.of(
            new RapidLineBreakpointHandler(this)
    );

    private final @NotNull Project project;

    /**
     * A {@code Phaser} used to control access to start the program.
     */
    private final @NotNull Phaser phaser = new Phaser() {
        @Override
        protected boolean onAdvance(int phase, int registeredParties) {
            return false;
        }
    };

    /**
     * A {@code Set} containing all breakpoints which are currently registered.
     */
    private final @NotNull Set<XBreakpoint<?>> breakpoints = new HashSet<>();
    private final @NotNull List<TaskState> tasks;


    private final @NotNull NetworkEngine networkEngine;


    public RapidDebugProcess(@NotNull Project project,
                             @NotNull XDebugSession session,
                             @NotNull List<TaskState> tasks,
                             @NotNull NetworkEngine networkEngine) {
        super(session);
        this.project = project;
        this.tasks = tasks;
        this.networkEngine = networkEngine;
    }

    private @NotNull ExecutionService getExecutionService() {
        return networkEngine.createService(ExecutionService.class);
    }

    @Override
    public void sessionInitialized() {
        ExecutionService executionService = getExecutionService();
        AtomicBoolean isStarted = new AtomicBoolean();
        executionService.resetProgramPointer().sendAsync()
                .thenComposeAsync(result -> executionService.onExecutionState().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
                    switch (event.getState()) {
                        case RUNNING -> {
                            isStarted.set(true);
                            logger.debug("Process resumed");
                            getSession().sessionResumed();
                        }
                        case STOPPED -> {
                            if (!(isStarted.get())) {
                                logger.trace("Process not started");
                                return;
                            }
                            // Don't resume the program until after all calculations are complete.
                            phaser.register();
                            TaskService taskService = networkEngine.createService(TaskService.class);
                            taskService.getTasks().sendAsync().thenAcceptAsync(tasks -> {
                                for (Task task : tasks) {
                                    if (task.getActivityState() == TaskActiveState.DISABLED) continue;
                                    task.getStackFrame(1).sendAsync().thenCombineAsync(task.getStackFrame(2).sendAsync(), (stackFrame, nextStackFrame) -> {
                                        onProgramStop(task, stackFrame, nextStackFrame);
                                        return null;
                                    }).handleAsync((value, throwable) -> {
                                        phaser.arriveAndDeregister();
                                        if (throwable != null) {
                                            logger.error(throwable);
                                        }
                                        return null;
                                    });
                                }
                            });
                        }
                    }
                })).thenComposeAsync(ignored -> startAsync(ExecutionMode.CONTINUE));
    }

    private void onProgramStop(@NotNull Task task, @NotNull StackFrame stackFrame, @NotNull StackFrame nextStackFrame) {
        logger.debug("Process paused at '" + stackFrame + "' invoked by '" + nextStackFrame + "'");
        RapidSuspendContext suspendContext = new RapidSuspendContext(project, breakpoints, networkEngine, task, stackFrame);
        for (XBreakpoint<?> breakpoint : breakpoints) {
            if (isAtBreakpoint(stackFrame, breakpoint)) {
                logger.debug("Breakpoint '" + breakpoint + "' reached");
                getSession().breakpointReached(breakpoint, null, suspendContext);
                return;
            }
        }
        if (hasStopped(stackFrame) || hasStopped(nextStackFrame)) {
            logger.debug("Process stopped");
            getSession().stop();
            networkEngine.closeAsync();
        } else {
            logger.debug("Process paused");
            getSession().positionReached(suspendContext);
        }
    }

    private boolean isAtBreakpoint(@NotNull StackFrame stackFrame, @NotNull XBreakpoint<?> breakpoint) {
        XSourcePosition sourcePosition = breakpoint.getSourcePosition();
        if (sourcePosition == null) return false;
        String breakpointModule = sourcePosition.getFile().getNameWithoutExtension();
        String currentModule = stackFrame.getRoutine().split("/")[2];
        if (!(breakpointModule.equals(currentModule))) return false;
        return sourcePosition.getLine() >= stackFrame.getStartRow() - 1 && sourcePosition.getLine() <= stackFrame.getEndRow() - 1;
    }

    private boolean hasStopped(@NotNull StackFrame stackFrame) {
        return stackFrame.getStartRow() == 0 && stackFrame.getEndRow() == 0 && stackFrame.getStartColumn() == 0 && stackFrame.getEndColumn() == 0;
    }

    private @NotNull CompletableFuture<Void> startAsync(@NotNull ExecutionMode executionMode) {
        MastershipService mastershipService = networkEngine.createService(MastershipService.class);
        return CompletableFuture.runAsync(() -> {
            int parties = phaser.getUnarrivedParties();
            if (parties > 0) {
                logger.debug("Awaiting '" + parties + "' to start process");
            }
            phaser.register();
            phaser.awaitAdvance(phaser.arriveAndDeregister());
            logger.debug("Starting process with mode: " + executionMode);
            mastershipService.getDomain(MastershipType.RAPID).sendAsync()
                    .thenComposeAsync(domain -> {
                        NetworkCall<Void> networkCall = getExecutionService().start(RegainMode.REGAIN, executionMode, ExecutionCycle.ONCE, ConditionState.CALLCHAIN, BreakpointMode.ENABLED, TaskExecutionMode.NORMAL);
                        return CloseableMastership.requestAsync(domain, networkCall::sendAsync);
                    });
        });
    }

    private @NotNull CompletableFuture<Void> stopAsync(@NotNull StopMode stopMode) {
        logger.debug("Stopping process with mode: " + stopMode);
        return getExecutionService().stop(stopMode, TaskExecutionMode.NORMAL).sendAsync();
    }

    /**
     * Registers a new breakpoint at the specified position.
     *
     * @param moduleName the name of the module.
     * @param line the line number, starting at 0.
     * @return the asynchronous request.
     */
    public @NotNull CompletableFuture<Breakpoint> registerBreakpoint(@NotNull String taskName, @NotNull String moduleName, int line) {
        TaskService taskService = networkEngine.createService(TaskService.class);
        return taskService.getTask(taskName).sendAsync()
                .thenComposeAsync(task -> task.getProgram().sendAsync())
                .thenComposeAsync(program -> program.setBreakpoint(moduleName, line + 1, 0).sendAsync()
                        .thenComposeAsync(result -> program.getBreakpoints().sendAsync())
                        .thenApplyAsync(breakpoints -> findBreakpoint(breakpoints, moduleName, line)));
    }

    private @Nullable Breakpoint findBreakpoint(@NotNull List<Breakpoint> breakpoints, @NotNull String moduleName, int line) {
        for (Breakpoint breakpoint : breakpoints) {
            if (breakpoint.getModuleName().equals(moduleName)) {
                if (line >= (breakpoint.getStartRow() - 1) && line <= (breakpoint.getEndRow() - 1)) {
                    return breakpoint;
                }
            }
        }
        logger.warn("Could not find breakpoint '" + moduleName + ":" + line + "' in " + breakpoints);
        return null;
    }

    /**
     * Registers the specified breakpoint.
     *
     * @param breakpoint the breakpoint.
     */
    public void registerBreakpoint(@NotNull XBreakpoint<?> breakpoint) {
        XSourcePosition sourcePosition = breakpoint.getSourcePosition();
        if (sourcePosition == null) return;
        String taskName = getTaskName(sourcePosition);
        if (taskName == null) return;
        String moduleName = getModuleName(sourcePosition);
        CompletableFuture<Breakpoint> completableFuture = registerBreakpoint(taskName, moduleName, sourcePosition.getLine());
        breakpoint.putUserData(BREAKPOINT_KEY, completableFuture);
        if (breakpoint instanceof XLineBreakpoint<?> lineBreakpoint) {
            completableFuture.thenRunAsync(() -> getSession().setBreakpointVerified(lineBreakpoint));
        }
        breakpoints.add(breakpoint);
    }

    /**
     * Removes the specified breakpoint.
     * <p>
     * An endpoint to remove a breakpoint was introduced in RobotWare 7.2, however, RobotWare 7 uses a different version
     * of API, which is not backwards compatible with RobotWare 6. As a result, to remove a breakpoint, the text covered
     * by the breakpoint is replaced by itself. Unfortunately, as a result, a breakpoint on the same line as the program
     * pointer cannot be removed. As modifying the line containing the program pointer will reset the program pointer.
     *
     * @param breakpoint the breakpoint to remove.
     * @return the asynchronous request.
     */
    public @NotNull CompletableFuture<Void> unregisterBreakpoint(@NotNull Breakpoint breakpoint) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        getExecutionService().onExecutionState().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
            if (event.getState() == ExecutionState.STOPPED) {
                phaser.register();
                String taskName = breakpoint.getLink("self").getPath().substring(1).split("/")[3];
                TaskService taskService = networkEngine.createService(TaskService.class);
                taskService.getTask(taskName).sendAsync().thenComposeAsync(task -> task.getStackFrame(1).sendAsync().thenComposeAsync(stackFrame -> {
                    if (!stackFrame.toTextRange().equals(breakpoint.toTextRange())) {
                        logger.debug("Removing breakpoint '" + breakpoint + "'");
                        return entity.unsubscribe()
                                .thenComposeAsync(unused -> findModule(taskName, breakpoint.getModuleName()))
                                .thenComposeAsync(module -> {
                                    if (module == null) return CompletableFuture.completedFuture(null);
                                    MastershipService mastershipService = networkEngine.createService(MastershipService.class);
                                    return module.getText(breakpoint.getStartRow(), breakpoint.getStartColumn(), breakpoint.getEndRow(), breakpoint.getEndColumn()).sendAsync()
                                            .thenComposeAsync(moduleText -> mastershipService.getDomain(MastershipType.RAPID).sendAsync()
                                                    .thenComposeAsync(domain -> {
                                                        NetworkCall<Void> networkCall = module.setText(task.getName(), ReplaceMode.REPLACE, QueryMode.FORCE, breakpoint.getStartRow(), breakpoint.getStartColumn(), breakpoint.getEndRow(), breakpoint.getEndColumn(), moduleText.getText());
                                                        return CloseableMastership.requestAsync(domain, networkCall::sendAsync);
                                                    }));
                                });
                    } else {
                        logger.debug("Could not remove breakpoint '" + breakpoint + "' at '" + stackFrame + "'");
                        return CompletableFuture.completedFuture(null);
                    }
                }).handleAsync((unused, throwable) -> {
                    phaser.arriveAndDeregister();
                    return null;
                }));
            }
        });
        return completableFuture;
    }

    private @NotNull CompletableFuture<ModuleEntity> findModule(@NotNull String taskName, @NotNull String moduleName) {
        TaskService taskService = networkEngine.createService(TaskService.class);
        return taskService.getTask(taskName).sendAsync().thenComposeAsync(task -> task.getModules().sendAsync()
                .thenComposeAsync(moduleInfos -> {
                    for (ModuleInfo moduleInfo : moduleInfos) {
                        if (moduleInfo.getName().equals(moduleName)) {
                            return moduleInfo.getModule().sendAsync();
                        }
                    }
                    logger.warn("Could not find module '" + moduleName + "' in " + moduleInfos);
                    return CompletableFuture.completedFuture(null);
                }));
    }

    /**
     * Unregisters the specified breakpoint.
     *
     * @param breakpoint the breakpoint.
     */
    public void unregisterBreakpoint(@NotNull XBreakpoint<?> breakpoint) {
        breakpoints.remove(breakpoint);
        CompletableFuture<Breakpoint> completableFuture = breakpoint.getUserData(BREAKPOINT_KEY);
        if (completableFuture == null) {
            throw new IllegalArgumentException();
        }
        completableFuture.thenComposeAsync(this::unregisterBreakpoint);
    }

    @Override
    public @NotNull XDebuggerEditorsProvider getEditorsProvider() {
        return editorsProvider;
    }

    @Override
    public XBreakpointHandler<?> @NotNull [] getBreakpointHandlers() {
        return breakpointHandlers.toArray(XBreakpointHandler.EMPTY_ARRAY);
    }

    @Override
    public void startPausing() {
        stopAsync(StopMode.INSTRUCTION);
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext context) {
        startAsync(ExecutionMode.STEP_OVER);
    }

    @Override
    public void startForceStepInto(@Nullable XSuspendContext context) {
        startStepInto(context);
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        startAsync(ExecutionMode.STEP_IN);
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        startAsync(ExecutionMode.STEP_OUT);
    }

    private @Nullable String getTaskName(@NotNull XSourcePosition sourcePosition) {
        Module module = ModuleUtil.findModuleForFile(sourcePosition.getFile(), project);
        if (module != null) {
            for (TaskState taskState : tasks) {
                if (module.getName().equals(taskState.getModuleName())) {
                    return taskState.getName();
                }
            }
        } else {
            RemoteRobotService service = RemoteRobotService.getInstance();
            RapidRobot robot = service.getRobot();
            if (robot == null) return null;
            for (RapidTask task : robot.getTasks()) {
                File file = new File(sourcePosition.getFile().getPath());
                if (task.getFiles().contains(file)) {
                    return task.getName();
                }
            }
        }
        return null;
    }

    private @NotNull String getModuleName(@NotNull XSourcePosition sourcePosition) {
        return sourcePosition.getFile().getNameWithoutExtension();
    }

    @Override
    public void runToPosition(@NotNull XSourcePosition sourcePosition, @Nullable XSuspendContext context) {
        String taskName = getTaskName(sourcePosition);
        if (taskName == null) return;
        registerBreakpoint(taskName, getModuleName(sourcePosition), sourcePosition.getLine())
                .thenComposeAsync(breakpoint -> {
                    if (breakpoint == null) return CompletableFuture.completedFuture(null);
                    return getExecutionService().onExecutionState().subscribe(SubscriptionPriority.MEDIUM, ((entity, event) -> {
                        if (event.getState() == ExecutionState.STOPPED) {
                            unregisterBreakpoint(breakpoint);
                        }
                    }));
                }).thenComposeAsync(entity -> startAsync(ExecutionMode.CONTINUE));
    }

    @Override
    public @NotNull Promise<Object> stopAsync() {
        AsyncPromise<Object> promise = new AsyncPromise<>();
        stopAsync(StopMode.STOP)
                .handleAsync((response, throwable) -> {
                    networkEngine.closeAsync();
                    if (throwable != null) {
                        promise.setError(throwable);
                    } else {
                        promise.setResult(response);
                    }
                    return null;
                });
        return promise;
    }

    @Override
    public void resume(@Nullable XSuspendContext context) {
        startAsync(ExecutionMode.CONTINUE);
    }
}

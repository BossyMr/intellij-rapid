package com.bossymr.rapid.ide.execution.debugger;

import com.bossymr.network.NetworkManager;
import com.bossymr.network.SubscriptionPriority;
import com.bossymr.rapid.ide.execution.RapidProcessHandler;
import com.bossymr.rapid.ide.execution.configurations.TaskState;
import com.bossymr.rapid.ide.execution.debugger.breakpoints.RapidLineBreakpointHandler;
import com.bossymr.rapid.ide.execution.debugger.frame.RapidSuspendContext;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.robot.CloseableMastership;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import com.bossymr.rapid.robot.network.robotware.rapid.execution.*;
import com.bossymr.rapid.robot.network.robotware.rapid.task.StackFrame;
import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskActiveState;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.*;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.Breakpoint;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.Program;
import com.intellij.execution.process.ProcessHandler;
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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Phaser;

public class RapidDebugProcess extends XDebugProcess {
    public static final @NotNull Key<BreakpointEntity> BREAKPOINT_KEY = Key.create("RapidBreakpointKey");
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
    private final @NotNull NetworkManager manager;
    private final @NotNull RapidProcessHandler processHandler;

    public RapidDebugProcess(@NotNull Project project, @NotNull XDebugSession session, @NotNull List<TaskState> taskStates, @NotNull NetworkManager manager) throws IOException, InterruptedException {
        super(session);
        this.project = project;
        this.tasks = taskStates;
        this.processHandler = new RapidProcessHandler(manager);
        this.manager = processHandler.getNetworkManager();
        ExecutionService executionService = this.manager.createService(ExecutionService.class);
        executionService.onExecutionState().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
            switch (event.getState()) {
                case RUNNING -> getSession().sessionResumed();
                case STOPPED -> {
                    phaser.register();
                    try {
                        List<Task> tasks = this.manager.createService(TaskService.class).getTasks().get();
                        for (Task task : tasks) {
                            if (task.getActivityState() == TaskActiveState.DISABLED) return;
                            StackFrame stackFrame = task.getStackFrame(1).get();
                            StackFrame nextStackFrame = task.getStackFrame(2).get();
                            onProgramStop(task, stackFrame, nextStackFrame);
                        }
                    } catch (IOException e) {
                        try {
                            entity.unsubscribe();
                        } catch (IOException | InterruptedException ignored) {}
                        onFailure(e);
                    } catch (InterruptedException ignored) {
                    } finally {
                        phaser.arriveAndDeregister();
                    }
                }
            }
        });
    }

    private @NotNull ExecutionService getExecutionService() {
        return manager.createService(ExecutionService.class);
    }

    @Override
    protected @NotNull ProcessHandler doGetProcessHandler() {
        return processHandler;
    }

    @Override
    public void sessionInitialized() {
        getSession().getConsoleView().attachToProcess(processHandler);
        ExecutionService executionService = getExecutionService();
        try {
            executionService.resetProgramPointer().get();
            start(ExecutionMode.CONTINUE);
        } catch (IOException e) {
            onFailure(e);
        } catch (InterruptedException ignored) {}
    }

    private void onProgramStop(@NotNull Task task, @NotNull StackFrame stackFrame, @NotNull StackFrame nextStackFrame) {
        logger.debug("Process paused at '" + stackFrame + "' invoked by '" + nextStackFrame + "'");
        if (hasStopped(stackFrame) || hasStopped(nextStackFrame)) {
            logger.debug("Process stopped");
            getSession().stop();
            try {
                manager.close();
            } catch (IOException e) {
                logger.error(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            RapidSuspendContext suspendContext = new RapidSuspendContext(project, breakpoints, manager, task, stackFrame);
            for (XBreakpoint<?> breakpoint : breakpoints) {
                if (isAtBreakpoint(stackFrame, breakpoint)) {
                    logger.debug("Breakpoint '" + breakpoint + "' reached");
                    getSession().breakpointReached(breakpoint, null, suspendContext);
                    return;
                }
            }
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

    private void start(@NotNull ExecutionMode executionMode) throws IOException, InterruptedException {
        phaser.register();
        phaser.awaitAdvance(phaser.arriveAndDeregister());
        try (CloseableMastership ignored = CloseableMastership.withMastership(manager, MastershipType.RAPID)) {
            getExecutionService().start(RegainMode.REGAIN, executionMode, ExecutionCycle.ONCE, ConditionState.CALLCHAIN, BreakpointMode.ENABLED, TaskExecutionMode.NORMAL).get();
        }
    }

    private void stop(@NotNull StopMode stopMode) throws IOException, InterruptedException {
        logger.debug("Stopping process with mode: " + stopMode);
        getExecutionService().stop(stopMode, TaskExecutionMode.NORMAL).get();
    }

    /**
     * Registers a new breakpoint at the specified position.
     *
     * @param moduleName the name of the module.
     * @param line the line number, starting at 0.
     * @return the asynchronous request.
     */
    public @Nullable BreakpointEntity registerBreakpoint(@NotNull String taskName, @NotNull String moduleName, int line) throws IOException, InterruptedException {
        TaskService taskService = manager.createService(TaskService.class);
        Task task = taskService.getTask(taskName).get();
        Program program = task.getProgram().get();
        program.setBreakpoint(moduleName, line + 1, 0).get();
        Breakpoint breakpoint = findBreakpoint(program.getBreakpoints().get(), moduleName, line);
        if (breakpoint == null) {
            return null;
        }
        return new BreakpointEntity(taskName, breakpoint);
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
    public void registerBreakpoint(@NotNull XBreakpoint<?> breakpoint) throws IOException, InterruptedException {
        XSourcePosition sourcePosition = breakpoint.getSourcePosition();
        if (sourcePosition == null) return;
        String taskName = getTaskName(sourcePosition);
        if (taskName == null) return;
        String moduleName = getModuleName(sourcePosition);
        BreakpointEntity breakpointEntity = registerBreakpoint(taskName, moduleName, sourcePosition.getLine());
        breakpoint.putUserData(BREAKPOINT_KEY, breakpointEntity);
        if (breakpoint instanceof XLineBreakpoint<?> lineBreakpoint) {
            getSession().setBreakpointVerified(lineBreakpoint);
        }
        breakpoints.add(breakpoint);
    }

    private void onFailure(@NotNull Throwable throwable) {
        try {
            manager.close();
        } catch (IOException | InterruptedException ignored) {}
        getSession().reportError(throwable.getLocalizedMessage());

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
     */
    public void unregisterBreakpoint(@NotNull String taskName, @NotNull Breakpoint breakpoint) throws IOException, InterruptedException {
        getExecutionService().onExecutionState().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
            if (event.getState() == ExecutionState.STOPPED) {
                phaser.register();
                try {
                    TaskService taskService = manager.createService(TaskService.class);
                    Task task = taskService.getTask(taskName).get();
                    StackFrame stackFrame = task.getStackFrame(1).get();
                    if (!stackFrame.toTextRange().equals(breakpoint.toTextRange())) {
                        logger.debug("Removing breakpoint '" + breakpoint + "'");
                        entity.unsubscribe();
                        ModuleEntity module = findModule(taskName, breakpoint.getModuleName());
                        if (module != null) {
                            ModuleText moduleText = module.getText(breakpoint.getStartRow(), breakpoint.getStartColumn(), breakpoint.getEndRow(), breakpoint.getEndColumn()).get();
                            try (CloseableMastership ignored = CloseableMastership.withMastership(manager, MastershipType.RAPID)) {
                                module.setText(task.getName(), ReplaceMode.REPLACE, QueryMode.FORCE, breakpoint.getStartRow(), breakpoint.getStartColumn(), breakpoint.getEndRow(), breakpoint.getEndColumn(), moduleText.getText()).get();
                            }
                        }
                    } else {
                        logger.debug("Could not remove breakpoint '" + breakpoint + "' at '" + stackFrame + "'");
                    }
                } catch (IOException e) {
                    onFailure(e);
                } catch (InterruptedException ignored) {
                } finally {
                    phaser.arriveAndDeregister();
                }
            }
        });
    }

    private @Nullable ModuleEntity findModule(@NotNull String taskName, @NotNull String moduleName) throws IOException, InterruptedException {
        TaskService taskService = manager.createService(TaskService.class);
        Task task = taskService.getTask(taskName).get();
        List<ModuleInfo> moduleInfos = task.getModules().get();
        for (ModuleInfo moduleInfo : moduleInfos) {
            if (moduleInfo.getName().equals(moduleName)) {
                return moduleInfo.getModule().get();
            }
        }
        logger.warn("Could not find module '" + moduleName + "' in " + moduleInfos);
        return null;
    }

    /**
     * Unregisters the specified breakpoint.
     *
     * @param breakpoint the breakpoint.
     */
    public void unregisterBreakpoint(@NotNull XBreakpoint<?> breakpoint) throws IOException, InterruptedException {
        breakpoints.remove(breakpoint);
        BreakpointEntity result = breakpoint.getUserData(BREAKPOINT_KEY);
        if (result == null) {
            // This breakpoint has not been registered.
            return;
        }
        unregisterBreakpoint(result.taskName(), result.breakpoint());
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
        try {
            stop(StopMode.INSTRUCTION);
        } catch (IOException e) {
            onFailure(e);
        } catch (InterruptedException ignored) {}
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext context) {
        try {
            start(ExecutionMode.STEP_OVER);
        } catch (IOException e) {
            onFailure(e);
        } catch (InterruptedException ignored) {}
    }

    @Override
    public void startForceStepInto(@Nullable XSuspendContext context) {
        startStepInto(context);
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        try {
            start(ExecutionMode.STEP_IN);
        } catch (IOException e) {
            onFailure(e);
        } catch (InterruptedException ignored) {}
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        try {
            start(ExecutionMode.STEP_OUT);
        } catch (IOException e) {
            onFailure(e);
        } catch (InterruptedException ignored) {}
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
            RobotService service = RobotService.getInstance();
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
        try {
            BreakpointEntity breakpointEntity = registerBreakpoint(taskName, getModuleName(sourcePosition), sourcePosition.getLine());
            if (breakpointEntity == null) return;
            getExecutionService().onExecutionState().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
                if (event.getState() == ExecutionState.STOPPED) {
                    try {
                        unregisterBreakpoint(breakpointEntity.taskName(), breakpointEntity.breakpoint());
                    } catch (IOException e) {
                        try {
                            entity.unsubscribe();
                        } catch (IOException | InterruptedException ignored) {}
                        onFailure(e);
                    } catch (InterruptedException ignored) {}
                }
            });
            start(ExecutionMode.CONTINUE);
        } catch (IOException e) {
            onFailure(e);
        } catch (InterruptedException ignored) {}
    }

    @Override
    public void stop() {
        getSession().stop();
    }

    @Override
    public void resume(@Nullable XSuspendContext context) {
        try {
            start(ExecutionMode.CONTINUE);
        } catch (IOException e) {
            onFailure(e);
        } catch (InterruptedException ignored) {}
    }

    public record BreakpointEntity(@NotNull String taskName, @NotNull Breakpoint breakpoint) {}
}

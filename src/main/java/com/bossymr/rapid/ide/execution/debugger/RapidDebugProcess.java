package com.bossymr.rapid.ide.execution.debugger;

import com.bossymr.network.NetworkAction;
import com.bossymr.network.NetworkManager;
import com.bossymr.network.ResponseStatusException;
import com.bossymr.network.SubscriptionPriority;
import com.bossymr.network.client.NetworkRequest;
import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.execution.RapidProcessHandler;
import com.bossymr.rapid.ide.execution.configurations.TaskState;
import com.bossymr.rapid.ide.execution.debugger.breakpoints.RapidLineBreakpointHandler;
import com.bossymr.rapid.ide.execution.debugger.frame.RapidSuspendContext;
import com.bossymr.rapid.ide.execution.filter.RapidFileFilter;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.robot.CloseableMastership;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import com.bossymr.rapid.robot.network.robotware.rapid.execution.*;
import com.bossymr.rapid.robot.network.robotware.rapid.task.*;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ModuleEntity;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ModuleText;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.QueryMode;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ReplaceMode;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;

public class RapidDebugProcess extends XDebugProcess {
    public static final @NotNull Key<BreakpointEntity> BREAKPOINT_KEY = Key.create("RapidBreakpointKey");
    private static final Logger logger = Logger.getInstance(RapidDebugProcess.class);

    private final @NotNull RapidDebuggerEditorsProvider editorsProvider = new RapidDebuggerEditorsProvider();
    private final @NotNull Set<XBreakpointHandler<?>> breakpointHandlers = Set.of(
            new RapidLineBreakpointHandler(this)
    );
    private final @NotNull Project project;

    private final @NotNull ExecutorService executorService;

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

    public RapidDebugProcess(@NotNull Project project, @NotNull XDebugSession session, @NotNull ExecutorService executorService, @NotNull List<TaskState> taskStates, @NotNull NetworkManager manager) {
        super(session);
        this.project = project;
        this.executorService = executorService;
        this.tasks = taskStates;
        this.processHandler = new RapidProcessHandler(manager, tasks, executorService) {
            @Override
            protected void handleException(@NotNull Throwable throwable) throws IOException, InterruptedException {
                super.handleException(throwable);
                getSession().reportError(throwable.getLocalizedMessage());
                getSession().stop();
            }
        };
        this.manager = processHandler.getNetworkManager();
    }

    public void execute(@NotNull NetworkRunnable callable) {
        executorService.submit(() -> {
            try {
                callable.compute();
            } catch (Exception e) {
                getSession().reportError(RapidBundle.message("run.execution.exception"));
                getSession().stop();
            }
            return null;
        });
    }

    public @NotNull NetworkManager getManager() {
        return manager;
    }

    private void onProgramStop() throws IOException, InterruptedException {
        if (processHandler.isProcessTerminating()) {
            return;
        }
        phaser.register();
        try {
            List<Task> tasks = manager.createService(TaskService.class).getTasks().get();
            for (Task task : tasks) {
                if (task.getActivityState() == TaskActiveState.DISABLED) {
                    continue;
                }
                if (task.getExecutionState() != TaskExecutionState.STOPPED) {
                    continue;
                }
                StackFrame stackFrame = task.getStackFrame(1).get();
                onProgramStop(task, stackFrame);
            }
        } finally {
            phaser.arriveAndDeregister();
        }
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
        getSession().getConsoleView().addMessageFilter(new RapidFileFilter(project));
        execute(() -> {
            if (processHandler.check()) {
                getSession().stop();
                return;
            }
            setup();
            ExecutionService executionService = getExecutionService();
            try {
                executionService.resetProgramPointer().get();
                start(ExecutionMode.CONTINUE);
            } catch (IOException | InterruptedException ignored) {}
        });
    }

    public void setup() throws IOException, InterruptedException {
        processHandler.setupEventLog();
        ExecutionService executionService = getExecutionService();
        try (CloseableMastership ignored = CloseableMastership.withMastership(manager, MastershipType.RAPID)) {
            executionService.resetProgramPointer().get();
        }
        executionService.onExecutionState().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
            switch (event.getState()) {
                case RUNNING -> getSession().sessionResumed();
                case STOPPED -> {
                    try {
                        onProgramStop();
                    } catch (IOException | InterruptedException ignored) {}
                }
            }
        });
    }

    private void onProgramStop(@NotNull Task task, @NotNull StackFrame stackFrame) {
        logger.debug("Process paused at '" + stackFrame + "'");
        if (isStopped(stackFrame)) {
            logger.debug("Process stopped");
            getSession().stop();
            return;
        }
        RapidSuspendContext suspendContext = new RapidSuspendContext(project, breakpoints, this, task, stackFrame);
        for (XBreakpoint<?> breakpoint : breakpoints) {
            if (isAtBreakpoint(stackFrame, breakpoint)) {
                logger.debug("Breakpoint '" + breakpoint + "' reached");
                boolean shouldSuspend = getSession().breakpointReached(breakpoint, null, suspendContext);
                if (!(shouldSuspend)) {
                    execute(() -> start(ExecutionMode.CONTINUE));
                }
                return;
            }
        }
        logger.debug("Process paused");
        getSession().positionReached(suspendContext);
    }

    private boolean isStopped(@NotNull StackFrame stackFrame) {
        return stackFrame.getStartRow() == 0 && stackFrame.getStartColumn() == 0 && stackFrame.getEndRow() == 0 && stackFrame.getEndColumn() == 0;
    }

    private boolean isAtBreakpoint(@NotNull StackFrame stackFrame, @NotNull XBreakpoint<?> breakpoint) {
        XSourcePosition sourcePosition = breakpoint.getSourcePosition();
        if (sourcePosition == null) return false;
        String breakpointModule = sourcePosition.getFile().getNameWithoutExtension();
        String currentModule = stackFrame.getRoutine().split("/")[2];
        if (!(breakpointModule.equals(currentModule))) return false;
        return sourcePosition.getLine() >= stackFrame.getStartRow() - 1 && sourcePosition.getLine() <= stackFrame.getEndRow() - 1;
    }

    private void start(@NotNull ExecutionMode executionMode) throws IOException, InterruptedException {
        phaser.register();
        phaser.awaitAdvance(phaser.arriveAndDeregister());
        ExecutionService executionService = getExecutionService();
        try (CloseableMastership ignored = CloseableMastership.withMastership(manager, MastershipType.RAPID)) {
            executionService.start(RegainMode.REGAIN, executionMode, ExecutionCycle.ONCE, ConditionState.CALLCHAIN, BreakpointMode.ENABLED, TaskExecutionMode.NORMAL).get();
        }
        Thread.sleep(100);
        ExecutionStatus executionStatus = executionService.getState().get();
        if (executionStatus.getState() == ExecutionState.STOPPED) {
            onProgramStop();
        }
    }

    /**
     * Registers a new breakpoint at the specified position.
     *
     * @param moduleName the name of the module.
     * @param line the line number, starting at 0.
     * @return the asynchronous request.
     */
    public @Nullable BreakpointEntity registerBreakpoint(@NotNull String taskName, @NotNull String moduleName, int line) throws IOException, InterruptedException {
        NetworkAction action = new NetworkAction(manager) {
            @Override
            protected boolean onFailure(@NotNull NetworkRequest<?> request, @NotNull Throwable throwable) throws IOException, InterruptedException {
                return false;
            }
        };
        try (action) {
            TaskService taskService = action.createService(TaskService.class);
            Task task = taskService.getTask(taskName).get();
            Program program = task.getProgram().get();
            program.setBreakpoint(moduleName, line + 1, 0).get();
            Breakpoint breakpoint = findBreakpoint(program.getBreakpoints().get(), moduleName, line);
            if (breakpoint == null) {
                return null;
            }
            return new BreakpointEntity(taskName, breakpoint);
        } catch (ResponseStatusException e) {
            return null;
        }
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
        execute(() -> {
            XSourcePosition sourcePosition = breakpoint.getSourcePosition();
            if (sourcePosition == null) return;
            String taskName = getTaskName(sourcePosition);
            if (taskName == null) return;
            String moduleName = getModuleName(sourcePosition);
            BreakpointEntity breakpointEntity = registerBreakpoint(taskName, moduleName, sourcePosition.getLine());
            breakpoint.putUserData(BREAKPOINT_KEY, breakpointEntity);
            if (breakpoint instanceof XLineBreakpoint<?> lineBreakpoint) {
                if (breakpointEntity != null) {
                    getSession().setBreakpointVerified(lineBreakpoint);
                } else {
                    getSession().setBreakpointInvalid(lineBreakpoint, null);
                }
            }
            breakpoints.add(breakpoint);
        });
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
        Task task = manager.createService(TaskService.class).getTask(taskName).get();
        TaskExecutionState executionState = task.getExecutionState();
        if (executionState == TaskExecutionState.STOPPED) {
            boolean unregistered = unregisterBreakpoint(task, breakpoint);
            if (unregistered) {
                return;
            }
        }
        getExecutionService().onExecutionState().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> execute(() -> {
            if (event.getState() == ExecutionState.STOPPED) {
                boolean unregistered = unregisterBreakpoint(task, breakpoint);
                if (unregistered) {
                    entity.unsubscribe();
                }
            }
        }));
    }

    private boolean unregisterBreakpoint(@NotNull Task task, @NotNull Breakpoint breakpoint) throws IOException, InterruptedException {
        phaser.register();
        try {
            StackFrame stackFrame = task.getStackFrame(1).get();
            if (!(stackFrame.toTextRange().equals(breakpoint.toTextRange()))) {
                logger.debug("Removing breakpoint '" + breakpoint + "'");
                ModuleEntity module = task.getModule(breakpoint.getModuleName()).get();
                if (module != null) {
                    unregisterBreakpoint(manager, module, task, breakpoint);
                }
                return true;
            } else {
                logger.debug("Could not remove breakpoint '" + breakpoint + "' at '" + stackFrame + "'");
            }
        } finally {
            phaser.arriveAndDeregister();
        }
        return false;
    }

    public static void unregisterBreakpoint(@NotNull NetworkManager manager, @NotNull ModuleEntity module, @NotNull Task task, @NotNull Breakpoint breakpoint) throws IOException, InterruptedException {
        ModuleText moduleText = module.getText(breakpoint.getStartRow(), breakpoint.getStartColumn(), breakpoint.getEndRow(), breakpoint.getEndColumn()).get();
        try (CloseableMastership ignored = CloseableMastership.withMastership(manager, MastershipType.RAPID)) {
            module.setText(task.getName(), ReplaceMode.REPLACE, QueryMode.FORCE, breakpoint.getStartRow(), breakpoint.getStartColumn(), breakpoint.getEndRow(), breakpoint.getEndColumn(), moduleText.getText()).get();
        }
    }

    /**
     * Unregisters the specified breakpoint.
     *
     * @param breakpoint the breakpoint.
     */
    public void unregisterBreakpoint(@NotNull XBreakpoint<?> breakpoint) {
        execute(() -> {
            breakpoints.remove(breakpoint);
            BreakpointEntity result = breakpoint.getUserData(BREAKPOINT_KEY);
            if (result == null) {
                return;
            }
            unregisterBreakpoint(result.taskName(), result.breakpoint());
        });
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
        execute(() -> {
            logger.debug("Stopping process with mode: " + StopMode.INSTRUCTION);
            getExecutionService().stop(StopMode.INSTRUCTION, TaskExecutionMode.NORMAL).get();
        });
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext context) {
        execute(() -> start(ExecutionMode.STEP_OVER));
    }

    @Override
    public void startForceStepInto(@Nullable XSuspendContext context) {
        startStepInto(context);
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        execute(() -> start(ExecutionMode.STEP_IN));
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        execute(() -> start(ExecutionMode.STEP_OUT));
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
        execute(() -> {
            String taskName = getTaskName(sourcePosition);
            if (taskName == null) return;
            BreakpointEntity breakpointEntity = registerBreakpoint(taskName, getModuleName(sourcePosition), sourcePosition.getLine());
            if (breakpointEntity == null) return;
            getExecutionService().onExecutionState().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> execute(() -> {
                if (event.getState() == ExecutionState.STOPPED) {
                    unregisterBreakpoint(breakpointEntity.taskName(), breakpointEntity.breakpoint());
                }
            }));
            start(ExecutionMode.CONTINUE);
        });
    }

    @Override
    public void stop() {
        getSession().stop();
    }

    @Override
    public void resume(@Nullable XSuspendContext context) {
        execute(() -> start(ExecutionMode.CONTINUE));
    }

    @FunctionalInterface
    public interface NetworkRunnable {
        void compute() throws Exception;
    }

    public record BreakpointEntity(@NotNull String taskName, @NotNull Breakpoint breakpoint) {}
}

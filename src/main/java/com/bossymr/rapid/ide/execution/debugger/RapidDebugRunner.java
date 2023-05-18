package com.bossymr.rapid.ide.execution.debugger;

import com.bossymr.network.NetworkAction;
import com.bossymr.network.NetworkManager;
import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.execution.RapidRunProfileState;
import com.bossymr.rapid.ide.execution.configurations.RapidRunConfiguration;
import com.bossymr.rapid.robot.CloseableMastership;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import com.bossymr.rapid.robot.network.robotware.rapid.execution.ExecutionService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskActiveState;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.*;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.Breakpoint;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.Program;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.AsyncProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.concurrency.Promises;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@code ProgramRunner} for starting a debugging session.
 */
public class RapidDebugRunner extends AsyncProgramRunner<RunnerSettings> {

    private static final Logger logger = Logger.getInstance(RapidDebugRunner.class);

    public static final @NotNull String RUNNER_ID = "RapidDebugRunner";

    @Override
    public @NotNull @NonNls String getRunnerId() {
        return RUNNER_ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) &&
                profile instanceof RapidRunConfiguration;
    }

    @Override
    protected @NotNull Promise<RunContentDescriptor> execute(@NotNull ExecutionEnvironment environment, @NotNull RunProfileState runProfileState) throws ExecutionException {
        if (!(runProfileState instanceof RapidRunProfileState state)) {
            return Promises.rejectedPromise();
        }
        try {
            NetworkManager manager = state.setupExecution();
            setupExecution(manager);
            Project project = environment.getProject();
            AsyncPromise<RunContentDescriptor> promise = new AsyncPromise<>();
            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    XDebugSession session = XDebuggerManager.getInstance(project).startSession(environment, new XDebugProcessStarter() {
                        @Override
                        public @NotNull XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException {
                            try {
                                return new RapidDebugProcess(project, session, state.getTasks(), manager);
                            } catch (IOException | InterruptedException e) {
                                throw new ExecutionException(e);
                            }
                        }
                    });
                    promise.setResult(session.getRunContentDescriptor());
                } catch (ExecutionException e) {
                    promise.setError(e);
                }
            });
            return promise;
        } catch (IOException | InterruptedException e) {
            throw new ExecutionException(RapidBundle.message("run.execution.exception"));
        }
    }

    private void removeBreakpoint(@NotNull NetworkManager manager, @NotNull Task task, @NotNull Map<String, ModuleEntity> modules, @NotNull Breakpoint breakpoint) throws IOException, InterruptedException {
        ModuleEntity module = modules.get(breakpoint.getModuleName());
        ModuleText moduleText = module.getText(breakpoint.getStartRow(), breakpoint.getStartColumn(), breakpoint.getEndRow(), breakpoint.getEndColumn()).get();
        try (CloseableMastership ignored = CloseableMastership.withMastership(manager, MastershipType.RAPID)) {
            module.setText(task.getName(), ReplaceMode.REPLACE, QueryMode.TRY, breakpoint.getStartRow(), breakpoint.getStartColumn(), breakpoint.getEndRow(), breakpoint.getEndColumn(), moduleText.getText()).get();
        }
    }

    private @NotNull Map<String, ModuleEntity> getModules(@NotNull Task task) throws IOException, InterruptedException {
        Map<String, ModuleEntity> modules = new ConcurrentHashMap<>();
        List<ModuleInfo> moduleInfos = task.getModules().get();
        for (ModuleInfo moduleInfo : moduleInfos) {
            ModuleEntity module = moduleInfo.getModule().get();
            modules.put(module.getName(), module);
        }
        return modules;
    }

    /**
     * Resets the robot to allow for a new debugging session to be executed.
     */
    private void setupExecution(@NotNull NetworkManager manager) throws IOException, InterruptedException {
        try (NetworkManager action = new NetworkAction(manager)) {
            ExecutionService executionService = action.createService(ExecutionService.class);
            try (CloseableMastership ignored = CloseableMastership.withMastership(action, MastershipType.RAPID)) {
                executionService.resetProgramPointer().get();
            }
            TaskService taskService = action.createService(TaskService.class);
            List<Task> tasks = taskService.getTasks().get();
            for (Task task : tasks) {
                if (task.getActivityState() == TaskActiveState.DISABLED) {
                    continue;
                }
                Map<String, ModuleEntity> modules = getModules(task);
                Program program = task.getProgram().get();
                List<Breakpoint> breakpoints = program.getBreakpoints().get();
                for (Breakpoint breakpoint : breakpoints) {
                    removeBreakpoint(action, task, modules, breakpoint);
                }
            }
        }
    }
}

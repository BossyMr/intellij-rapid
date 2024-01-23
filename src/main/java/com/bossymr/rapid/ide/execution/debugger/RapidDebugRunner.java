package com.bossymr.rapid.ide.execution.debugger;

import com.bossymr.network.NetworkAction;
import com.bossymr.network.NetworkManager;
import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.execution.RapidRunProfileState;
import com.bossymr.rapid.ide.execution.configurations.RapidRunConfiguration;
import com.bossymr.rapid.robot.CloseableMastership;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
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
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A {@code ProgramRunner} for starting a debugging session.
 */
public class RapidDebugRunner extends AsyncProgramRunner<RunnerSettings> {

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
        FileDocumentManager.getInstance().saveAllDocuments();
        Project project = environment.getProject();
        AsyncPromise<RunContentDescriptor> promise = new AsyncPromise<>();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        XDebuggerManager debuggerManager = XDebuggerManager.getInstance(project);
        executorService.submit(() -> ReadAction.compute((ThrowableComputable<Void, Exception>) () -> {
            XDebugSession session = debuggerManager.startSession(environment, new XDebugProcessStarter() {
                @Override
                public @NotNull XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException {
                    try {
                        NetworkManager manager = state.getNetworkManager();
                        return new RapidDebugProcess(project, session, executorService, state.getTasks(), manager, () -> {
                            state.setupExecution(manager);
                            setupExecution(manager);
                            return null;
                        });
                    } catch (IOException | InterruptedException e) {
                        throw new ExecutionException(RapidBundle.message("run.execution.exception"));
                    }
                }
            });
            promise.setResult(session.getRunContentDescriptor());
            return null;
        }));
        return promise;
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

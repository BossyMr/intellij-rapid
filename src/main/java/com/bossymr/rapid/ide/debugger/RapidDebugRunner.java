package com.bossymr.rapid.ide.debugger;

import com.bossymr.rapid.ide.execution.RapidRunProfileState;
import com.bossymr.rapid.ide.execution.configurations.RapidRunConfiguration;
import com.bossymr.rapid.robot.network.RobotService;
import com.bossymr.rapid.robot.network.robotware.mastership.CloseableMastership;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipDomain;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipService;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import com.bossymr.rapid.robot.network.robotware.rapid.execution.ExecutionService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.Module;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.*;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.Breakpoint;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.Program;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.AsyncProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task.Backgroundable;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @NotNull
    @Override
    protected Promise<RunContentDescriptor> execute(@NotNull ExecutionEnvironment environment, @NotNull RunProfileState runProfileState) throws ExecutionException {
        if (runProfileState instanceof RapidRunProfileState state) {
            AsyncPromise<RunContentDescriptor> promise = new AsyncPromise<>();
            new Backgroundable(environment.getProject(), ExecutionBundle.message("progress.title.starting.run.configuration", environment.getRunProfile().getName())) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    ApplicationManager.getApplication().invokeAndWait(() -> {
                        try {
                            RunContentDescriptor descriptor = XDebuggerManager.getInstance(environment.getProject()).startSession(environment, new XDebugProcessStarter() {
                                @Override
                                public @NotNull XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException {
                                    try {
                                        RobotService robotService = state.getRobotService();
                                        Task task = getTask(robotService, state);
                                        Program program = task.getProgram().send();
                                        prepare(robotService, task, program);
                                        return new RapidDebugProcess(environment.getProject(), session, robotService, task, program);
                                    } catch (IOException | InterruptedException e) {
                                        throw new ExecutionException(e);
                                    }
                                }
                            }).getRunContentDescriptor();
                            promise.setResult(descriptor);
                        } catch (ExecutionException e) {
                            promise.setError(e);
                        }
                    });
                }
            }.queue();
            return promise;
        }
        return Promises.rejectedPromise();
    }

    private Task getTask(@NotNull RobotService robotService, @NotNull RapidRunProfileState state) throws IOException, InterruptedException {
        String taskName = state.getTask().getName();
        TaskService taskService = robotService.getRobotWareService().getRapidService().getTaskService();
        return taskService.getTask(taskName).send();
    }

    /**
     * Resets the robot to allow for a new debugging session to be executed.
     */
    private void prepare(@NotNull RobotService robotService, @NotNull Task task, @NotNull Program program) throws InterruptedException, IOException {
        /*
         * Clear any previous execution state by resetting program pointer.
         */
        ExecutionService executionService = robotService.getRobotWareService().getRapidService().getExecutionService();
        executionService.resetProgramPointer().send();
        /*
         * Clear any existing breakpoints.
         */
        List<Breakpoint> breakpoints = program.getBreakpoints().send();
        List<ModuleInfo> moduleInfos = task.getModules().send();
        Map<String, Module> modules = new HashMap<>();
        for (Breakpoint breakpoint : breakpoints) {
            Module module = null;
            if (modules.containsKey(breakpoint.getModuleName())) {
                module = modules.get(breakpoint.getModuleName());
            } else {
                for (ModuleInfo moduleInfo : moduleInfos) {
                    if (moduleInfo.getName().equals(breakpoint.getModuleName())) {
                        module = moduleInfo.getModule().send();
                        modules.put(breakpoint.getModuleName(), module);
                    }
                }
            }
            if (module == null) {
                throw new IllegalStateException();
            }
            ModuleText moduleText = module.getText(breakpoint.getStartRow(), breakpoint.getStartColumn(), breakpoint.getEndRow(), breakpoint.getEndColumn()).send();
            MastershipService mastershipService = robotService.getRobotWareService().getMastershipService();
            MastershipDomain mastershipDomain = mastershipService.getDomain(MastershipType.RAPID).send();
            try (CloseableMastership ignored = CloseableMastership.request(mastershipDomain)) {
                module.setText(task.getName(), ReplaceMode.REPLACE, QueryMode.TRY, breakpoint.getStartRow(), breakpoint.getStartColumn(), breakpoint.getEndRow(), breakpoint.getEndColumn(), moduleText.getText()).send();
            }
        }
    }
}

package com.bossymr.rapid.ide.debugger;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.client.NetworkEngine;
import com.bossymr.rapid.ide.execution.RapidRunProfileState;
import com.bossymr.rapid.ide.execution.configurations.RapidRunConfiguration;
import com.bossymr.rapid.robot.network.robotware.mastership.CloseableMastership;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipService;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import com.bossymr.rapid.robot.network.robotware.rapid.execution.ExecutionService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskActiveState;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ModuleEntity;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ModuleInfo;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.QueryMode;
import com.bossymr.rapid.robot.network.robotware.rapid.task.module.ReplaceMode;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.Breakpoint;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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
                    ApplicationManager.getApplication().invokeLater(() -> state.setupExecution().thenComposeAsync(networkEngine -> prepare(networkEngine))
                            .thenAcceptAsync(networkEngine -> ApplicationManager.getApplication().invokeAndWait(() -> {
                                try {
                                    RunContentDescriptor descriptor = XDebuggerManager.getInstance(environment.getProject()).startSession(environment, new XDebugProcessStarter() {
                                        @Override
                                        public @NotNull XDebugProcess start(@NotNull XDebugSession session) {
                                            return new RapidDebugProcess(environment.getProject(), session, state.getTaskStates(), networkEngine);
                                        }
                                    }).getRunContentDescriptor();
                                    promise.setResult(descriptor);
                                } catch (ExecutionException e) {
                                    promise.setError(e);
                                }
                            })));
                }
            }.queue();
            return promise;
        }
        return Promises.rejectedPromise();
    }

    private @NotNull CompletableFuture<Void> removeBreakpoint(@NotNull Task task, @NotNull Map<String, ModuleEntity> modules, @NotNull Breakpoint breakpoint) {
        ModuleEntity module = modules.get(breakpoint.getModuleName());
        return module.getText(breakpoint.getStartRow(), breakpoint.getStartColumn(), breakpoint.getEndRow(), breakpoint.getEndColumn()).sendAsync()
                .thenComposeAsync(text -> breakpoint.getNetworkEngine().createService(MastershipService.class).getDomain(MastershipType.RAPID).sendAsync()
                        .thenComposeAsync(domain -> {
                            NetworkCall<Void> networkCall = module.setText(task.getName(), ReplaceMode.REPLACE, QueryMode.TRY, breakpoint.getStartRow(), breakpoint.getStartColumn(), breakpoint.getEndRow(), breakpoint.getEndColumn(), text.getText());
                            return CloseableMastership.requestAsync(domain, networkCall::sendAsync);
                        }));
    }

    private @NotNull CompletableFuture<Map<String, ModuleEntity>> getModules(@NotNull Task task) {
        Map<String, ModuleEntity> modules = new ConcurrentHashMap<>();
        return task.getModules().sendAsync()
                .thenComposeAsync(moduleInfos -> {
                    List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
                    for (ModuleInfo moduleInfo : moduleInfos) {
                        completableFutures.add(moduleInfo.getModule().sendAsync()
                                .thenAcceptAsync(module -> modules.put(module.getName(), module)));
                    }
                    return CompletableFuture.allOf(completableFutures.toArray(CompletableFuture[]::new));
                }).thenApplyAsync(unused -> Map.copyOf(modules));
    }

    /**
     * Resets the robot to allow for a new debugging session to be executed.
     */
    private @NotNull CompletableFuture<NetworkEngine> prepare(@NotNull NetworkEngine networkEngine) {
        ExecutionService executionService = networkEngine.createService(ExecutionService.class);
        return executionService.resetProgramPointer().sendAsync()
                .thenComposeAsync(unused -> {
                    TaskService taskService = networkEngine.createService(TaskService.class);
                    return taskService.getTasks().sendAsync();
                }).thenComposeAsync(tasks -> {
                    List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
                    for (Task task : tasks) {
                        if (task.getActivityState() == TaskActiveState.DISABLED) continue;
                        completableFutures.add(task.getProgram().sendAsync()
                                .thenComposeAsync(program -> program.getBreakpoints().sendAsync())
                                .thenComposeAsync(breakpoints -> getModules(task)
                                        .thenComposeAsync(modules ->
                                                CompletableFuture.allOf(breakpoints.stream()
                                                        .map(breakpoint -> removeBreakpoint(task, modules, breakpoint))
                                                        .toList().toArray(CompletableFuture[]::new)))));
                    }
                    return CompletableFuture.allOf(completableFutures.toArray(CompletableFuture[]::new));
                }).thenApplyAsync(unused -> networkEngine);
    }
}

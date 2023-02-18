package com.bossymr.rapid.ide.execution;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.ServiceModel;
import com.bossymr.network.SubscriptionEntity;
import com.bossymr.network.SubscriptionPriority;
import com.bossymr.network.client.DelegatingNetworkEngine;
import com.bossymr.rapid.robot.network.EventLogCategory;
import com.bossymr.rapid.robot.network.RobotService;
import com.bossymr.rapid.robot.network.robotware.mastership.CloseableMastership;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipDomain;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import com.bossymr.rapid.robot.network.robotware.rapid.execution.*;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RapidProcessHandler extends ProcessHandler {

    private final @NotNull DelegatingNetworkEngine delegatingNetworkEngine;
    private final @NotNull RobotService robotService;

    public RapidProcessHandler(@NotNull RobotService robotService) {
        this.delegatingNetworkEngine = new DelegatingNetworkEngine(robotService.getNetworkEngine()) {
            @Override
            protected void onFailure(@NotNull NetworkCall<?> request, @NotNull Throwable throwable) {
                destroyProcess();
            }

            @Override
            protected void onFailure(@NotNull Throwable throwable) {
                destroyProcess();
            }
        };
        this.robotService = ServiceModel.move(robotService, delegatingNetworkEngine);
    }

    public void startProcess() throws IOException, InterruptedException {
        try {
            subscribeToOutput();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IOException exception) throw exception;
            throw new IOException(e.getCause());
        }
        MastershipDomain mastershipDomain = robotService.getRobotWareService().getMastershipService().getDomain(MastershipType.RAPID).send();
        try (CloseableMastership ignored = CloseableMastership.request(mastershipDomain)) {
            ExecutionService executionService = robotService.getRobotWareService().getRapidService().getExecutionService();
            executionService.onExecutionState().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
                if (event.getState() == ExecutionState.STOPPED) {
                    notifyProcessTerminated(0);
                    delegatingNetworkEngine.closeAsync();
                }
            }).get();
            executionService.resetProgramPointer().send();
            executionService.start(RegainMode.REGAIN, ExecutionMode.CONTINUE, ExecutionCycle.ONCE, ConditionState.CALLCHAIN, BreakpointMode.DISABLED, TaskExecutionMode.NORMAL).send();
            startNotify();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IOException exception) {
                throw exception;
            } else {
                throw new IOException(e.getCause());
            }
        }
    }

    private void subscribeToOutput() throws ExecutionException, InterruptedException {
        robotService.getRobotWareService().getEventLogService().getCategories("en").sendAsync()
                .thenComposeAsync(categories -> {
                    if (categories.isEmpty()) {
                        return CompletableFuture.completedFuture(null);
                    }
                    return subscribeToOutput(categories.get(0));
                }).get();
    }

    private @NotNull CompletableFuture<SubscriptionEntity> subscribeToOutput(@NotNull EventLogCategory category) {
        return category.onMessage().subscribe(SubscriptionPriority.MEDIUM, (entity, event) ->
                event.getMessage("en").sendAsync()
                        .thenAcceptAsync(message -> {
                            String text = message.getMessageType().name() + " ";
                            text += "[" + message.getTimestamp() + "]: ";
                            text += message.getMessageTitle() + '\n';
                            text += message.getDescription() + '\n';
                            String actions = message.getActions();
                            if (actions != null && actions.length() > 0) text += "Actions: " + actions + '\n';
                            String causes = message.getCauses();
                            if (causes != null && causes.length() > 0) text += "Causes: " + causes + '\n';
                            String consequences = message.getConsequences();
                            if (consequences != null && consequences.length() > 0)
                                text += "Consequences: " + consequences + '\n';
                            notifyTextAvailable(text, switch (message.getMessageType()) {
                                case INFORMATION, WARNING -> ProcessOutputType.STDOUT;
                                case ERROR -> ProcessOutputType.STDERR;
                            });
                        }));
    }

    @Override
    protected void destroyProcessImpl() {
        robotService.getRobotWareService().getRapidService().getExecutionService()
                .stop(StopMode.STOP, TaskExecutionMode.NORMAL).sendAsync()
                .thenComposeAsync((entity) -> delegatingNetworkEngine.closeAsync());
    }

    @Override
    protected void detachProcessImpl() {
        delegatingNetworkEngine.closeAsync();
        notifyProcessDetached();
    }

    @Override
    public boolean detachIsDefault() {
        return false;
    }

    @Override
    public @Nullable OutputStream getProcessInput() {
        return null;
    }
}

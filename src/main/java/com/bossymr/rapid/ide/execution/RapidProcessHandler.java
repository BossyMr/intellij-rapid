package com.bossymr.rapid.ide.execution;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.SubscriptionEntity;
import com.bossymr.network.SubscriptionPriority;
import com.bossymr.network.client.DelegatingNetworkEngine;
import com.bossymr.network.client.NetworkEngine;
import com.bossymr.rapid.robot.network.EventLogCategory;
import com.bossymr.rapid.robot.network.EventLogService;
import com.bossymr.rapid.robot.network.robotware.mastership.CloseableMastership;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipService;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import com.bossymr.rapid.robot.network.robotware.rapid.execution.*;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class RapidProcessHandler extends ProcessHandler {

    private static final Logger logger = Logger.getInstance(RapidProcessHandler.class);

    private final @NotNull CompletableFuture<DelegatingNetworkEngine> delegatingNetworkEngine;


    public RapidProcessHandler(@NotNull CompletableFuture<NetworkEngine> completableFuture) {
        this.delegatingNetworkEngine = completableFuture.thenApplyAsync(networkEngine -> new DelegatingNetworkEngine(networkEngine) {
            @Override
            protected void onFailure(@NotNull NetworkCall<?> request, @NotNull Throwable throwable) {
                destroyProcess();
            }

            @Override
            protected void onFailure(@NotNull Throwable throwable) {
                destroyProcess();
            }
        });
        delegatingNetworkEngine.thenComposeAsync(this::start);
    }

    public @NotNull CompletableFuture<Void> start(@NotNull NetworkEngine networkEngine) {
        logger.info("Starting process");
        AtomicBoolean started = new AtomicBoolean();
        ExecutionService executionService = networkEngine.createService(ExecutionService.class);
        return connectOutputStream(networkEngine.createService(EventLogService.class))
                .thenComposeAsync(unused -> executionService.onExecutionState().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
                    logger.debug("State '" + event.getState() + "'");
                    switch (event.getState()) {
                        case RUNNING -> {
                            logger.debug("Execution started");
                            started.set(true);
                        }
                        case STOPPED -> {
                            if (started.get()) {
                                logger.debug("Execution stopped");
                                notifyProcessTerminated(0);
                                networkEngine.closeAsync();
                            }
                        }
                    }
                }))
                .thenComposeAsync(unused -> executionService.resetProgramPointer().sendAsync())
                .thenComposeAsync(unused -> networkEngine.createService(MastershipService.class).getDomain(MastershipType.RAPID).sendAsync())
                .thenComposeAsync(domain -> {
                    logger.debug("Starting execution");
                    NetworkCall<Void> networkCall = executionService.start(RegainMode.REGAIN, ExecutionMode.CONTINUE, ExecutionCycle.ONCE, ConditionState.CALLCHAIN, BreakpointMode.DISABLED, TaskExecutionMode.NORMAL);
                    return CloseableMastership.requestAsync(domain, networkCall::sendAsync);
                });
    }

    private @NotNull CompletableFuture<Void> connectOutputStream(@NotNull EventLogService eventLogService) {
        return eventLogService.getCategories("en").sendAsync()
                .thenComposeAsync(categories -> {
                    if (categories.isEmpty()) {
                        return CompletableFuture.completedFuture(null);
                    }
                    return connectOutputStream(categories.get(0)).thenRunAsync(() -> {});
                });
    }

    private @NotNull CompletableFuture<SubscriptionEntity> connectOutputStream(@NotNull EventLogCategory category) {
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
        delegatingNetworkEngine.thenAcceptAsync(networkEngine -> {
            ExecutionService executionService = networkEngine.createService(ExecutionService.class);
            executionService.stop(StopMode.STOP, TaskExecutionMode.NORMAL).sendAsync()
                    .handleAsync((unused, throwable) -> {
                        if (throwable != null) {
                            notifyProcessDetached();
                            logger.error(throwable);
                        }
                        return null;
                    });
        });
    }

    @Override
    protected void detachProcessImpl() {
        delegatingNetworkEngine.thenAcceptAsync(NetworkEngine::closeAsync);
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

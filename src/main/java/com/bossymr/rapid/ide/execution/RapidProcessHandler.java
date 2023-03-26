package com.bossymr.rapid.ide.execution;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.ResponseStatusException;
import com.bossymr.network.SubscriptionPriority;
import com.bossymr.network.client.DelegatingNetworkEngine;
import com.bossymr.network.client.NetworkEngine;
import com.bossymr.rapid.language.symbol.RapidRobot.Mastership;
import com.bossymr.rapid.robot.network.EventLogService;
import com.bossymr.rapid.robot.network.robotware.rapid.execution.*;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;

import static com.bossymr.rapid.language.symbol.RapidRobot.withMastership;

public class RapidProcessHandler extends ProcessHandler {

    private static final Logger logger = Logger.getInstance(RapidProcessHandler.class);
    private final @NotNull CompletableFuture<NetworkEngine> completableFuture;

    public RapidProcessHandler(@NotNull NetworkEngine engine) {
        this.completableFuture = CompletableFuture.completedFuture(createNetworkEngine(engine))
                .thenComposeAsync(delegate -> subscribe(delegate)
                        .thenApplyAsync(unused -> delegate));
    }

    public RapidProcessHandler(@NotNull CompletableFuture<NetworkEngine> completableFuture) {
        this.completableFuture = completableFuture.thenApplyAsync(this::createNetworkEngine)
                .thenComposeAsync(engine -> subscribe(engine)
                        .thenComposeAsync(unused -> onExecutionState(engine))
                        .thenComposeAsync(unused -> start(engine))
                        .thenApplyAsync(unused -> engine)
                        .exceptionally(throwable -> {
                            logger.error(throwable);
                            notifyProcessTerminated(1);
                            engine.closeAsync();
                            return null;
                        }));
    }

    public @NotNull CompletableFuture<NetworkEngine> getNetworkEngine() {
        return completableFuture;
    }

    private @NotNull CompletableFuture<?> subscribe(@NotNull NetworkEngine engine) {
        return engine.createService(EventLogService.class).getCategories("en").sendAsync()
                .thenComposeAsync(categories -> {
                    if (categories.isEmpty()) {
                        return CompletableFuture.completedFuture(null);
                    }
                    return categories.get(0).onMessage().subscribe(SubscriptionPriority.MEDIUM, (entity, event) ->
                            event.getMessage("en").sendAsync()
                                    .thenAcceptAsync(message -> {
                                        StringJoiner stringJoiner = new StringJoiner("\n");
                                        stringJoiner.add(message.getMessageType() + " " + "[" + message.getTimestamp() + "]: " + message.getMessageTitle());
                                        stringJoiner.add(message.getDescription());
                                        BiConsumer<String, String> append = (name, string) -> {
                                            if (string != null && string.length() > 0) {
                                                stringJoiner.add(name + ": " + string);
                                            }
                                        };
                                        append.accept("Actions", message.getActions());
                                        append.accept("Causes", message.getCauses());
                                        append.accept("Consequences", message.getConsequences());
                                        notifyTextAvailable(stringJoiner + "\n", switch (message.getMessageType()) {
                                            case INFORMATION -> ProcessOutputType.STDOUT;
                                            case ERROR, WARNING -> ProcessOutputType.STDERR;
                                        });
                                    })).thenRunAsync(() -> logger.debug("Subscribed to event log"));
                });
    }

    private @NotNull NetworkEngine createNetworkEngine(@NotNull NetworkEngine engine) {
        return new DelegatingNetworkEngine(engine) {
            @Override
            protected void onFailure(@NotNull NetworkCall<?> request, @NotNull Throwable throwable) {
                onFailure(throwable);
            }

            @Override
            protected void onFailure(@NotNull Throwable throwable) {
                while (throwable instanceof CompletionException) {
                    throwable = throwable.getCause();
                }
                notifyTextAvailable(throwable + "\n", ProcessOutputType.STDERR);
                notifyProcessTerminated(1);
                if (throwable instanceof ResponseStatusException exception && exception.getResponse().statusCode() == 400) {
                    return;
                }
                logger.error(throwable);
            }
        };
    }

    private @NotNull CompletableFuture<?> start(@NotNull NetworkEngine engine) {
        logger.debug("Starting process");
        ExecutionService executionService = engine.createService(ExecutionService.class);
        return executionService.resetProgramPointer().sendAsync()
                .thenComposeAsync(unused -> withMastership(engine, Mastership.RAPID, () ->
                        executionService.start(RegainMode.REGAIN, ExecutionMode.CONTINUE, ExecutionCycle.ONCE, ConditionState.CALLCHAIN, BreakpointMode.DISABLED, TaskExecutionMode.NORMAL).sendAsync()
                )).thenRunAsync(() -> logger.debug("Started process"));
    }

    private @NotNull CompletableFuture<?> onExecutionState(@NotNull NetworkEngine engine) {
        ExecutionService executionService = engine.createService(ExecutionService.class);
        return executionService.onExecutionState().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
            if (event.getState().equals(ExecutionState.STOPPED)) {
                logger.debug("Program stopped");
                notifyProcessTerminated(0);
                engine.closeAsync();
            }
        }).thenRunAsync(() -> logger.debug("Subscribed to execution state"));
    }

    @Override
    protected void destroyProcessImpl() {
        completableFuture.thenComposeAsync(engine -> engine.createService(ExecutionService.class).stop(StopMode.STOP, TaskExecutionMode.NORMAL).sendAsync());
    }

    @Override
    protected void detachProcessImpl() {
        completableFuture.thenComposeAsync(NetworkEngine::closeAsync).thenRunAsync(this::notifyProcessDetached);
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

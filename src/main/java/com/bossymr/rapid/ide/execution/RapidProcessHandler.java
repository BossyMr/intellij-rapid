package com.bossymr.rapid.ide.execution;

import com.bossymr.network.SubscriptionPriority;
import com.bossymr.network.client.NetworkAction;
import com.bossymr.network.client.NetworkManager;
import com.bossymr.rapid.robot.CloseableMastership;
import com.bossymr.rapid.robot.network.EventLogCategory;
import com.bossymr.rapid.robot.network.EventLogMessage;
import com.bossymr.rapid.robot.network.EventLogService;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import com.bossymr.rapid.robot.network.robotware.rapid.execution.*;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;

public class RapidProcessHandler extends ProcessHandler {

    private static final Logger logger = Logger.getInstance(RapidProcessHandler.class);
    private final @NotNull NetworkAction action;

    public RapidProcessHandler(@NotNull NetworkManager manager) throws IOException, InterruptedException {
        this.action = manager.createAction();
        subscribe(action);
        onExecutionState(action);
        start(action);
    }

    public @NotNull NetworkAction getNetworkManager() {
        return action;
    }

    private void subscribe(@NotNull NetworkAction action) throws IOException, InterruptedException {
        logger.debug("Subscribing to process event log");
        EventLogService eventLogService = action.createService(EventLogService.class);
        List<EventLogCategory> categories = eventLogService.getCategories("en").get();
        if (categories.size() == 0) {
            logger.warn("Couldn't find process event log");
            return;
        }
        EventLogCategory category = categories.get(0);
        category.onMessage().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
            logger.debug("Received event '" + event + "'");
            EventLogMessage message;
            try {
                message = event.getMessage("en").get();
                logger.debug("Retrieved message '" + message + "' for event '" + event + "'");
            } catch (IOException e) {
                logger.error(e);
                throw new AssertionError(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
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
        });
    }

    private void onFailure(@NotNull Throwable throwable) {
        while (throwable instanceof CompletionException) {
            throwable = throwable.getCause();
        }
        notifyTextAvailable(throwable + "\n", ProcessOutputType.STDERR);
        notifyProcessTerminated(1);
        try {
            action.close();
        } catch (IOException e) {
            e.addSuppressed(throwable);
            logger.error(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.error(throwable);
    }

    private void start(@NotNull NetworkAction action) throws IOException, InterruptedException {
        logger.debug("Starting process");
        ExecutionService executionService = action.createService(ExecutionService.class);
        executionService.resetProgramPointer().get();
        try (CloseableMastership ignored = CloseableMastership.withMastership(action, MastershipType.RAPID)) {
            executionService.start(RegainMode.REGAIN, ExecutionMode.CONTINUE, ExecutionCycle.ONCE, ConditionState.CALLCHAIN, BreakpointMode.DISABLED, TaskExecutionMode.NORMAL).get();
            logger.debug("Started process");
        }
    }

    private void onExecutionState(@NotNull NetworkAction action) throws IOException, InterruptedException {
        ExecutionService executionService = action.createService(ExecutionService.class);
        executionService.onExecutionState().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
            if (event.getState().equals(ExecutionState.STOPPED)) {
                logger.debug("Program stopped");
                notifyProcessTerminated(0);
                try {
                    this.action.close();
                } catch (IOException e) {
                    logger.error(e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        logger.debug("Subscribed to execution state");
    }

    @Override
    protected void destroyProcessImpl() {
        try {
            action.createService(ExecutionService.class).stop(StopMode.STOP, TaskExecutionMode.NORMAL).get();
        } catch (IOException | InterruptedException ex) {
            onFailure(ex);
        }
    }

    @Override
    protected void detachProcessImpl() {
        notifyProcessDetached();
        try {
            action.close();
        } catch (IOException e) {
            logger.error(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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

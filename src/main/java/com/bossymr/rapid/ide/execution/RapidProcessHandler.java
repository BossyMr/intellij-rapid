package com.bossymr.rapid.ide.execution;

import com.bossymr.rapid.ide.execution.configurations.TaskState;
import com.bossymr.rapid.ide.execution.debugger.RapidDebugProcess;
import com.bossymr.rapid.robot.api.*;
import com.bossymr.rapid.robot.network.EventLogCategory;
import com.bossymr.rapid.robot.network.EventLogMessage;
import com.bossymr.rapid.robot.network.EventLogService;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

public class RapidProcessHandler extends ProcessHandler {

    private static final Logger logger = Logger.getInstance(RapidProcessHandler.class);

    private final @NotNull CompletableFuture<NetworkManager> manager;
    private final @NotNull CompletableFuture<NetworkManager.Group> group;

    private final @NotNull List<TaskState> tasks;

    private final @NotNull ExecutorService executorService;

    public RapidProcessHandler(@NotNull CompletableFuture<NetworkManager> manager, @NotNull List<TaskState> tasks, @NotNull ExecutorService executorService) {
        this.manager = manager;
        this.group = manager.thenApply(NetworkManager::group);
        this.tasks = tasks;
        this.executorService = executorService;
    }

    public @NotNull List<TaskState> getTasks() {
        return tasks;
    }

    public void execute(@NotNull RapidDebugProcess.NetworkRunnable callable) {
        executorService.submit(() -> {
            try {
                callable.compute();
            } catch (Exception e) {
                notifyProcessTerminated(1);
            }
            return null;
        });
    }

    protected void handleException(@NotNull Throwable throwable) throws IOException, InterruptedException {
        notifyProcessTerminated(1);
    }

    public @NotNull NetworkManager getNetworkManager() {
        return manager.join();
    }

    public @NotNull NetworkManager.Group getGroup() {
        return group.join();
    }

    public void setupEventLog() throws IOException, InterruptedException {
        logger.debug("Subscribing to process event log");
        EventLogService eventLogService = getNetworkManager().createService(EventLogService.class);
        List<EventLogCategory> categories = eventLogService.getCategories("en").get();
        if (categories.isEmpty()) {
            logger.warn("Couldn't find process event log");
            return;
        }
        for (int i = 1; i < categories.size(); i++) {
            EventLogCategory category = getNetworkManager().move(categories.get(i));
            try {
                SubscriptionEntity subscription = category.onMessage().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
                    logger.debug("Received event '" + event + "'");
                    EventLogMessage message;
                    try {
                        message = event.getMessage("en").get();
                        logger.debug("Retrieved message '" + message + "' for event '" + event + "'");
                    } catch (IOException e) {
                        return;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    StringJoiner stringJoiner = new StringJoiner("\n");
                    stringJoiner.add(message.getMessageType() + " " + "[" + message.getTimestamp() + "]: " + message.getMessageTitle());
                    stringJoiner.add(message.getDescription());
                    BiConsumer<String, String> append = (name, string) -> {
                        if (string != null && !(string.isEmpty())) {
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
                getGroup().collect(subscription);
            } catch (IOException ignored) {}
        }
    }

    public void start() throws IOException, InterruptedException {
        logger.debug("Starting process");
        ExecutionService executionService = getNetworkManager().createService(ExecutionService.class);
        executionService.resetProgramPointer().get();
        executionService.start(RegainMode.REGAIN, ExecutionMode.CONTINUE, ExecutionCycle.ONCE, ConditionState.CALLCHAIN, BreakpointMode.DISABLED, TaskExecutionMode.NORMAL).get();
        logger.debug("Started process");
    }

    public void setupExecutionState() throws IOException, InterruptedException {
        ExecutionService executionService = getNetworkManager().createService(ExecutionService.class);
        SubscriptionEntity subscription = executionService.onExecutionState().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
            if (event.getState().equals(ExecutionState.STOPPED)) {
                logger.debug("Program stopped");
                notifyProcessTerminated(0);
                try {
                    getGroup().close();
                } catch (IOException ignored) {
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        getGroup().collect(subscription);
        logger.debug("Subscribed to execution state");
    }

    @Override
    protected void destroyProcessImpl() {
        execute(() -> {
            ExecutionService executionService = getNetworkManager().createService(ExecutionService.class);
            ExecutionStatus executionStatus = executionService.getState().get();
            if (executionStatus.getState() == ExecutionState.STOPPED) {
                notifyProcessTerminated(0);
                return;
            }
            SubscriptionEntity subscription = executionService.onExecutionState().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
                if (event.getState() == ExecutionState.STOPPED) {
                    notifyProcessTerminated(0);
                }
            });
            getGroup().collect(subscription);
            executionService.stop(StopMode.STOP, TaskExecutionMode.NORMAL).get();
            Thread.sleep(100);
            ExecutionStatus laterExecutionStatus = executionService.getState().get();
            if (laterExecutionStatus.getState() == ExecutionState.STOPPED) {
                notifyProcessTerminated(0);
            }
        });
    }

    @Override

    protected void detachProcessImpl() {
        notifyProcessDetached();
    }

    @Override
    protected void notifyProcessTerminated(int exitCode) {
        super.notifyProcessTerminated(exitCode);
        try {
            getGroup().close();
            executorService.shutdownNow();
        } catch (IOException ignored) {
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void notifyProcessDetached() {
        super.notifyProcessDetached();
        executorService.shutdownNow();
        try {
            getGroup().close();
        } catch (IOException ignored) {
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

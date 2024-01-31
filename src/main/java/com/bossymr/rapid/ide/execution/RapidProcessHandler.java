package com.bossymr.rapid.ide.execution;

import com.bossymr.network.NetworkAction;
import com.bossymr.network.NetworkManager;
import com.bossymr.network.SubscriptionPriority;
import com.bossymr.network.client.NetworkRequest;
import com.bossymr.rapid.ide.execution.configurations.TaskState;
import com.bossymr.rapid.ide.execution.debugger.RapidDebugProcess;
import com.bossymr.rapid.robot.CloseableMastership;
import com.bossymr.rapid.robot.network.EventLogCategory;
import com.bossymr.rapid.robot.network.EventLogMessage;
import com.bossymr.rapid.robot.network.EventLogService;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import com.bossymr.rapid.robot.network.robotware.rapid.execution.*;
import com.bossymr.rapid.robot.network.robotware.rapid.task.Task;
import com.bossymr.rapid.robot.network.robotware.rapid.task.TaskService;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.BuildLogError;
import com.bossymr.rapid.robot.network.robotware.rapid.task.program.Program;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

public class RapidProcessHandler extends ProcessHandler {

    private static final Logger logger = Logger.getInstance(RapidProcessHandler.class);
    private final @NotNull NetworkManager manager;
    private final @NotNull List<TaskState> tasks;

    private final @NotNull ExecutorService executorService;

    public RapidProcessHandler(@NotNull NetworkManager manager, @NotNull List<TaskState> tasks, @NotNull ExecutorService executorService) {
        this.manager = new NetworkAction(manager) {
            @Override
            protected boolean onFailure(@NotNull NetworkRequest<?> request, @NotNull Throwable throwable) throws IOException, InterruptedException {
                handleException(throwable);
                return false;
            }
        };
        this.tasks = tasks;
        this.executorService = executorService;
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

    public boolean check() throws IOException, InterruptedException, ExecutionException {
        boolean hasError = false;
        TaskService service = manager.createService(TaskService.class);
        for (TaskState taskState : tasks) {
            if(taskState.getName() == null) {
                continue;
            }
            if(!taskState.isEnabled()) {
                continue;
            }
            Task task = service.getTask(taskState.getName()).get();
            Program program = task.getProgram().get();
            List<BuildLogError> events = program.getBuildErrors().get();
            for (BuildLogError event : events) {
                String message = event.getModuleName() + ":" + event.getRow() + ":" + event.getColumn() + ": " + event.getErrorMessage();
                notifyTextAvailable(message, ProcessOutputType.STDERR);
            }
            hasError = hasError || !events.isEmpty();
        }
        return hasError;
    }

    protected void handleException(@NotNull Throwable throwable) throws IOException, InterruptedException {
        notifyProcessTerminated(1);
        manager.close();
    }

    public @NotNull NetworkManager getNetworkManager() {
        return manager;
    }

    public void setup() throws IOException, InterruptedException {
        logger.debug("Subscribing to process event log");
        EventLogService eventLogService = manager.createService(EventLogService.class);
        List<EventLogCategory> categories = eventLogService.getCategories("en").get();
        if (categories.isEmpty()) {
            logger.warn("Couldn't find process event log");
            return;
        }
        for (int i = 1; i < categories.size(); i++) {
            EventLogCategory category = categories.get(i);
            category.onMessage().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
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
        }
    }

    public void start() throws IOException, InterruptedException {
        logger.debug("Starting process");
        ExecutionService executionService = manager.createService(ExecutionService.class);
        try (CloseableMastership ignored = CloseableMastership.withMastership(manager, MastershipType.RAPID)) {
            executionService.resetProgramPointer().get();
            executionService.start(RegainMode.REGAIN, ExecutionMode.CONTINUE, ExecutionCycle.ONCE, ConditionState.CALLCHAIN, BreakpointMode.DISABLED, TaskExecutionMode.NORMAL).get();
            logger.debug("Started process");
        }
    }

    public void onExecutionState() throws IOException, InterruptedException {
        ExecutionService executionService = manager.createService(ExecutionService.class);
        executionService.onExecutionState().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
            if (event.getState().equals(ExecutionState.STOPPED)) {
                logger.debug("Program stopped");
                notifyProcessTerminated(0);
                try {
                    this.manager.close();
                } catch (IOException ignored) {
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        logger.debug("Subscribed to execution state");
    }

    @Override
    protected void destroyProcessImpl() {
        execute(() -> {
            ExecutionService executionService = manager.createService(ExecutionService.class);
            ExecutionStatus executionStatus = executionService.getState().get();
            if (executionStatus.getState() == ExecutionState.STOPPED) {
                manager.close();
                notifyProcessTerminated(0);
                return;
            }
            executionService.onExecutionState().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
                if (event.getState() == ExecutionState.STOPPED) {
                    try {
                        entity.unsubscribe();
                        manager.close();
                        notifyProcessTerminated(0);
                    } catch (IOException ex) {
                        logger.error(ex);
                    } catch (InterruptedException ignored) {}
                }
            });
            executionService.stop(StopMode.STOP, TaskExecutionMode.NORMAL).get();
        });
    }

    @Override
    protected void notifyProcessTerminated(int exitCode) {
        super.notifyProcessTerminated(exitCode);
        executorService.shutdown();
    }

    @Override
    protected void detachProcessImpl() {
        notifyProcessDetached();
        try {
            manager.close();
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

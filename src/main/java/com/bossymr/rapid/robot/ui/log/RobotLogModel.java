package com.bossymr.rapid.robot.ui.log;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotEventListener;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.api.NetworkManager;
import com.bossymr.rapid.robot.api.SubscriptionPriority;
import com.bossymr.rapid.robot.network.EventLogCategory;
import com.bossymr.rapid.robot.network.EventLogMessage;
import com.bossymr.rapid.robot.network.EventLogService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Service
public final class RobotLogModel implements PersistentStateComponent<RobotLogModel.Model> {

    private static final Logger logger = Logger.getInstance(RobotLogModel.class);

    private final List<EventLogMessage> messages = new ArrayList<>();

    private final List<Consumer<Boolean>> onRefresh = new ArrayList<>();

    private final List<Consumer<EventLogMessage>> onMessage = new ArrayList<>();

    private Model model = new Model();

    public RobotLogModel() {
        RobotEventListener.connect(new RobotEventListener() {
            @Override
            public void onRefresh(@NotNull RapidRobot robot, @NotNull NetworkManager manager) {
                createSubscription(manager);
            }

            @Override
            public void onConnect(@NotNull RapidRobot robot, @NotNull NetworkManager manager) {
                messages.clear();
            }
        });
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            RapidRobot robot = RobotService.getInstance().getRobot();
            if (robot != null) {
                NetworkManager manager = robot.getNetworkManager();
                if (manager != null) {
                    createSubscription(manager);
                }
            }
        });
    }

    public static @NotNull RobotLogModel getInstance() {
        return ApplicationManager.getApplication().getService(RobotLogModel.class);
    }

    public void onMessage(@NotNull Consumer<EventLogMessage> consumer) {
        onMessage.add(consumer);
    }

    public void onRefresh(@NotNull Consumer<Boolean> consumer) {
        onRefresh.add(consumer);
    }

    private void requireRefresh(boolean requireReload) {
        for (Consumer<Boolean> consumer : onRefresh) {
            consumer.accept(requireReload);
        }
    }

    public @NotNull List<EventLogMessage> getMessages() {
        return messages.stream()
                .filter(this::showMessage)
                .toList();
    }

    public boolean isAutoScroll() {
        return model.autoScroll;
    }

    private void createSubscription(@NotNull NetworkManager manager) {
        try {
            EventLogService service = manager.createService(EventLogService.class);
            List<EventLogCategory> categories = service.getCategories("en").get();
            if (categories.isEmpty()) {
                logger.warn("Connected robot has no event log");
                return;
            }
            EventLogCategory category = categories.get(0);
            category.onMessage().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
                try {
                    EventLogMessage message = event.getMessage("en").get();
                    messages.add(message);
                    if (showMessage(message)) {
                        for (Consumer<EventLogMessage> consumer : onMessage) {
                            consumer.accept(message);
                        }
                    }
                } catch (IOException e) {
                    logger.warn("Could not retrieve event", e);
                } catch (InterruptedException e) {
                    throw new ProcessCanceledException();
                }
            });
        } catch (IOException e) {
            logger.warn("Could not subscribe to event log", e);
        } catch (InterruptedException e) {
            throw new ProcessCanceledException();
        }
    }

    private boolean showMessage(@NotNull EventLogMessage message) {
        EventType eventType = switch(message.getMessageType()) {
            case INFORMATION -> EventType.INFORMATION;
            case WARNING -> EventType.WARNING;
            case ERROR -> EventType.ERROR;
        };
        return model.events.contains(eventType);
    }

    @Override
    public @NotNull RobotLogModel.Model getState() {
        return model;
    }

    @Override
    public void loadState(@NotNull Model model) {
        this.model = model;
    }

    public enum EventType {
        ERROR(AllIcons.General.Error, RapidBundle.message("robot.tool.window.tab.log.filter.error")),
        WARNING(AllIcons.General.Warning, RapidBundle.message("robot.tool.window.tab.log.filter.warning")),
        INFORMATION(AllIcons.General.Information, RapidBundle.message("robot.tool.window.tab.log.filter.information"));

        public final Icon icon;
        public final String title;

        EventType(@NotNull Icon icon, @NotNull String title) {
            this.icon = icon;
            this.title = title;
        }
    }

    public static class Model {
        public @NotNull Set<EventType> events = EnumSet.allOf(EventType.class);

        public boolean autoScroll = false;
    }

    public final class FilterTypeAction extends ToggleAction {

        private final EventType eventType;

        public FilterTypeAction(@NotNull EventType eventType) {
            super(() -> eventType.title, eventType.icon);
            this.eventType = eventType;
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }

        @Override
        public boolean isSelected(@NotNull AnActionEvent e) {
            return model.events.contains(eventType);
        }

        @Override
        public void setSelected(@NotNull AnActionEvent e, boolean state) {
            if (state) {
                model.events.add(eventType);
            } else {
                model.events.remove(eventType);
            }
            requireRefresh(true);
        }
    }

    public final class AutoScrollAction extends ToggleAction {

        public AutoScrollAction() {
            super(RapidBundle.messagePointer("robot.tool.window.tab.log.autoscroll"), AllIcons.General.AutoscrollFromSource);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }

        @Override
        public boolean isSelected(@NotNull AnActionEvent e) {
            return model.autoScroll;
        }

        @Override
        public void setSelected(@NotNull AnActionEvent e, boolean state) {
            model.autoScroll = state;
            requireRefresh(false);
        }
    }

    public final class ClearLogAction extends AnAction {

        public ClearLogAction() {
            super(RapidBundle.messagePointer("robot.tool.window.tab.log.clear"), AllIcons.Actions.GC);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            messages.clear();
            requireRefresh(true);
        }
    }
}

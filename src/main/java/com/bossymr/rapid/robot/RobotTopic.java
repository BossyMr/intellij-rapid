package com.bossymr.rapid.robot;

import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

public interface RobotTopic {

    Topic<RobotTopic> ROBOT_TOPIC = Topic.create("Robot", RobotTopic.class);

    default void onConnect(@NotNull Robot robot) {}

    default void onDisconnect(@NotNull Robot robot) {}

    default void onDisconnect() {}

    default void onRefresh(@NotNull Robot robot) {}
}

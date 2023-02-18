package com.bossymr.rapid.ide.execution.configurations;

import com.intellij.execution.configurations.ModuleBasedConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;
import org.jetbrains.annotations.Nullable;

public class RapidRunConfigurationOptions extends ModuleBasedConfigurationOptions {

    /**
     * The name of the task on the robot to execute.
     */
    private final StoredProperty<String> taskName = string("").provideDelegate(this, "taskName");

    /**
     * The path to the robot.
     */
    private final StoredProperty<String> robotPath = string("").provideDelegate(this, "robotPath");

    public @Nullable String getTaskName() {
        return taskName.getValue(this);
    }

    public void setTaskName(@Nullable String taskName) {
        this.taskName.setValue(this, taskName);
    }

    public @Nullable String getRobotPath() {
        return robotPath.getValue(this);
    }

    public void setRobotPath(@Nullable String robotPath) {
        this.robotPath.setValue(this, robotPath);
    }
}

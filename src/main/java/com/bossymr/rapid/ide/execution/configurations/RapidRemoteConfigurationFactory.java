package com.bossymr.rapid.ide.execution.configurations;

import com.bossymr.rapid.RapidBundle;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code ConfigurationFactory} used to create run configurations to execute a remote task.
 */
public class RapidRemoteConfigurationFactory extends ConfigurationFactory {

    public RapidRemoteConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull @NonNls String getId() {
        return "RapidRemoteConfigurationType";
    }

    @Override
    public @NotNull @Nls String getName() {
        return RapidBundle.message("run.configuration.factory.remote");
    }

    @Override
    public boolean isEditableInDumbMode() {
        return true;
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return null;
    }
}

package com.bossymr.rapid.ide.execution.configurations;

import com.bossymr.rapid.RapidBundle;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code ConfigurationFactory} used to create run configurations to execute a local module.
 */
public class RapidConfigurationFactory extends ConfigurationFactory {

    public RapidConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull @NonNls String getId() {
        return "RapidLocalConfigurationType";
    }

    @Override
    public @NotNull @Nls String getName() {
        return RapidBundle.message("run.configuration.factory.local");
    }

    @Override
    public boolean isEditableInDumbMode() {
        return true;
    }

    @Override
    public @Nullable Class<? extends BaseState> getOptionsClass() {
        return RapidRunConfigurationOptions.class;
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new RapidRunConfiguration("", project, this);
    }
}

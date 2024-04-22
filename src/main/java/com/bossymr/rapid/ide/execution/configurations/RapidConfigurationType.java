package com.bossymr.rapid.ide.execution.configurations;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.RapidIcons;
import com.intellij.execution.configurations.*;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code ConfigurationType} for the {@code Rapid} custom language.
 * <p>
 * This {@code ConfigurationType} adds support for executing both local and remote projects.
 */
public class RapidConfigurationType extends SimpleConfigurationType {

    public static @NotNull RapidConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(RapidConfigurationType.class);
    }

    public RapidConfigurationType() {
        super("RapidConfigurationType", RapidBundle.message("run.configuration.type.display.name"), null, NotNullLazyValue.createConstantValue(RapidIcons.ROBOT));
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

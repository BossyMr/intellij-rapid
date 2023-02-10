package com.bossymr.rapid.ide.execution.configurations;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.RapidIcons;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * A {@code ConfigurationType} for the {@code Rapid} custom language.
 * <p>
 * This {@code ConfigurationType} adds support for executing both local and remote projects.
 */
public class RapidConfigurationType implements ConfigurationType {

    private final ConfigurationFactory[] CONFIGURATION_FACTORIES = {
            new RapidLocalConfigurationFactory(this),
            new RapidRemoteConfigurationFactory(this)
    };

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return RapidBundle.message("run.configuration.type.display.name");
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) String getConfigurationTypeDescription() {
        return getDisplayName();
    }

    @Override
    public @NotNull Icon getIcon() {
        return RapidIcons.ROBOT_ICON;
    }

    @Override
    public @NotNull @NonNls String getId() {
        return "RapidRunConfigurationType";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return CONFIGURATION_FACTORIES;
    }
}

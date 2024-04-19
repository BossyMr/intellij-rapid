package com.bossymr.rapid.ide.execution.configurations.ui;

import com.intellij.execution.configuration.RunConfigurationExtensionBase;
import com.intellij.execution.configuration.RunConfigurationExtensionsManager;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.extensions.ExtensionPointName;
import org.jetbrains.annotations.NotNull;

@Service
public final class RapidRunConfigurationExtensionManager extends RunConfigurationExtensionsManager<RunConfigurationBase<?>, RunConfigurationExtensionBase<RunConfigurationBase<?>>> {

    public static final ExtensionPointName<RunConfigurationExtensionBase<RunConfigurationBase<?>>> EP_NAME = new ExtensionPointName<>("com.bossymr.rapid.runConfigurationExtension");

    public RapidRunConfigurationExtensionManager() {
        super(EP_NAME);
    }

    public static @NotNull RapidRunConfigurationExtensionManager getInstance() {
        return ApplicationManager.getApplication().getService(RapidRunConfigurationExtensionManager.class);
    }
}

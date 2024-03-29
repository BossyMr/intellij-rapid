package com.bossymr.rapid.ide.execution.configurations;

import com.bossymr.rapid.ide.execution.RapidRunProfileState;
import com.bossymr.rapid.ide.execution.configurations.ui.RapidRunConfigurationEditor;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

public class RapidRunConfiguration extends LocatableConfigurationBase<Element> {

    public RapidRunConfiguration(@Nullable String name, @NotNull Project project, @NotNull ConfigurationFactory factory) {
        super(project, factory, name);
    }

    @Override
    public @NotNull RapidRunConfigurationOptions getOptions() {
        return (RapidRunConfigurationOptions) super.getOptions();
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new RapidRunConfigurationEditor(getProject());
    }

    @Override
    public @Nullable RapidRunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
        return RapidRunProfileState.create(getProject(), getOptions());
    }
}

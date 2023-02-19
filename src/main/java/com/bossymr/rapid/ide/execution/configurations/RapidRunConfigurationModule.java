package com.bossymr.rapid.ide.execution.configurations;

import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class RapidRunConfigurationModule extends RunConfigurationModule {

    public RapidRunConfigurationModule(@NotNull Project project) {
        super(project);
    }
}

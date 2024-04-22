package com.bossymr.rapid.ide.execution.configurations.ui;

import com.bossymr.rapid.ide.execution.configurations.RapidRunConfiguration;
import com.intellij.execution.ui.BeforeRunFragment;
import com.intellij.execution.ui.RunConfigurationFragmentedEditor;
import com.intellij.execution.ui.SettingsEditorFragment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RapidRunConfigurationSettingsEditor extends RunConfigurationFragmentedEditor<RapidRunConfiguration> {

    public RapidRunConfigurationSettingsEditor(@NotNull RapidRunConfiguration runConfiguration) {
        super(runConfiguration, RapidRunConfigurationExtensionManager.getInstance());
    }

    @Override
    protected List<SettingsEditorFragment<RapidRunConfiguration, ?>> createRunFragments() {
        List<SettingsEditorFragment<RapidRunConfiguration, ?>> fragments = new ArrayList<>(BeforeRunFragment.createGroup());
        fragments.add(new RapidRobotFragment(getProject()));
        return fragments;
    }
}

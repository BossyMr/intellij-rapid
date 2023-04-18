package com.bossymr.rapid.ide.debugger.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.xdebugger.settings.DebuggerSettingsCategory;
import com.intellij.xdebugger.settings.XDebuggerSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class RapidDebuggerSettings extends XDebuggerSettings<RapidDebuggerSettings> {

    public RapidDebuggerSettings() {
        super("Rapid");
    }

    @Override
    public @NotNull RapidDebuggerSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull RapidDebuggerSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Override
    public @NotNull Collection<? extends Configurable> createConfigurables(@NotNull DebuggerSettingsCategory category) {
        return switch (category) {
            case ROOT -> createRootConfigurable();
            case GENERAL -> createGeneralConfigurable();
            case DATA_VIEWS -> createDataViewConfigurable();
            case STEPPING -> createSteppingConfigurable();
            case HOTSWAP -> createHotswapConfigurable();
        };
    }

    private @NotNull Collection<? extends Configurable> createRootConfigurable() {
        return Collections.emptyList();
    }

    private @NotNull Collection<? extends Configurable> createGeneralConfigurable() {
        return Collections.emptyList();
    }

    private @NotNull Collection<? extends Configurable> createDataViewConfigurable() {
        return Collections.emptyList();
    }

    private @NotNull Collection<? extends Configurable> createSteppingConfigurable() {
        return Collections.emptyList();
    }

    private @NotNull Collection<? extends Configurable> createHotswapConfigurable() {
        return Collections.emptyList();
    }

}
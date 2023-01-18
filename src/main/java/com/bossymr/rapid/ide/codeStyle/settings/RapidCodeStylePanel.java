package com.bossymr.rapid.ide.codeStyle.settings;

import com.bossymr.rapid.language.RapidLanguage;
import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public class RapidCodeStylePanel extends TabbedLanguageCodeStylePanel {

    public RapidCodeStylePanel(CodeStyleSettings currentSettings, @NotNull CodeStyleSettings settings) {
        super(RapidLanguage.INSTANCE, currentSettings, settings);
    }
}

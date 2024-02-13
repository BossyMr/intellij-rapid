package com.bossymr.rapid.ide.editor.formatting.settings;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.RapidLanguage;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RapidIndentOptionsEditor extends SmartIndentOptionsEditor {

    private JCheckBox dontIndentTopLevel;

    @Override
    protected void addComponents() {
        super.addComponents();
        dontIndentTopLevel = new JBCheckBox(RapidBundle.message("checkbox.indent.top.level"));
        add(dontIndentTopLevel);
    }

    @Override
    public boolean isModified(CodeStyleSettings settings, CommonCodeStyleSettings.IndentOptions options) {
        boolean modified = super.isModified(settings, options);
        CommonCodeStyleSettings common = settings.getCommonSettings(RapidLanguage.INSTANCE);
        modified |= isFieldModified(dontIndentTopLevel, common.DO_NOT_INDENT_TOP_LEVEL_CLASS_MEMBERS);
        return modified;
    }

    @Override
    public void apply(CodeStyleSettings settings, CommonCodeStyleSettings.IndentOptions options) {
        super.apply(settings, options);
        CommonCodeStyleSettings common = settings.getCommonSettings(RapidLanguage.INSTANCE);
        common.DO_NOT_INDENT_TOP_LEVEL_CLASS_MEMBERS = dontIndentTopLevel.isSelected();
    }

    @Override
    public void reset(@NotNull CodeStyleSettings settings, CommonCodeStyleSettings.@NotNull IndentOptions options) {
        super.reset(settings, options);
        CommonCodeStyleSettings common = settings.getCommonSettings(RapidLanguage.INSTANCE);
        dontIndentTopLevel.setSelected(common.DO_NOT_INDENT_TOP_LEVEL_CLASS_MEMBERS);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        dontIndentTopLevel.setEnabled(enabled);
    }
}

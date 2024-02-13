package com.bossymr.rapid.ide.editor.formatting;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

import static com.intellij.psi.codeStyle.CommonCodeStyleSettings.DO_NOT_WRAP;

public class RapidCodeStyleSettings extends CustomCodeStyleSettings {


    public int BLANK_LINES_AROUND_STRUCTURE = 1;
    public int BLANK_LINES_AROUND_COMPONENT = 1;
    public int BLANK_LINES_AROUND_FIELD = 1;
    public int BLANK_LINES_AROUND_ROUTINE = 1;

    public int ATTRIBUTE_LIST_WRAP = DO_NOT_WRAP;
    public boolean ALIGN_MULTILINE_ATTRIBUTE_LIST = false;

    public boolean SPACE_BEFORE_ATTRIBUTE_LIST = true;

    public boolean INDENT_ROUTINE_STATEMENT_LIST = true;
    public boolean INDENT_CASE_FROM_TEST_STATEMENT = true;

    protected RapidCodeStyleSettings(@NotNull CodeStyleSettings container) {
        super(RapidCodeStyleSettings.class.getSimpleName(), container);
    }
}

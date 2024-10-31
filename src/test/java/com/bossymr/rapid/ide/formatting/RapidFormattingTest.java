package com.bossymr.rapid.ide.formatting;

import com.bossymr.rapid.RapidTestCase;
import com.bossymr.rapid.ide.editor.formatting.RapidCodeStyleSettings;
import com.bossymr.rapid.language.RapidFileType;
import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiConsumer;

class RapidFormattingTest extends RapidTestCase {

    private void doTest(@NotNull String input, @NotNull String expected, @NotNull BiConsumer<CommonCodeStyleSettings, RapidCodeStyleSettings> settings) {
        getFixture().configureByText(RapidFileType.getInstance(), input);
        CommonCodeStyleSettings languageSettings = CodeStyle.getLanguageSettings(getFixture().getFile());
        RapidCodeStyleSettings customSettings = CodeStyle.getCustomSettings(getFixture().getFile(), RapidCodeStyleSettings.class);
        settings.accept(languageSettings, customSettings);
        WriteCommandAction.writeCommandAction(getFixture().getProject()).run(() -> {
            CodeStyleManager.getInstance(getFixture().getProject()).reformatText(getFixture().getFile(), List.of(getFixture().getFile().getTextRange()));
        });
        getFixture().checkResult(expected);
    }

    @Test
    void formatModule() {
        doTest("""
                ! COMMENT
                MODULE name
                ! COMMENT
                RECORD record1
                name name1;
                ENDRECORD
                ALIAS type1 name1;
                FUNC name1 name2()
                ! COMMENT
                CONNECT variable1 with trap1;
                ERROR
                TRYNEXT;
                ENDFUNC
                ENDMODULE
                """, """
                ! COMMENT
                MODULE name
                
                
                    ! COMMENT
                    RECORD record1
                
                        name name1;
                
                    ENDRECORD
                
                
                    ALIAS type1 name1;
                
                
                    FUNC name1 name2()
                        ! COMMENT
                        CONNECT variable1 with trap1;
                        ERROR
                            TRYNEXT;
                    ENDFUNC
                
                ENDMODULE
                """, (commonSettings, customSettings) -> {
            customSettings.BLANK_LINES_AROUND_COMPONENT = 1;
            customSettings.BLANK_LINES_AROUND_STRUCTURE = 2;
            customSettings.BLANK_LINES_AROUND_ROUTINE = 1;
            customSettings.INDENT_ROUTINE_STATEMENT_LIST = true;
        });
    }
}

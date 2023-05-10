package com.bossymr.rapid.ide.formatting;

import com.bossymr.rapid.ide.codeStyle.RapidCodeStyleSettings;
import com.bossymr.rapid.language.RapidFileType;
import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;

public class RapidFormattingTest extends BasePlatformTestCase {

    private void doTest(@NotNull String input, @NotNull String expected, @NotNull BiConsumer<CommonCodeStyleSettings, RapidCodeStyleSettings> settings) {
        myFixture.configureByText(RapidFileType.getInstance(), input);
        CommonCodeStyleSettings languageSettings = CodeStyle.getLanguageSettings(myFixture.getFile());
        RapidCodeStyleSettings customSettings = CodeStyle.getCustomSettings(myFixture.getFile(), RapidCodeStyleSettings.class);
        settings.accept(languageSettings, customSettings);
        WriteCommandAction.writeCommandAction(getProject()).run(() -> {
            CodeStyleManager.getInstance(getProject()).reformatText(myFixture.getFile(), List.of(myFixture.getFile().getTextRange()));
        });
        myFixture.checkResult(expected);
    }

    public void testFormatModule() {
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

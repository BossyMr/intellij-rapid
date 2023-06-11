package com.bossymr.rapid.ide.completion;

import com.bossymr.rapid.language.RapidFileType;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RapidCompletionTest extends BasePlatformTestCase {

    private void doTest(@NotNull String text, @NotNull String... expected) {
        myFixture.configureByText(RapidFileType.getInstance(), text);
        myFixture.complete(CompletionType.BASIC);
        List<String> strings = myFixture.getLookupElementStrings();
        assertNotNull(strings);
        assertSameElements(strings, expected);
    }

    public void testAliasType() {
        // An alias cannot be defined upon another alias type.
        doTest("""
                MODULE name
                    RECORD record1 ENDRECORD
                    RECORD record2 ENDRECORD
                    ALIAS type1 name1;
                    ALIAS type2 name2;
                    ALIAS <caret>
                ENDMODULE""", "record1", "record2");
    }

    public void testComponentType() {
        doTest("""
                MODULE name
                    RECORD record1
                        <caret>
                    ENDRECORD
                    RECORD record2 ENDRECORD
                    ALIAS type1 name1;
                    ALIAS type2 name2;
                ENDMODULE""", "record1", "record2", "name1", "name2");
    }

    public void testRoutineType() {
        doTest("""
                MODULE name
                    RECORD record1 ENDRECORD
                    RECORD record2 ENDRECORD
                    ALIAS type1 name1;
                    ALIAS type2 name2;
                    FUNC <caret>
                ENDMODULE""", "record1", "record2", "name1", "name2");
    }

    public void testName() {
        doTest("""
                MODULE <caret>
                ENDMODULE
                """);
    }

    public void testModuleKeyword() {
        doTest("", "MODULE");
    }

    public void testAfterModuleKeyword() {
        doTest("""
                MODULE name ENDMODULE
                <caret>
                """);
    }

    public void testModuleAttribute() {
        doTest("""
                MODULE name(<caret>)
                ENDMODULE""", "SYSMODULE", "NOVIEW", "NOSTEPIN", "VIEWONLY", "READONLY");
    }

    public void testSymbolKeyword() {
        doTest("""
                MODULE name
                    <caret>
                ENDMODULE""", "LOCAL", "TASK", "VAR", "PERS", "CONST", "FUNC", "PROC", "TRAP", "ALIAS", "RECORD");
    }

    public void testLocalSymbolKeyword() {
        doTest("""
                MODULE name
                    LOCAL <caret>
                ENDMODULE""", "VAR", "PERS", "CONST", "RECORD, ALIAS", "FUNC", "PROC", "TRAP");
    }

    public void testTaskSymbolKeyword() {
        doTest("""
                MODULE name
                    TASK <caret>
                ENDMODULE""", "VAR", "PERS");
    }

    public void testParameter() {
        doTest("""
                MODULE name
                    ALIAS type1 name1;
                    PROC name(<caret>
                ENDMODULE
                """, "name1", "VAR", "PERS", "INOUT");
    }

    public void testStatement() {
        doTest("""
                MODULE name
                    PROC name(type1 name1)
                        <caret>
                    ENDPROC
                ENDMODULE
                """, "name", "name1", "RETURN", "BACKWARD", "CONNECT", "CONST", "ERROR", "EXIT", "FOR", "GOTO", "IF", "PERS", "RAISE", "TEST", "UNDO", "VAR", "WHILE");
    }
}
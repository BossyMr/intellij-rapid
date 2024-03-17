package com.bossymr.rapid.ide.completion;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class RapidCompletionTest extends BasePlatformTestCase {

    private void doTest(@NotNull String text, @NotNull String... expected) {
        try {
            RobotService.getInstance().disconnect();
        } catch (IOException | InterruptedException e) {
            fail();
        }
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
                ENDMODULE""", "ANYTYPE#", "bool", "dnum", "num", "orient", "pos", "pose", "record1", "record2", "string");
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
                ENDMODULE""", "ANYTYPE#", "bool", "dnum", "num", "orient", "pos", "pose", "record1", "record2", "name1", "name2", "string");
    }

    public void testRoutineType() {
        doTest("""
                MODULE name
                    RECORD record1 ENDRECORD
                    RECORD record2 ENDRECORD
                    ALIAS type1 name1;
                    ALIAS type2 name2;
                    FUNC <caret>
                ENDMODULE""", "ANYTYPE#", "bool", "dnum", "num", "orient", "pos", "pose", "record1", "record2", "name1", "name2", "string");
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
                """, "ANYTYPE#", "bool", "dnum", "num", "orient", "pos", "pose", "name1", "VAR", "PERS", "INOUT", "string");
    }

    public void testStatement() {
        doTest("""
                MODULE name
                    PROC name(type1 name1)
                        <caret>
                    ENDPROC
                ENDMODULE
                """, "BACKWARD", "CONNECT", "CONST", "Cos", "Dim", "ERROR", "EXIT", "FOR", "GOTO", "IF", "name", "name1", "PERS", "Present", "RAISE", "RETURN", "TEST", "UNDO", "VAR", "WHILE");
    }
}

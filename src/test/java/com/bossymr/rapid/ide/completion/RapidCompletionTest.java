package com.bossymr.rapid.ide.completion;

import com.bossymr.rapid.RapidTestCase;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.codeInsight.completion.CompletionType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

class RapidCompletionTest extends RapidTestCase {

    private void doTest(@NotNull String text, @NotNull String... expected) {
        try {
            // In case we are currently connected to a robot, try to disconnect.
            // Otherwise, we might get completion alternatives for symbols on the robot.
            RobotService.getInstance().disconnect();
        } catch (IOException | InterruptedException e) {
            Assertions.fail();
        }
        getFixture().configureByText(RapidFileType.getInstance(), text);
        getFixture().complete(CompletionType.BASIC);
        List<String> strings = getFixture().getLookupElementStrings();
        Assertions.assertNotNull(strings);
        Assertions.assertEquals(expected.length, strings.size());
        Assertions.assertTrue(strings.containsAll(List.of(expected)));
    }

    @Test
    void aliasTypeTest() {
        // `name1` and `name2` are not suggested, because an alias cannot be defined upon another alias type.
        doTest("""
                MODULE name
                    RECORD record1 ENDRECORD
                    RECORD record2 ENDRECORD
                    ALIAS type1 name1;
                    ALIAS type2 name2;
                    ALIAS <caret>
                ENDMODULE""", "ANYTYPE#", "bool", "dnum", "num", "orient", "pos", "pose", "record1", "record2", "string");
    }

    @Test
    void componentTypeTest() {
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


    @Test
    void routineTypeTest() {
        doTest("""
                MODULE name
                    RECORD record1 ENDRECORD
                    RECORD record2 ENDRECORD
                    ALIAS type1 name1;
                    ALIAS type2 name2;
                    FUNC <caret>
                ENDMODULE""", "ANYTYPE#", "bool", "dnum", "num", "orient", "pos", "pose", "record1", "record2", "name1", "name2", "string");
    }

    @Test
    void nameTest() {
        doTest("""
                MODULE <caret>
                ENDMODULE
                """);
    }

    @Test
    void moduleKeywordTest() {
        doTest("", "MODULE");
    }

    @Test
    void afterModuleKeywordTest() {
        doTest("""
                MODULE name ENDMODULE
                <caret>
                """);
    }

    @Test
    void moduleAttributeTest() {
        doTest("""
                MODULE name(<caret>)
                ENDMODULE""", "SYSMODULE", "NOVIEW", "NOSTEPIN", "VIEWONLY", "READONLY");
    }

    @Test
    void symbolKeywordTest() {
        doTest("""
                MODULE name
                    <caret>
                ENDMODULE""", "LOCAL", "TASK", "VAR", "PERS", "CONST", "FUNC", "PROC", "TRAP", "ALIAS", "RECORD");
    }

    @Test
    void localSymbolKeywordTest() {
        doTest("""
                MODULE name
                    LOCAL <caret>
                ENDMODULE""", "VAR", "PERS", "CONST", "RECORD, ALIAS", "FUNC", "PROC", "TRAP");
    }

    @Test
    void taskSymbolKeywordTest() {
        doTest("""
                MODULE name
                    TASK <caret>
                ENDMODULE""", "VAR", "PERS");
    }

    @Test
    void parameterTest() {
        doTest("""
                MODULE name
                    ALIAS type1 name1;
                    PROC name(<caret>
                ENDMODULE
                """, "ANYTYPE#", "bool", "dnum", "num", "orient", "pos", "pose", "name1", "VAR", "PERS", "INOUT", "string");
    }

    @Test
    void statementTest() {
        doTest("""
                MODULE name
                    PROC name(type1 name1)
                        <caret>
                    ENDPROC
                ENDMODULE
                """, "BACKWARD", "CONNECT", "CONST", "Cos", "Dim", "ERROR", "EXIT", "FOR", "GOTO", "IF", "name", "name1", "PERS", "Present", "RAISE", "RETURN", "TEST", "UNDO", "VAR", "WHILE");
    }
}

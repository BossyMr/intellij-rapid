package com.bossymr.rapid.ide.insight.flow;

import com.bossymr.rapid.ide.editor.insight.inspection.flow.UnreachableStatementInspection;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.ControlFlowService;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UnreachableStatementInspectionTest extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(List.of(UnreachableStatementInspection.class));
        ControlFlowService.getInstance().reload();
    }

    private void doTest(@NotNull String text) {
        myFixture.configureByText(RapidFileType.getInstance(), text);
        myFixture.checkHighlighting(true, true, true, true);
    }

    public void testSingleStatement() {
        doTest("""
                MODULE foo
                    PROC bar()
                        VAR value := 0;
                        IF FALSE THEN
                            <warning descr="Unreachable code">value := -1;</warning>
                        ENDIF
                    ENDPROC
                ENDMODULE
                """);
    }

    public void testGroup() {
        doTest("""
                MODULE foo
                    PROC bar()
                        VAR value := 0;
                        IF FALSE THEN
                            <warning descr="Unreachable code">value := -1;
                            value := -3;</warning>
                        ENDIF
                    ENDPROC
                ENDMODULE
                """);
    }

    public void testGroupWithJump() {
        doTest("""
                MODULE foo
                    PROC bar()
                        VAR value := 0;
                        label:
                        IF FALSE THEN
                            <warning descr="Unreachable code">value := -1;
                            GOTO label;
                            value := -3;</warning>
                        ENDIF
                    ENDPROC
                ENDMODULE
                """);
    }

    public void testSplitGroup() {
        doTest("""
                MODULE foo
                    PROC bar()
                        VAR value := 0;
                        IF FALSE THEN
                            <warning descr="Unreachable code">value := -1;
                            value := -2;</warning>
                            ! ...
                            <warning descr="Unreachable code">value := -3;
                            value := -4;</warning>
                        ENDIF
                    ENDPROC
                ENDMODULE
                """);
    }

    public void testSplitGroup2() {
        doTest("""
                MODULE foo
                    PROC bar()
                        VAR value := 0;
                        IF FALSE THEN
                            <warning descr="Unreachable code">value := -1;
                            value := -2;</warning>
                            ! ...
                            <warning descr="Unreachable code">value := -3;
                            value := -4;</warning>
                        ENDIF
                        FOR i FROM 1 TO 0 STEP 1 DO
                            <warning descr="Unreachable code">value := -3;
                            value := -4;</warning>
                        ENDFOR
                        WHILE FALSE DO
                            <warning descr="Unreachable code">value := -3;
                            value := -4;</warning>
                        ENDWHILE
                    ENDPROC
                ENDMODULE
                """);
    }

    public void testChild() {
        doTest("""
                MODULE foo
                    PROC bar()
                        VAR value := 0;
                        IF FALSE THEN
                            <warning descr="Unreachable code">IF FALSE THEN
                                value := -1;
                            ENDIF</warning>
                        ENDIF
                    ENDPROC
                ENDMODULE
                """);
    }
}

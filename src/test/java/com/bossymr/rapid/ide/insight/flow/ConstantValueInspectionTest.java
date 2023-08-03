package com.bossymr.rapid.ide.insight.flow;

import com.bossymr.rapid.ide.editor.insight.inspection.flow.ConstantValueInspection;
import com.bossymr.rapid.language.RapidFileType;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConstantValueInspectionTest extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(List.of(ConstantValueInspection.class));
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    private void doTest(@NotNull String text) {
        myFixture.configureByText(RapidFileType.getInstance(), text);
        myFixture.checkHighlighting(true, true, true, true);
    }

    public void testSimple() {
        doTest("""
                MODULE foo
                    PROC bar()
                        VAR num variable := <warning descr="Value of expression is always 8">2 * 4</warning>;
                    ENDPROC
                ENDMODULE
                """);
    }

    public void testFunctionCall() {
        doTest("""
                MODULE foo
                    PROC bar()
                        VAR num variable := 0;
                        variable := Abs(-1);
                        IF <warning descr="Value of expression is always true">variable = 1</warning> THEN
                        ENDIF
                    ENDPROC
                    
                    FUNC num Abs(num value)
                        IF value >= 0 THEN
                            return value;
                        ELSE
                            return -value;
                        ENDIF
                    ENDFUNC
                ENDMODULE
                """);
    }

    public void testGroupExpression() {
        doTest("""
                MODULE foo
                    PROC bar(bool condition)
                        VAR num a := 0;
                        VAR num b := 0;
                        VAR num c := 0;
                        IF condition THEN
                            a := 1;
                            b := 2;
                            c := 3;
                        ELSE
                            a := -1;
                            b := -2;
                            c := -3;
                        ENDIF
                        IF a < 0 THEN
                            IF <warning descr="Value of expression is always true">b = -2</warning> THEN
                            ENDIF
                        ENDIF
                    ENDPROC
                ENDMODULE
                """);
    }

    public void testVariableAssignment() {
        doTest("""
                MODULE foo
                    PROC bar()
                        VAR num variable := 2;
                        variable := <warning descr="Value of expression is always 8">2 * 4</warning>;
                    ENDPROC
                ENDMODULE
                """);
    }

    public void testConditionJump() {
        doTest("""
                MODULE foo
                    PROC bar(num x)
                        VAR num variable := 2;
                        IF x = 2 THEN
                            variable := <warning descr="Value of expression is always 4">variable * 2</warning>;
                            IF <warning descr="Value of expression is always true">x > 0</warning> THEN
                            ENDIF
                        ENDIF
                    ENDPROC
                ENDMODULE
                """);
    }

    public void testArrayVariable() {
        doTest("""
                MODULE foo
                    PROC bar(num x)
                        VAR num variable{2, 3} := [[0, 1, 2], [3, 4, 5]];
                        variable{1, x} := <warning descr="Value of expression is always 8">variable{1, 3} * variable{2, 2}</warning>;
                    ENDPROC
                ENDMODULE
                """);
    }

    public void testMissingVariable() {
        doTest("""
                MODULE foo
                    PROC bar(\\num x)
                        VAR num y := 5;
                        VAR num z := 0;
                        IF Present(x) THEN
                            y := y + x;
                        ELSE
                            y := y - <warning descr="Variable 'x' is always missing">x</warning>;
                            z := x;
                        ENDIF
                    ENDPROC
                ENDMODULE
                """);
    }

    public void testMutuallyExlusiveArgument() {
        doTest("""
                MODULE foo
                    PROC bar(\\num x | num y)
                        VAR num z := 0;
                        IF Present(x) THEN
                            z := <warning descr="Variable 'y' is always missing">y</warning> + x;
                        ELSE
                            z := <warning descr="Variable 'y' might be missing">y</warning> - <warning descr="Variable 'x' is always missing">x</warning>;
                        ENDIF
                    ENDPROC
                ENDMODULE
                """);
    }

    public void testLargeMutuallyExlusiveArguments() {
        doTest("""
                MODULE foo
                    PROC bar(\\num x | num y | num z)
                        IF Present(x) THEN
                            z := <warning descr="Variable 'y' is always missing">y</warning> + x + <warning descr="Variable 'z' is always missing">z</warning>;
                        ELSE
                            z := <warning descr="Variable 'y' might be missing">y</warning> - <warning descr="Variable 'x' is always missing">x</warning> - <warning descr="Variable 'z' might be missing">z</warning>;
                        ENDIF
                    ENDPROC
                ENDMODULE
                """);
    }
}

package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.debug.ControlFlowFormatVisitor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ControlFlowTest extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        ControlFlowService.getInstance().reload();
        super.setUp();
    }

    private void check(@NotNull String text, @NotNull String expected) {
        myFixture.configureByText(RapidFileType.getInstance(), text);
        ControlFlowService service = ControlFlowService.getInstance();
        Set<ControlFlowBlock> controlFlow = service.getDataFlow(myFixture.getProject());
        assertTextEquals(expected.replaceAll(" {4}", "\t"), ControlFlowFormatVisitor.format(controlFlow).replaceAll(" {4}", "\t"));
    }

    public void testSimpleFunction() {
        check("""
                MODULE foo
                    FUNC num bar()
                        VAR num value := 2 * 3;
                        return value;
                    ENDFUNC
                ENDMODULE
                """, """
                func num foo:bar() {
                	var num _0 [value];
                	
                	STATEMENT_LIST:
                	0: _0 := 2.0 * 3.0;
                	1: return _0;
                }
                """);
    }

    public void testIfStatement() {
        check("""
                MODULE foo
                    FUNC num bar()
                        VAR num value := 2 * 3;
                        IF value > 4 THEN
                            value := value + 1;
                        ELSEIF value > 2 THEN
                            value := value - 1;
                        ELSE
                            value := value * 2 + 2;
                        ENDIF
                        RETURN value;
                    ENDFUNC
                ENDMODULE
                """, """
                func num foo:bar() {
                	var num _0 [value];
                	bool _1;
                	bool _2;
                                
                	STATEMENT_LIST:
                	0: _0 := 2.0 * 3.0;
                	1: _1 := _0 > 4.0;
                	2: if(_1) -> [true: 3, false: 4]
                                
                	3: _0 := _0 + 1.0;
                	   goto -> [8];
                                
                	4: _2 := _0 > 2.0;
                	5: if(_2) -> [true: 6, false: 7]
                                
                	6: _0 := _0 - 1.0;
                	   goto -> [8];
                                
                	7: _0 := (_0 * 2.0) + 2.0;
                	8: return _0;
                }
                """);
    }

    public void testWhileStatement() {
        check("""
                MODULE foo
                    FUNC num bar()
                        VAR num value := 2 * 3;
                        WHILE value > 0 DO
                            value := value - 1;
                        ENDWHILE
                        return value;
                    ENDFUNC
                ENDMODULE
                """, """
                func num foo:bar() {
                	var num _0 [value];
                	bool _1;
                                
                	STATEMENT_LIST:
                	0: _0 := 2.0 * 3.0;
                	1: _1 := _0 > 0.0;
                	2: if(_1) -> [true: 3, false: 4]
                                
                	3: _0 := _0 - 1.0;
                	   goto -> [1];
                                
                	4: return _0;
                }
                """);
    }

    public void testArrayExpression() {
        check("""
                MODULE foo
                    FUNC num bar()
                        VAR num value{2, 3} := [[1, 2, 3], [3, 4, 5]];
                        value{2, 3} := 6;
                        return value{0, 2};
                    ENDFUNC
                ENDMODULE
                """, """
                func num foo:bar() {
                	var num{*,*} _0 [value];
                                
                	STATEMENT_LIST:
                	0: _0 := [[1.0, 2.0, 3.0], [3.0, 4.0, 5.0]];
                	1: _0[2.0][3.0] := 6.0;
                	2: return _0[0.0][2.0];
                }
                """);
    }

    public void testGoToStatement() {
        check("""
                MODULE foo
                    PROC bar()
                        VAR num value;
                        label_1:
                        value := 0;
                        GOTO label_1;
                    ENDPROC
                ENDMODULE
                """, """
                proc foo:bar() {
                	var num _0 [value];
                                
                	STATEMENT_LIST:
                	0: _0 := 0.0;
                	   goto -> [0];
                }
                """);
    }

    public void testForStatement() {
        check("""
                MODULE foo
                    FUNC num bar()
                        VAR num value := 0;
                        FOR i FROM 0 TO 10 DO
                            value := value + 1;
                        ENDFOR
                        return value;
                    ENDFUNC
                ENDMODULE
                """, """
                func num foo:bar() {
                	var num _0 [value];
                	num _1;
                	num _2;
                	bool _3;
                	bool _4;
                	bool _5;
                                
                	STATEMENT_LIST:
                	0: _0 := 0.0;
                	1: _1 := 0.0;
                	2: _3 := _1 < 10.0;
                	3: if(_3) -> [true: 4, false: 5]
                                
                	4: _2 := 1;
                	   goto -> [6];
                                
                	5: _2 := -1;
                	6: _5 := _2 < 0;
                	7: if(_5) -> [true: 8, false: 9]
                                
                	8: _4 := _1 > 10.0;
                	   goto -> [10];
                                
                	9: _4 := _1 < 10.0;
                	10: if(_4) -> [true: 11, false: 13]
                                
                	11: _0 := _0 + 1.0;
                	12: _1 := _1 + _2;
                		goto -> [6];
                                
                	13: return _0;
                }
                """);
    }

    public void testStepForStatement() {
        check("""
                MODULE foo
                    FUNC num bar()
                        VAR num value := 0;
                        FOR i FROM 0 TO 10 STEP 5 DO
                            value := value + 1;
                        ENDFOR
                        return value;
                    ENDFUNC
                ENDMODULE
                """, """
                func num foo:bar() {
                	var num _0 [value];
                	num _1;
                	bool _2;
                	bool _3;
                                
                	STATEMENT_LIST:
                	0: _0 := 0.0;
                	1: _1 := 0.0;
                	2: _3 := 5.0 < 0;
                	3: if(_3) -> [true: 4, false: 5]
                                
                	4: _2 := _1 > 10.0;
                	   goto -> [6];
                                
                	5: _2 := _1 < 10.0;
                	6: if(_2) -> [true: 7, false: 9]
                                
                	7: _0 := _0 + 1.0;
                	8: _1 := _1 + 5.0;
                	   goto -> [2];
                                
                	9: return _0;
                }
                """);
    }

    public void testTestStatement() {
        check("""
                MODULE foo
                    FUNC num bar()
                        VAR num value := 0;
                        TEST value
                            CASE 0:
                                RETURN 0;
                            CASE 1, 3:
                                RETURN 1;
                            CASE 4, 5, 6:
                                RETURN 3;
                            DEFAULT:
                                RETURN -1;
                            CASE 2:
                                RETURN 2;
                        ENDTEST
                    ENDFUNC
                ENDMODULE
                """, """
                func num foo:bar() {
                	var num _0 [value];
                	bool _1;
                	bool _2;
                	bool _3;
                                
                	STATEMENT_LIST:
                	0: _0 := 0.0;
                	1: _1 := _0 = 0.0;
                	2: if(_1) -> [true: 3, false: 4]
                                
                	3: return 0.0;
                                
                	4: _2 := (_0 = 1.0) OR (_0 = 3.0);
                	5: if(_2) -> [true: 6, false: 7]
                                
                	6: return 1.0;
                                
                	7: _3 := ((_0 = 4.0) OR (_0 = 5.0)) OR (_0 = 6.0);
                	8: if(_3) -> [true: 9, false: 10]
                                
                	9: return 3.0;
                                
                	10: return -1.0;
                }
                """);
    }

    public void testProcedureCall() {
        check("""
                MODULE foo
                    FUNC num bar()
                        return Abs(-1);
                    ENDFUNC
                    
                    FUNC num Abs(num value)
                        IF value >= 0 THEN
                            return value;
                        ELSE
                            return -value;
                        ENDIF
                    ENDFUNC
                ENDMODULE
                """, """
                func num foo:Abs(input num _0 [value]) {
                	bool _1;
                                
                	STATEMENT_LIST:
                	0: _1 := _0 >= 0.0;
                	1: if(_1) -> [true: 2, false: 3]
                                
                	2: return _0;
                                
                	3: return -_0;
                }
                                
                func num foo:bar() {
                	num _0;
                	num _1;
                                
                	STATEMENT_LIST:
                	0: _1 := -1.0;
                	1: _0 := foo:Abs(_0 := _1);
                	2: return _0;
                }
                """);
    }

    public void testProcedureCallWithConditionalArgument() {
        check("""
                MODULE foo
                    PROC bar(\\num a)
                        conditional \\a?a;
                    ENDPROC
                    
                    PROC conditional(\\num a) ENDPROC
                ENDMODULE
                """, """
                proc foo:bar(\\input num _0 [a]) {
                	bool _1;
                                
                	STATEMENT_LIST:
                	0: _1 := PRESENT _0;
                	1: if(_1) -> [true: 2, false: 3]
                                
                	2: foo:conditional(_a := _0);
                	   goto -> [4];
                                
                	3: foo:conditional();
                	4: return;
                }
                                
                proc foo:conditional(\\input num _0 [a]) {
                	STATEMENT_LIST:
                	0: return;
                }
                """);
    }

    public void testFunctionCallExpressionWithMultipleConditionalArguments() {
        check("""
                MODULE foo
                    PROC bar(\\num a, \\num b)
                        conditional \\a?a, \\b?b;
                    ENDPROC
                    
                    PROC conditional(\\num a, \\num b) ENDPROC
                ENDMODULE
                """, """
                proc foo:bar(\\input num _0 [a], \\input num _1 [b]) {
                	bool _2;
                	bool _3;
                	bool _4;
                                
                	STATEMENT_LIST:
                	0: _2 := PRESENT _0;
                	1: if(_2) -> [true: 2, false: 6]
                                
                	2: _3 := PRESENT _1;
                	3: if(_3) -> [true: 4, false: 5]
                                
                	4: foo:conditional(_a := _0, _b := _1);
                	   goto -> [10];
                                
                	5: foo:conditional(_a := _0);
                	   goto -> [10];
                                
                	6: _4 := PRESENT _1;
                	7: if(_4) -> [true: 8, false: 9]
                                
                	8: foo:conditional(_b := _1);
                	   goto -> [10];
                                
                	9: foo:conditional();
                	10: return;
                }
                                
                proc foo:conditional(\\input num _0 [a], \\input num _1 [b]) {
                	STATEMENT_LIST:
                	0: return;
                }
                """);
    }
}

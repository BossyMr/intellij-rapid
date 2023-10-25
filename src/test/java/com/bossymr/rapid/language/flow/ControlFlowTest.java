package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.debug.ControlFlowFormatVisitor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

public class ControlFlowTest extends BasePlatformTestCase {

    private void check(@NotNull String text, @NotNull String expected) {
        myFixture.configureByText(RapidFileType.getInstance(), text);
        ControlFlow controlFlow = ControlFlowService.getInstance().getControlFlow(myFixture.getProject());
        assertTextEquals(expected.replaceAll(" {4}", "\t"), ControlFlowFormatVisitor.format(controlFlow));
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
                                
                	STATEMENT_LIST:
                	0: _0 := 2.0 * 3.0;
                	1: if(_0 > 4.0) -> [true: 2, false: 3]
                                
                	2: _0 := _0 + 1.0;
                	   goto -> [6];
                                
                	3: if(_0 > 2.0) -> [true: 4, false: 5]
                                
                	4: _0 := _0 - 1.0;
                	   goto -> [6];
                                
                	5: _0 := _0 * 2.0 + 2.0;
                	6: return _0;
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
                                
                	STATEMENT_LIST:
                	0: _0 := 2.0 * 3.0;
                	1: if(_0 > 0.0) -> [true: 2, false: 3]
                                
                	2: _0 := _0 - 1.0;
                	   goto -> [1];
                                
                	3: return _0;
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
                	var num _1 [i];
                	num _2;
                	bool _3;
                                
                	STATEMENT_LIST:
                	0: _0 := 0.0;
                	1: _1 := 0.0;
                	2: if(_1 < 10.0) -> [true: 3, false: 4]
                                
                	3: _2 := 1;
                	   goto -> [5];
                                
                	4: _2 := -1;
                	5: if(_2 < 0) -> [true: 6, false: 7]
                                
                	6: _3 := _1 > 10.0;
                	   goto -> [8];
                                
                	7: _3 := _1 < 10.0;
                	8: if(_3) -> [true: 9, false: 10]
                                
                	9: _0 := _0 + 1.0;
                	   goto -> [5];
                                
                	10: return _0;
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
                	var num _1 [i];
                	bool _2;
                                
                	STATEMENT_LIST:
                	0: _0 := 0.0;
                	1: _1 := 0.0;
                	2: if(5.0 < 0) -> [true: 3, false: 4]
                                
                	3: _2 := _1 > 10.0;
                	   goto -> [5];
                                
                	4: _2 := _1 < 10.0;
                	5: if(_2) -> [true: 6, false: 7]
                                
                	6: _0 := _0 + 1.0;
                	   goto -> [2];
                                
                	7: return _0;
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
                                
                	STATEMENT_LIST:
                	0: _0 := 0.0;
                	1: if(_0 = 0.0) -> [true: 2, false: 3]
                                
                	2: return 0.0;
                                
                	3: if(_0 = 1.0 OR _0 = 3.0) -> [true: 4, false: 5]
                                
                	4: return 1.0;
                                
                	5: if(_0 = 4.0 OR _0 = 5.0 OR _0 = 6.0) -> [true: 6, false: 7]
                                
                	6: return 3.0;
                                
                	7: return -1.0;
                }
                """);
    }

    public void testFunctionCallExpression() {
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
                	STATEMENT_LIST:
                	0: if(_0 >= 0.0) -> [true: 1, false: 2]
                                
                	1: return _0;
                                
                	2: return -_0;
                }
                                
                func num foo:bar() {
                	num _0;
                	num _1;
                                
                	STATEMENT_LIST:
                	0: _1 := -1.0;
                	1: _0 := :Abs(_0 := _1);
                	2: return _0;
                }
                """);
    }

    public void testFunctionCallExpressionWithConditionalArgument() {
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
                	0: _1 := :Present(_0 := _0);
                	1: if(_1) -> [true: 2, false: 3]
                                
                	2: conditional(_a := _0);
                	   goto -> [4];
                                
                	3: conditional();
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
                	0: _2 := :Present(_0 := _0);
                	1: if(_2) -> [true: 2, false: 6]
                                
                	2: _3 := :Present(_0 := _1);
                	3: if(_3) -> [true: 4, false: 5]
                                
                	4: conditional(_a := _0, _b := _1);
                	   goto -> [10];
                                
                	5: conditional(_a := _0);
                	   goto -> [10];
                                
                	6: _4 := :Present(_0 := _1);
                	7: if(_4) -> [true: 8, false: 9]
                                
                	8: conditional(_b := _1);
                	   goto -> [10];
                                
                	9: conditional();
                	10: return;
                }
                                
                proc foo:conditional(\\input num _0 [a], \\input num _1 [b]) {
                	STATEMENT_LIST:
                	0: return;
                }
                """);
    }
}

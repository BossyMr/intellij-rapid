package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.debug.ControlFlowFormatVisitor;
import com.bossymr.rapid.language.flow.parser.ControlFlowElementVisitor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

public class ControlFlowTest extends BasePlatformTestCase {

    private void check(@NotNull String text, @NotNull String expected) {
        myFixture.configureByText(RapidFileType.getInstance(), text);
        ControlFlow controlFlow = ControlFlowElementVisitor.createControlFlow(myFixture.getModule());
        assertTextEquals(expected.replaceAll(" {4}", "\t"), ControlFlowFormatVisitor.format(controlFlow));
    }

    public void testSimpleFunction() {
        check("""
                MODULE foo
                    FUNC num bar()
                        VAR num value := 2 * 3;
                        return value;
                    ENDPROC
                ENDMODULE
                """, """
                func num foo:bar() {
                	var num _0 [value];
                	
                	entry 0 {
                		_0 := 2.0 * 3.0;
                		return _0;
                	}
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
                    ENDPROC
                ENDMODULE
                """, """
                func num foo:bar() {
                	var num _0 [value];
                	bool _1;
                	num _2;
                	bool _3;
                	num _4;
                	num _5;
                	num _6;
                                
                	entry 0 {
                		_0 := 2.0 * 3.0;
                		_1 := _0 > 4.0;
                		if(_1) -> [true: 1, false: 2]
                	}
                                
                	block 1 {
                		_2 := _0 + 1.0;
                		_0 := _2;
                		goto -> 3;
                	}
                                
                	block 2 {
                		_3 := _0 > 2.0;
                		if(_3) -> [true: 4, false: 5]
                	}
                                
                	block 3 {
                		return _0;
                	}
                                
                	block 4 {
                		_4 := _0 - 1.0;
                		_0 := _4;
                		goto -> 3;
                	}
                                
                	block 5 {
                		_5 := _0 * 2.0;
                		_6 := _5 + 2.0;
                		_0 := _6;
                		goto -> 3;
                	}
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
                    ENDPROC
                ENDMODULE
                """, """
                func num foo:bar() {
                    var num _0 [value];
                	bool _1;
                	num _2;
                                
                	entry 0 {
                		_0 := 2.0 * 3.0;
                		goto -> 1;
                	}
                                
                	block 1 {
                		_1 := _0 > 0.0;
                		if(_1) -> [true: 3, false: 2]
                	}
                                
                	block 2 {
                		return _0;
                	}
                                
                	block 3 {
                		_2 := _0 - 1.0;
                		_0 := _2;
                		goto -> 1;
                	}
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
                                
                	entry 0 {
                		goto -> 1;
                	}
                                
                	block 1 {
                		_0 := 0.0;
                		goto -> 1;
                	}
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
                    ENDPROC
                ENDMODULE
                """, """
                func num foo:bar() {
                	var num _0 [value] := 0.0;
                	num _1 [i] := 0.0;
                	num _2 := 0;
                	bool _3 := false;
                	num _4;
                	bool _5 := false;
                                
                	entry 0 {
                		_3 := _1 < 10.0;
                		if(_3) -> [true: 3, false: 4]
                	}
                                
                	block 1 {
                		_4 := _0 + 1.0;
                		_0 := _4;
                		_1 := _1 + _2;
                		_5 := _1 = 10.0;
                		if(_5) -> [true: 2, false: 1]
                	}
                                
                	block 2 {
                		return _0;
                	}
                                
                	block 3 {
                		_2 := 1;
                		goto -> 1;
                	}
                                
                	block 4 {
                		_2 := -1;
                		goto -> 1;
                	}
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
                    ENDPROC
                ENDMODULE
                """, """
                func num foo:bar() {
                	var num _0 [value] := 0.0;
                	num _1 [i] := 0.0;
                	num _2;
                	bool _3 := false;
                                
                	entry 0 {
                		goto -> 1;
                	}
                                
                	block 1 {
                		_2 := _0 + 1.0;
                		_0 := _2;
                		_1 := _1 + 5.0;
                		_3 := _1 = 10.0;
                		if(_3) -> [true: 2, false: 1]
                	}
                                
                	block 2 {
                		return _0;
                	}
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
                    ENDPROC
                ENDMODULE
                """, """
                func num foo:bar() {
                	var num _0 [value] := 0.0;
                	bool _1 := false;
                	bool _2 := false;
                	bool _3 := false;
                	bool _4 := false;
                	bool _5 := false;
                	bool _6 := false;
                	bool _7 := false;
                	bool _8 := false;
                	bool _9 := false;
                                
                	entry 0 {
                		_1 := _0 = 0.0;
                		if(_1) -> [true: 1, false: 2]
                	}
                                
                	block 1 {
                		return 0.0;
                	}
                                
                	block 2 {
                		_2 := _0 = 1.0;
                		_3 := _0 = 3.0;
                		_4 := _2 OR _3;
                		_2 := _4;
                		if(_2) -> [true: 3, false: 4]
                	}
                                
                	block 3 {
                		return 1.0;
                	}
                                
                	block 4 {
                		_5 := _0 = 4.0;
                		_6 := _0 = 5.0;
                		_7 := _5 OR _6;
                		_5 := _7;
                		_8 := _0 = 6.0;
                		_9 := _5 OR _8;
                		_5 := _9;
                		if(_5) -> [true: 5, false: 6]
                	}
                                
                	block 5 {
                		return 3.0;
                	}
                                
                	block 6 {
                		goto -> 7;
                	}
                                
                	block 7 {
                		return -1.0;
                	}
                }
                """);
    }

    public void testFunctionCallExpression() {
        check("""
                MODULE foo
                    FUNC num bar()
                        VAR num value := -1;
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
                func num foo:bar() {
                	var num _0 [value];
                	num _1;
                                
                	entry 0 {
                		_0 := -1.0;
                		_1 := foo:Abs(_0 := -1.0) -> 1;
                	}
                                
                	block 1 {
                		return _1;
                	}
                }
                                
                func num foo:Abs(input num _0 [value]) {
                    bool _1;
                    num _2;
                    
                    entry 0 {
                        _1 := _0 >= 0.0;
                        if(_1) -> [true: 1, false: 2]
                    }
                    
                    block 1 {
                        return _0;
                    }
                    
                    block 2 {
                        _2 := -_0;
                        return _2;
                    }
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
                                
                	entry 0 {
                		_1 := Present(_0 := _0) -> 2;
                	}
                                
                	block 1 {
                		return;
                	}
                                
                	block 2 {
                		if(_1) -> [true: 3, false: 4]
                	}
                                
                	block 3 {
                		foo:conditional(_0) -> 1;
                	}
                                
                	block 4 {
                		foo:conditional() -> 1;
                	}
                }
                                
                proc foo:conditional(\\input num _0 [a]) {
                	entry 0 {
                		return;
                	}
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
                                
                	entry 0 {
                		_2 := Present(_0 := _0) -> 2;
                	}
                                
                	block 1 {
                		return;
                	}
                                
                	block 2 {
                		if(_2) -> [true: 3, false: 4]
                	}
                                
                	block 3 {
                		_3 := Present(_0 := _1) -> 5;
                	}
                                
                	block 4 {
                		_4 := Present(_0 := _1) -> 8;
                	}
                                
                	block 5 {
                		if(_3) -> [true: 6, false: 7]
                	}
                                
                	block 6 {
                		foo:conditional(_0, _1) -> 1;
                	}
                                
                	block 7 {
                		foo:conditional(_0) -> 1;
                	}
                                
                	block 8 {
                		if(_4) -> [true: 9, false: 10]
                	}
                                
                	block 9 {
                		foo:conditional(_1) -> 1;
                	}
                                
                	block 10 {
                		foo:conditional() -> 1;
                	}
                }
                                
                proc foo:conditional(\\input num _0 [a], \\input num _1 [b]) {
                	entry 0 {
                		return;
                	}
                }
                """);
    }
}

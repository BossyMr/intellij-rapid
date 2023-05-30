package com.bossymr.rapid.language.flow;

import com.bossymr.network.client.security.Credentials;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.debug.ViewFlowAction;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

public class ControlFlowTest extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (RobotService.getInstance().getRobot() == null) {
            RobotService.getInstance().connect(URI.create("http://localhost:80"), new Credentials("Default User", "robotics".toCharArray()));
        }
    }

    private void check(@NotNull String text, @NotNull String expected) {
        myFixture.configureByText(RapidFileType.getInstance(), text);
        ControlFlow controlFlow = ControlFlowService.createControlFlow(myFixture.getModule());
        assertTextEquals(expected.replaceAll(" {4}", "\t"), ViewFlowAction.getControlFlowText(controlFlow));
    }

    public void testSimpleFunction() {
        check("""
                MODULE foo
                    FUNC num bar()
                        VAR num value := 2 * 3;
                        RETURN value;
                    ENDPROC
                ENDMODULE
                """, """
                FUNCTION num foo::bar() {
                	VAR num _0 [value];
                	
                	scope 0 [statement_list] {
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
                FUNCTION num foo::bar() {
                    VAR num _0 [value];
                    bool _1;
                    num _2;
                    bool _3;
                    num _4;
                    num _5;
                    num _6;
                 
                    scope 0 [statement_list] {
                        _0 := 2.0 * 3.0;
                        _1 := _0 > 4.0;
                        if(_1) -> [true:2, false:3]
                    }
                 
                    scope 1 {
                        return _0;
                    }
                 
                    scope 2 {
                        _2 := _0 + 1.0;
                        _0 := _2;
                        goto -> 1;
                    }
                 
                    scope 3 {
                        _3 := _0 > 2.0;
                        if(_3) -> [true:4, false:5]
                    }
                 
                    scope 4 {
                        _4 := _0 - 1.0;
                        _0 := _4;
                        goto -> 1;
                    }
                 
                    scope 5 {
                        _5 := _0 * 2.0;
                        _6 := _5 + 2.0;
                        _0 := _6;
                        goto -> 1;
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
                        RETURN value;
                    ENDPROC
                ENDMODULE
                """, """
                FUNCTION num foo::bar() {
                    VAR num _0 [value];
                	bool _1;
                	num _2;
                                
                	scope 0 [statement_list] {
                		_0 := 2.0 * 3.0;
                		goto -> 1;
                	}
                                
                	scope 1 {
                		_1 := _0 > 0.0;
                		if(_1) -> [true:3, false:2]
                	}
                                
                	scope 2 {
                		return _0;
                	}
                                
                	scope 3 {
                		_2 := _0 - 1.0;
                		_0 := _2;
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
                        RETURN value;
                    ENDPROC
                ENDMODULE
                """, """
                FUNCTION num foo::bar() {
                	VAR num _0 [value] := 0.0;
                	num _1 [i] := 0.0;
                	num _2 := 0;
                	bool _3 := false;
                	num _4;
                	bool _5 := false;
                                
                	scope 0 [statement_list] {
                		_3 := _1 < 10.0;
                		if(_3) -> [true:3, false:4]
                	}
                                
                	scope 1 {
                		_4 := _0 + 1.0;
                		_0 := _4;
                		_1 := _1 + _2;
                		_5 := _1 = 10.0;
                		if(_5) -> [true:2, false:1]
                	}
                                
                	scope 2 {
                		return _0;
                	}
                                
                	scope 3 {
                		_2 := 1;
                		goto -> 1;
                	}
                                
                	scope 4 {
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
                        RETURN value;
                    ENDPROC
                ENDMODULE
                """, """
                FUNCTION num foo::bar() {
                	VAR num _0 [value] := 0.0;
                	num _1 [i] := 0.0;
                	num _2;
                	bool _3 := false;
                                
                	scope 0 [statement_list] {
                		goto -> 1;
                	}
                                
                	scope 1 {
                		_2 := _0 + 1.0;
                		_0 := _2;
                		_1 := _1 + 5.0;
                		_3 := _1 = 10.0;
                		if(_3) -> [true:2, false:1]
                	}
                                
                	scope 2 {
                		return _0;
                	}
                }
                """);
    }

    public void testFunctionCallExpression() {
        check("""
                MODULE foo
                    FUNC num bar()
                        VAR num value := -1;
                        RETURN Abs(-1);
                    ENDFUNC
                ENDMODULE
                """, """
                FUNCTION num foo::bar() {
                	VAR num _0 [value];
                	num _1;
                                
                	scope 0 [statement_list] {
                		_0 := -1.0;
                		_1 := "Abs"(_0 := -1.0) -> 1;
                	}
                                
                	scope 1 {
                		return _1;
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
                PROCEDURE foo::bar(\\num a (0)) {
                	bool _1;
                                
                	scope 0 [statement_list] {
                		_1 := "Present"(_0 := _0) -> 2;
                	}
                                
                	scope 1 {
                		return;
                	}
                                
                	scope 2 {
                		if(_1) -> [true:3, false:4]
                	}
                                
                	scope 3 {
                		"conditional"(_0) -> 1;
                	}
                                
                	scope 4 {
                		"conditional"() -> 1;
                	}
                }
                                
                PROCEDURE foo::conditional(\\num a (0)) {
                                
                	scope 0 [statement_list] {
                		return;
                	}
                }
                """);
    }

    public void testFunctionCallExpressionWithManyConditionalArguments() {
        check("""
                MODULE foo
                    PROC bar(\\num a, \\num b)
                        conditional \\a?a, \\b?b;
                    ENDPROC
                    
                    PROC conditional(\\num a, \\num b) ENDPROC
                ENDMODULE
                """, """
                PROCEDURE foo::bar(\\num a (0)\\num b (1)) {
                	bool _2;
                	bool _3;
                	bool _4;
                                
                	scope 0 [statement_list] {
                		_2 := "Present"(_0 := _0) -> 2;
                	}
                                
                	scope 1 {
                		return;
                	}
                                
                	scope 2 {
                		if(_2) -> [true:3, false:4]
                	}
                                
                	scope 3 {
                		_3 := "Present"(_0 := _1) -> 5;
                	}
                                
                	scope 4 {
                		_4 := "Present"(_0 := _1) -> 8;
                	}
                                
                	scope 5 {
                		if(_3) -> [true:6, false:7]
                	}
                                
                	scope 6 {
                		"conditional"(_0, _1) -> 1;
                	}
                                
                	scope 7 {
                		"conditional"(_0) -> 1;
                	}
                                
                	scope 8 {
                		if(_4) -> [true:9, false:10]
                	}
                                
                	scope 9 {
                		"conditional"(_1) -> 1;
                	}
                                
                	scope 10 {
                		"conditional"() -> 1;
                	}
                }
                                
                PROCEDURE foo::conditional(\\num a (0)\\num b (1)) {
                                
                	scope 0 [statement_list] {
                		return;
                	}
                }
                """);
    }
}

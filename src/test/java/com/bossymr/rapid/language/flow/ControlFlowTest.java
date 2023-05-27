package com.bossymr.rapid.language.flow;

import com.bossymr.network.client.security.Credentials;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.debug.ViewFlowAction;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.net.URI;

public class ControlFlowTest extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (RobotService.getInstance().getRobot() == null) {
            RobotService.getInstance().connect(URI.create("http://localhost:80"), new Credentials("Default User", "robotics".toCharArray()));
        }
    }

    public void testSimpleFunction() {
        myFixture.configureByText(RapidFileType.getInstance(), """
                MODULE foo
                    FUNC num bar()
                        VAR num value := 2 * 3;
                        RETURN value;
                    ENDPROC
                ENDMODULE
                """);
        ControlFlow controlFlow = ControlFlowVisitor.createControlFlow(myFixture.getModule());
        assertEquals("""
                FUNCTION num foo::bar() {
                	VAR num _0 [value];
                	
                 	scope 0 [regular] {
                		_0 := 2.0 * 3.0;
                		return _0;
                	}
                }
                """, ViewFlowAction.getControlFlowText(controlFlow));
        System.out.println(ViewFlowAction.getControlFlowText(controlFlow));
    }

    public void testIfStatement() {
        myFixture.configureByText(RapidFileType.getInstance(), """
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
                """);
        ControlFlow controlFlow = ControlFlowVisitor.createControlFlow(myFixture.getModule());
        assertEquals("""
                FUNCTION num foo::bar() {
                    VAR num _0 [value];
                    bool _1;
                    num _2;
                    bool _3;
                    num _4;
                    num _5;
                    num _6;
                 
                    scope 0 [regular] {
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
                """, ViewFlowAction.getControlFlowText(controlFlow));
        System.out.println(ViewFlowAction.getControlFlowText(controlFlow));
    }

    public void testWhileStatement() {
        myFixture.configureByText(RapidFileType.getInstance(), """
                MODULE foo
                    FUNC num bar()
                        VAR num value := 2 * 3;
                        WHILE value > 0 DO
                            value := value - 1;
                        ENDWHILE
                        RETURN value;
                    ENDPROC
                ENDMODULE
                """);
        ControlFlow controlFlow = ControlFlowVisitor.createControlFlow(myFixture.getModule());
        assertEquals("""
                FUNCTION num foo::bar() {
                	VAR num _0 [value];
                	bool _1;
                	num _2;
                                
                	scope 0 [regular] {
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
                """, ViewFlowAction.getControlFlowText(controlFlow));
        System.out.println(ViewFlowAction.getControlFlowText(controlFlow));
    }

    public void testForStatement() {
        myFixture.configureByText(RapidFileType.getInstance(), """
                MODULE foo
                    FUNC num bar()
                        VAR num value := 0;
                        FOR i FROM 0 TO 10 DO
                            value := value + 1;
                        ENDFOR
                        RETURN value;
                    ENDPROC
                ENDMODULE
                """);
        ControlFlow controlFlow = ControlFlowVisitor.createControlFlow(myFixture.getModule());
        assertEquals("""
                                
                """, ViewFlowAction.getControlFlowText(controlFlow));
        System.out.println(ViewFlowAction.getControlFlowText(controlFlow));
    }
}

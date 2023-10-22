package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.Label;
import com.bossymr.rapid.language.builder.RapidBuilder;
import com.bossymr.rapid.language.flow.ControlFlow;
import com.bossymr.rapid.language.flow.debug.ControlFlowFormatVisitor;
import com.bossymr.rapid.language.flow.value.BinaryOperator;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.flow.value.UnaryOperator;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ControlFlowBuilderTest {

    private void check(@NotNull Consumer<RapidBuilder> consumer, @NotNull String expected) {
        ControlFlowBuilder builder = new ControlFlowBuilder();
        consumer.accept(builder);
        ControlFlow controlFlow = builder.getControlFlow();
        String actual = ControlFlowFormatVisitor.format(controlFlow);
        if (expected.isEmpty()) {
            System.out.println(actual);
        } else {
            assertEquals(expected.replaceAll(" {4}", "\t"), actual);
        }
    }

    @Test
    void assign() {
        check(builder -> {
            builder.withModule("foo", moduleBuilder -> {
                moduleBuilder.withRoutine("bar", RoutineType.PROCEDURE, null, routineBuilder -> {
                    routineBuilder.withCode(codeBuilder -> {
                        ReferenceExpression x = codeBuilder.createVariable(RapidPrimitiveType.NUMBER);
                        codeBuilder.assign(x, codeBuilder.literal(0));
                        ReferenceExpression y = codeBuilder.createVariable(RapidPrimitiveType.BOOLEAN);
                        codeBuilder.assign(y, codeBuilder.binary(BinaryOperator.EQUAL_TO, x, codeBuilder.literal(0)));
                    });
                });
            });
        }, """
                proc foo:bar() {
                	num _0;
                	bool _1;
                                
                	STATEMENT_LIST:
                	0: _0 := 0;
                	1: _1 := _0 = 0;
                	2: return;
                }
                """);
    }

    @Test
    void ifThen() {
        check(builder -> {
            builder.withModule("foo", moduleBuilder -> {
                moduleBuilder.withRoutine("bar", RoutineType.PROCEDURE, null, routineBuilder -> {
                    routineBuilder.withCode(codeBuilder -> {
                        ReferenceExpression x = codeBuilder.createVariable(RapidPrimitiveType.NUMBER);
                        codeBuilder.assign(x, codeBuilder.literal(0));
                        ReferenceExpression y = codeBuilder.createVariable(RapidPrimitiveType.BOOLEAN);
                        codeBuilder.assign(y, codeBuilder.binary(BinaryOperator.EQUAL_TO, x, codeBuilder.literal(0)));
                        codeBuilder.ifThen(y, ifThenBuilder -> ifThenBuilder.assign(x, ifThenBuilder.literal(1)));
                        codeBuilder.assign(x, codeBuilder.literal(-1));
                    });
                });
            });
        }, """
                proc foo:bar() {
                	num _0;
                	bool _1;
                                
                	STATEMENT_LIST:
                	0: _0 := 0;
                	1: _1 := _0 = 0;
                	2: if(_1) -> [true: 3, false: 4]
                                
                	3: _0 := 1;
                	4: _0 := -1;
                	5: return;
                }
                """);
    }

    @Test
    void ifThenElseNoFallThrough() {
        check(builder -> {
            builder.withModule("foo", moduleBuilder -> {
                moduleBuilder.withRoutine("bar", RoutineType.FUNCTION, RapidPrimitiveType.NUMBER, routineBuilder -> {
                    routineBuilder.withCode(codeBuilder -> {
                        ReferenceExpression x = codeBuilder.createVariable(RapidPrimitiveType.NUMBER);
                        codeBuilder.assign(x, codeBuilder.literal(0));
                        ReferenceExpression y = codeBuilder.createVariable(RapidPrimitiveType.BOOLEAN);
                        codeBuilder.assign(y, codeBuilder.binary(BinaryOperator.LESS_THAN, x, codeBuilder.literal(0)));
                        codeBuilder.ifThenElse(y,
                                ifThenBuilder -> ifThenBuilder.returnValue(ifThenBuilder.unary(UnaryOperator.NEGATE, x)),
                                ifThenBuilder -> ifThenBuilder.returnValue(x));
                    });
                });
            });
        }, """
                func num foo:bar() {
                	num _0;
                	bool _1;
                                
                	STATEMENT_LIST:
                	0: _0 := 0;
                	1: _1 := _0 < 0;
                	2: if(_1) -> [true: 3, false: 4]
                                
                	3: return -_0;
                                
                	4: return _0;
                }
                """);
    }

    @Test
    void goToUnknownLabel() {
        check(builder -> {
            builder.withModule("foo", moduleBuilder -> {
                moduleBuilder.withRoutine("bar", RoutineType.PROCEDURE, null, routineBuilder -> {
                    routineBuilder.withCode(codeBuilder -> {
                        codeBuilder.goTo(codeBuilder.getLabel("label"));
                        codeBuilder.assign(codeBuilder.createVariable(RapidPrimitiveType.NUMBER), codeBuilder.literal(0));
                        codeBuilder.createLabel("label");
                        codeBuilder.exit();
                    });
                });
            });
        }, """
                proc foo:bar() {
                    num _0;
                
                	STATEMENT_LIST:
                	0: exit;
                }
                """);
    }

    @Test
    void ifThenElseEmptyBlock() {
        check(builder -> {
            builder.withModule("foo", moduleBuilder -> {
                moduleBuilder.withRoutine("bar", RoutineType.FUNCTION, RapidPrimitiveType.NUMBER, routineBuilder -> {
                    routineBuilder.withCode(codeBuilder -> {
                        ReferenceExpression x = codeBuilder.createVariable(RapidPrimitiveType.NUMBER);
                        codeBuilder.assign(x, codeBuilder.literal(0));
                        ReferenceExpression y = codeBuilder.createVariable(RapidPrimitiveType.BOOLEAN);
                        codeBuilder.assign(y, codeBuilder.binary(BinaryOperator.LESS_THAN, x, codeBuilder.literal(0)));
                        codeBuilder.ifThenElse(y,
                                ifThenBuilder -> {},
                                ifThenBuilder -> ifThenBuilder.returnValue(x));
                        codeBuilder.returnValue(codeBuilder.unary(UnaryOperator.NEGATE, x));
                    });
                });
            });
        }, """
                func num foo:bar() {
                	num _0;
                	bool _1;
                                
                	STATEMENT_LIST:
                	0: _0 := 0;
                	1: _1 := _0 < 0;
                	2: if(_1) -> [true: 4, false: 3]
                                                                
                	3: return _0;
                	
                	4: return -_0;
                }
                """);
    }

    @Test
    void ifThenElseGotoBlock() {
        check(builder -> {
            builder.withModule("foo", moduleBuilder -> {
                moduleBuilder.withRoutine("bar", RoutineType.FUNCTION, RapidPrimitiveType.NUMBER, routineBuilder -> {
                    routineBuilder.withCode(codeBuilder -> {
                        ReferenceExpression x = codeBuilder.createVariable(RapidPrimitiveType.NUMBER);
                        Label label = codeBuilder.createLabel();
                        codeBuilder.assign(x, codeBuilder.literal(0));
                        ReferenceExpression y = codeBuilder.createVariable(RapidPrimitiveType.BOOLEAN);
                        codeBuilder.assign(y, codeBuilder.binary(BinaryOperator.LESS_THAN, x, codeBuilder.literal(0)));
                        codeBuilder.ifThenElse(y,
                                ifThenBuilder -> ifThenBuilder.goTo(label),
                                ifThenBuilder -> ifThenBuilder.returnValue(x));
                        codeBuilder.returnValue(codeBuilder.unary(UnaryOperator.NEGATE, x));
                    });
                });
            });
        }, """
                func num foo:bar() {
                	num _0;
                	bool _1;
                                
                	STATEMENT_LIST:
                	0: _0 := 0;
                	1: _1 := _0 < 0;
                	2: if(_1) -> [true: 0, false: 3]
                                                                
                	3: return _0;
                }
                """);
    }

    @Test
    void call() {
        check(builder -> {
            builder.withModule("foo", moduleBuilder -> {
                moduleBuilder.withRoutine("Abs", RoutineType.FUNCTION, RapidPrimitiveType.NUMBER, routineBuilder -> {
                    routineBuilder.withParameterGroup(false, parameterGroupBuilder -> {
                                parameterGroupBuilder.withParameter("x", ParameterType.INPUT, RapidPrimitiveType.NUMBER);
                            })
                            .withCode(codeBuilder -> {
                                ReferenceExpression x = codeBuilder.getArgument("x");
                                codeBuilder.assign(x, codeBuilder.literal(0));
                                ReferenceExpression y = codeBuilder.createVariable(RapidPrimitiveType.BOOLEAN);
                                codeBuilder.assign(y, codeBuilder.binary(BinaryOperator.LESS_THAN, x, codeBuilder.literal(0)));
                                codeBuilder.ifThenElse(y,
                                        ifThenBuilder -> ifThenBuilder.returnValue(ifThenBuilder.unary(UnaryOperator.NEGATE, x)),
                                        ifThenBuilder -> ifThenBuilder.returnValue(x));
                            });
                }).withRoutine("bar", RoutineType.PROCEDURE, null, routineBuilder -> {
                    routineBuilder.withCode(codeBuilder -> {
                        codeBuilder.returnValue(codeBuilder.call("foo:Abs", RapidPrimitiveType.NUMBER, argumentBuilder -> {
                            argumentBuilder.withRequiredArgument(codeBuilder.literal(-1));
                        }));
                    });
                });
            });
        }, """
                func num foo:Abs(input num _0 [x]) {
                	bool _1;
                                
                	STATEMENT_LIST:
                	0: _0 := 0;
                	1: _1 := _0 < 0;
                	2: if(_1) -> [true: 3, false: 4]
                                
                	3: return -_0;
                                
                	4: return _0;
                }
                                
                proc foo:bar() {
                	num _0;
                	num _1;
                                
                	STATEMENT_LIST:
                	0: _1 := -1;
                	1: _0 := foo:Abs(_0 := _1);
                	2: return _0;
                }
                """);
    }
}
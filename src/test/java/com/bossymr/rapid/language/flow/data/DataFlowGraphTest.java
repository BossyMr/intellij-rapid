package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.builder.Label;
import com.bossymr.rapid.language.builder.RapidBuilder;
import com.bossymr.rapid.language.builder.RapidCodeBlockBuilder;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlow;
import com.bossymr.rapid.language.flow.ControlFlowService;
import com.bossymr.rapid.language.flow.builder.ControlFlowBuilder;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.debug.ControlFlowFormatVisitor;
import com.bossymr.rapid.language.flow.debug.DataFlowGraphService;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.value.BinaryOperator;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.flow.value.UnaryOperator;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

class DataFlowGraphTest {

    private static final int MAX_PASSES = -1;
    private static final boolean DRAW_EACH_PASS = false;
    private static final boolean DRAW_FINAL_PASS = true;

    private void check(@NotNull TestInfo testInfo, @NotNull Consumer<RapidBuilder> consumer) throws IOException, ExecutionException {
        ControlFlowBuilder builder = new ControlFlowBuilder();
        consumer.accept(builder);
        ControlFlow controlFlow = builder.getControlFlow();
        String name = testInfo.getTestMethod().orElseThrow().getName();
        File outputDirectory = Path.of(System.getProperty("user.home"), "graph", name).toFile();
        Path path = outputDirectory.toPath();
        if (DRAW_FINAL_PASS || DRAW_EACH_PASS) {
            if (outputDirectory.exists()) {
                FileUtil.delete(outputDirectory);
            }
            if (!(outputDirectory.exists() || outputDirectory.mkdirs())) {
                throw new IOException("Could not create output folder");
            }
            String output = ControlFlowFormatVisitor.format(controlFlow);
            FileUtil.writeToFile(path.resolve("flow.txt").toFile(), output);
        }
        Map<Instruction, AtomicInteger> passes = new HashMap<>();
        AtomicInteger total = new AtomicInteger();
        DataFlow result = ControlFlowService.calculateDataFlow(controlFlow, (dataFlow, state) -> {
            if (!(DRAW_EACH_PASS) && MAX_PASSES < 0) {
                return true;
            }
            DataFlowBlock block = state.getBlock();
            Instruction instruction = block.getInstruction();
            Block functionBlock = instruction.getBlock();
            passes.computeIfAbsent(instruction, key -> new AtomicInteger());
            int pass = passes.get(instruction).getAndIncrement();
            if (DRAW_EACH_PASS) {
                File outputFile = path.resolve(total.getAndIncrement() + "_" + functionBlock.getModuleName() + "-" + functionBlock.getName() + "_Pass_#" + pass + "_Block_#" + block.getInstruction().getIndex() + ".svg").toFile();
                try {
                    DataFlowGraphService.convert(outputFile, dataFlow);
                } catch (IOException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            if (MAX_PASSES < 0) {
                return true;
            }
            return pass <= MAX_PASSES;
        });
        if (DRAW_FINAL_PASS) {
            File outputFile = path.resolve("complete.svg").toFile();
            DataFlowGraphService.convert(outputFile, result);
        }
    }

    @Test
    void functionCall(TestInfo testInfo) throws IOException, ExecutionException {
        /*
         * MODULE foo
         *      PROC bar()
         *          VAR num variable := 0;
         *          variable := Abs(-1);
         *          IF <warning descr="Value of expression is always true">variable = 1</warning> THEN
         *          ENDIF
         *      ENDPROC
         *
         *      FUNC num Abs(num value)
         *          IF value >= 0 THEN
         *              return value;
         *          ELSE
         *              return -value;
         *          ENDIF
         *      ENDFUNC
         *  ENDMODULE
         */
        check(testInfo, builder -> builder
                .withModule("foo", moduleBuilder -> moduleBuilder
                        .withProcedure("bar", routineBuilder -> routineBuilder
                                .withCode(codeBuilder -> {
                                    ReferenceExpression reference = codeBuilder.getReference(codeBuilder.createVariable("variable", RapidPrimitiveType.NUMBER));
                                    codeBuilder.assign(reference, codeBuilder.literal(0))
                                               .assign(reference, codeBuilder.call("foo:Abs", RapidPrimitiveType.NUMBER, argumentBuilder -> argumentBuilder
                                                       .withRequiredArgument(codeBuilder.literal(-1))))
                                               .ifThen(codeBuilder.binary(BinaryOperator.EQUAL_TO, reference, codeBuilder.literal(1)),
                                                       thenBuilder -> {});
                                }))
                        .withFunction("Abs", RapidPrimitiveType.NUMBER, routineBuilder -> routineBuilder
                                .withParameterGroup(false, parameterGroupBuilder -> parameterGroupBuilder
                                        .withParameter("value", ParameterType.INPUT, RapidPrimitiveType.NUMBER))
                                .withCode(codeBuilder -> {
                                    ReferenceExpression argument = codeBuilder.getReference(Objects.requireNonNull(codeBuilder.getArgument("value")));
                                    codeBuilder.ifThenElse(codeBuilder.binary(BinaryOperator.GREATER_THAN_OR_EQUAL, argument, codeBuilder.literal(0)),
                                            thenBuilder -> thenBuilder.returnValue(argument),
                                            elseBuilder -> elseBuilder.returnValue(elseBuilder.unary(UnaryOperator.NEGATE, argument)));
                                }))));
    }

    @Test
    void infiniteLoop(TestInfo testInfo) throws IOException, ExecutionException {
        check(testInfo, builder -> builder
                .withModule("foo", moduleBuilder -> moduleBuilder
                        .withProcedure("bar", routineBuilder -> routineBuilder
                                .withCode(codeBuilder -> codeBuilder.whileLoop(codeBuilder.literal(true), loopBuilder -> {
                                    ReferenceExpression stepVariable = loopBuilder.getReference(loopBuilder.createVariable(RapidPrimitiveType.NUMBER));
                                    ReferenceExpression indexVariable = loopBuilder.getReference(loopBuilder.createVariable(RapidPrimitiveType.NUMBER));
                                    loopBuilder.assign(indexVariable, codeBuilder.literal(1));
                                    loopBuilder.ifThenElse(loopBuilder.binary(BinaryOperator.LESS_THAN, indexVariable, loopBuilder.literal(3)),
                                            thenBuilder -> thenBuilder.assign(stepVariable, codeBuilder.literal(1)),
                                            elseBuilder -> elseBuilder.assign(stepVariable, codeBuilder.literal(-1)));
                                    Label label = loopBuilder.createLabel();
                                    ReferenceExpression breakVariable = loopBuilder.getReference(loopBuilder.createVariable(RapidPrimitiveType.BOOLEAN));
                                    loopBuilder.ifThenElse(loopBuilder.binary(BinaryOperator.LESS_THAN, stepVariable, loopBuilder.literal(0)),
                                            thenBuilder -> thenBuilder.assign(breakVariable, thenBuilder.binary(BinaryOperator.GREATER_THAN, indexVariable, thenBuilder.literal(3))),
                                            elseBuilder -> elseBuilder.assign(breakVariable, elseBuilder.binary(BinaryOperator.LESS_THAN, indexVariable, elseBuilder.literal(3))));
                                    loopBuilder.ifThen(breakVariable,
                                            innerBuilder -> {
                                                innerBuilder.assign(indexVariable, innerBuilder.binary(BinaryOperator.ADD, indexVariable, stepVariable));
                                                innerBuilder.goTo(label);
                                            });
                                })))));
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Test
    void mutuallyExclusiveArgument(TestInfo testInfo) throws IOException, ExecutionException {
        /*
         *  MODULE foo
         *      PROC bar(\num x | num y)
         *          VAR num z := 0;
         *          IF Present(x) THEN
         *              z := y + x;
         *          ELSE
         *               z := y - x;
         *          ENDIF
         *      ENDPROC
         *  ENDMODULE
         *
         */
        check(testInfo, builder -> builder
                .withModule("foo", moduleBuilder -> moduleBuilder
                        .withProcedure("bar", routineBuilder -> routineBuilder
                                .withParameterGroup(true, parameterGroupBuilder -> parameterGroupBuilder
                                        .withParameter("x", ParameterType.INPUT, RapidPrimitiveType.NUMBER)
                                        .withParameter("y", ParameterType.INPUT, RapidPrimitiveType.NUMBER))
                                .withCode(codeBuilder -> {
                                    ReferenceExpression x = codeBuilder.getReference(Objects.requireNonNull(codeBuilder.getArgument("x")));
                                    ReferenceExpression y = codeBuilder.getReference(Objects.requireNonNull(codeBuilder.getArgument("y")));
                                    ReferenceExpression z = codeBuilder.getReference(codeBuilder.createVariable("z", RapidPrimitiveType.NUMBER));
                                    Expression expression = codeBuilder.call(":Present", RapidPrimitiveType.BOOLEAN, argumentBuilder -> argumentBuilder
                                            .withRequiredArgument(x));
                                    codeBuilder.ifThenElse(expression,
                                            thenConsumer -> thenConsumer
                                                    .assign(z, thenConsumer.binary(BinaryOperator.ADD, y, x))
                                            , elseConsumer -> elseConsumer
                                                    .assign(z, elseConsumer.binary(BinaryOperator.SUBTRACT, y, x)));
                                }))));
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Test
    void missingVariable(TestInfo testInfo) throws IOException, ExecutionException {
        /*
         *  MODULE foo
         *      PROC bar(\num x)
         *          VAR num y := 5;
         *          VAR num z := 0;
         *          IF Present(x) THEN
         *              y := y + x;
         *          ELSE
         *              y := y - <warning descr="Variable 'x' is always missing">x</warning>;
         *              z := x;
         *          ENDIF
         *      ENDPROC
         *  ENDMODULE
         */
        check(testInfo, builder -> builder
                .withModule("foo", moduleBuilder -> moduleBuilder
                        .withProcedure("bar", routineBuilder -> routineBuilder
                                .withParameterGroup(true, parameterGroupBuilder -> parameterGroupBuilder
                                        .withParameter("x", ParameterType.INPUT, RapidPrimitiveType.NUMBER))
                                .withCode(codeBuilder -> {
                                    ReferenceExpression x = codeBuilder.getReference(Objects.requireNonNull(codeBuilder.getArgument("x")));
                                    ReferenceExpression y = codeBuilder.getReference(codeBuilder.createVariable("y", RapidPrimitiveType.NUMBER));
                                    ReferenceExpression z = codeBuilder.getReference(codeBuilder.createVariable("z", RapidPrimitiveType.NUMBER));
                                    codeBuilder.assign(y, codeBuilder.literal(5))
                                               .assign(z, codeBuilder.literal(0));
                                    Expression expression = codeBuilder.call(":Present", RapidPrimitiveType.BOOLEAN, argumentBuilder -> argumentBuilder
                                            .withRequiredArgument(x));
                                    codeBuilder.ifThenElse(expression,
                                            thenConsumer -> thenConsumer
                                                    .assign(y, thenConsumer.binary(BinaryOperator.ADD, y, x))
                                            , elseConsumer -> elseConsumer
                                                    .assign(y, elseConsumer.binary(BinaryOperator.SUBTRACT, y, x))
                                                    .assign(z, x));
                                }))));
    }

    @Test
    void loop1(TestInfo testInfo) throws IOException, ExecutionException {
        /*
         *  MODULE foo
         *      PROC bar(num n)
         *          VAR num i := 3;
         *          WHILE i < n DO
         *               i := i + 4
         *          ENDWHILE
         *          IF i = 15 THEN
         *              RETURN;
         *          ELSE
         *              RETURN;
         *          ENDIF
         *      ENDPROC
         *  ENDMODULE
         */
        check(testInfo, builder -> builder
                .withModule("foo", moduleBuilder -> moduleBuilder
                        .withProcedure("bar", routineBuilder -> {
                            routineBuilder.withParameterGroup(false, parameterGroupBuilder -> parameterGroupBuilder
                                    .withParameter("n", ParameterType.INPUT, RapidPrimitiveType.NUMBER));
                            routineBuilder.withCode(codeBuilder -> {
                                ReferenceExpression i = codeBuilder.getReference(codeBuilder.createVariable("i", RapidPrimitiveType.NUMBER));
                                ReferenceExpression n = codeBuilder.getReference(Objects.requireNonNull(codeBuilder.getArgument("n")));
                                codeBuilder.assign(i, codeBuilder.literal(3));
                                codeBuilder.whileLoop(codeBuilder.binary(BinaryOperator.LESS_THAN, i, n), loopBuilder -> loopBuilder
                                                   .assign(i, loopBuilder.binary(BinaryOperator.ADD, i, loopBuilder.literal(4))))
                                           .ifThenElse(codeBuilder.binary(BinaryOperator.EQUAL_TO, i, codeBuilder.literal(15)),
                                                   RapidCodeBlockBuilder::returnValue, RapidCodeBlockBuilder::returnValue);
                            });
                        })));
    }

    @Test
    void loop2(TestInfo testInfo) throws IOException, ExecutionException {
        /*
         *  MODULE foo
         *      FUNC num bar(num{*} A, num n, num x)
         *          VAR num i;
         *          WHILE i < n DO
         *              IF A{i} = x THEN
         *                  RETURN i;
         *              ELSE
         *                  i := i + 1;
         *              ENDIF
         *          ENDWHILE
         *          RETURN -1;
         *      ENDFUNC
         *  ENDMODULE
         */
        check(testInfo, builder -> builder
                .withModule("foo", moduleBuilder -> moduleBuilder
                        .withFunction("bar", RapidPrimitiveType.NUMBER, routineBuilder -> routineBuilder
                                .withParameterGroup(false, parameterGroupBuilder -> parameterGroupBuilder
                                        .withParameter("A", ParameterType.INPUT, RapidPrimitiveType.NUMBER.createArrayType(1)))
                                .withParameterGroup(false, parameterGroupBuilder -> parameterGroupBuilder
                                        .withParameter("n", ParameterType.INPUT, RapidPrimitiveType.NUMBER))
                                .withParameterGroup(false, parameterGroupBuilder -> parameterGroupBuilder
                                        .withParameter("x", ParameterType.INPUT, RapidPrimitiveType.NUMBER)).withCode(codeBuilder -> {
                                    ReferenceExpression i = codeBuilder.getReference(codeBuilder.createVariable("i", RapidPrimitiveType.NUMBER));
                                    ReferenceExpression n = codeBuilder.getReference(Objects.requireNonNull(codeBuilder.getArgument("n")));
                                    ReferenceExpression a = codeBuilder.getReference(Objects.requireNonNull(codeBuilder.getArgument("A")));
                                    ReferenceExpression x = codeBuilder.getReference(Objects.requireNonNull(codeBuilder.getArgument("x")));
                                    codeBuilder.whileLoop(codeBuilder.binary(BinaryOperator.LESS_THAN, i, n), blockBuilder -> {
                                        ReferenceExpression index = codeBuilder.index(a, i);
                                        blockBuilder.ifThenElse(blockBuilder.binary(BinaryOperator.EQUAL_TO, index, x),
                                                thenConsumer -> thenConsumer
                                                        .returnValue(i),
                                                elseConsumer -> elseConsumer
                                                        .assign(i, elseConsumer.binary(BinaryOperator.ADD, i, elseConsumer.literal(1))));
                                    }).returnValue(codeBuilder.literal(-1));
                                }))));
    }

    @Test
    void array(TestInfo testInfo) throws IOException, ExecutionException {
        /*
         *  MODULE foo
         *      PROC bar(num x)
         *          VAR num variable{2, 3} := [[0, 1, 2], [3, 4, 5]];
         *          IF <warning descr="Value of expression is always true">(variable{1, 3} * variable{2, 2}) = 8</warning> THEN
         *          ENDIF
         *      ENDPROC
         *  ENDMODULE
         */
        check(testInfo, builder -> builder
                .withModule("foo", moduleBuilder -> moduleBuilder
                        .withProcedure("bar", routineBuilder -> routineBuilder
                                .withParameterGroup(false, parameterGroupBuilder -> parameterGroupBuilder
                                        .withParameter("x", ParameterType.INPUT, RapidPrimitiveType.NUMBER))
                                .withCode(codeBuilder -> {
                                    RapidType arrayType = RapidPrimitiveType.NUMBER.createArrayType(2);
                                    RapidType componentType = RapidPrimitiveType.NUMBER.createArrayType(1);
                                    ReferenceExpression variable = codeBuilder.getReference(codeBuilder.createVariable("variable", arrayType));
                                    Expression aggregate1 = codeBuilder.aggregate(componentType,
                                            List.of(codeBuilder.literal(0), codeBuilder.literal(1), codeBuilder.literal(2)));
                                    Expression aggregate2 = codeBuilder.aggregate(componentType,
                                            List.of(codeBuilder.literal(3), codeBuilder.literal(4), codeBuilder.literal(5)));
                                    List<Expression> expressions = List.of(aggregate1, aggregate2);
                                    codeBuilder.assign(variable, codeBuilder.aggregate(arrayType, expressions));
                                    Expression multiply = codeBuilder.binary(BinaryOperator.MULTIPLY,
                                            codeBuilder.index(codeBuilder.index(variable, codeBuilder.literal(1)), codeBuilder.literal(3)),
                                            codeBuilder.index(codeBuilder.index(variable, codeBuilder.literal(2)), codeBuilder.literal(2)));
                                    codeBuilder.ifThen(codeBuilder.binary(BinaryOperator.EQUAL_TO, multiply, codeBuilder.literal(8)), thenBuilder -> {});
                                }))));
    }
}

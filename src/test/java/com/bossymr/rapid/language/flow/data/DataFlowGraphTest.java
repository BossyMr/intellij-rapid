package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.builder.RapidBuilder;
import com.bossymr.rapid.language.builder.RapidCodeBuilder;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlow;
import com.bossymr.rapid.language.flow.ControlFlowService;
import com.bossymr.rapid.language.flow.builder.ControlFlowBuilder;
import com.bossymr.rapid.language.flow.debug.ControlFlowFormatVisitor;
import com.bossymr.rapid.language.flow.debug.DataFlowGraphService;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.value.BinaryOperator;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.IndexExpression;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

class DataFlowGraphTest {

    private static final int MAX_PASSES = -1;
    private static final boolean DRAW_EACH_PASS = false;

    private void check(@NotNull TestInfo testInfo, @NotNull Consumer<RapidBuilder> consumer) throws IOException, ExecutionException {
        ControlFlowBuilder builder = new ControlFlowBuilder();
        consumer.accept(builder);
        ControlFlow controlFlow = builder.getControlFlow();
        String name = testInfo.getTestMethod().orElseThrow().getName();
        File outputDirectory = Path.of(System.getProperty("user.home"), "graph", name).toFile();
        if (outputDirectory.exists()) {
            FileUtil.delete(outputDirectory);
        }
        if (!(outputDirectory.exists() || outputDirectory.mkdirs())) {
            throw new IOException("Could not create output folder");
        }
        Map<Instruction, AtomicInteger> passes = new HashMap<>();
        String output = ControlFlowFormatVisitor.format(controlFlow);
        Path path = outputDirectory.toPath();
        FileUtil.writeToFile(path.resolve("flow.txt").toFile(), output);
        AtomicInteger total = new AtomicInteger();
        DataFlow result = ControlFlowService.getDataFlow(controlFlow, (dataFlow, block) -> {
            Instruction instruction = block.getInstruction();
            Block functionBlock = instruction.getBlock();
            passes.computeIfAbsent(instruction, key -> new AtomicInteger());
            int pass = passes.get(instruction).getAndIncrement();
            if (DRAW_EACH_PASS) {
                File outputFile = path.resolve(total.getAndIncrement() + " " + functionBlock.getModuleName() + "-" + functionBlock.getName() + " Pass #" + pass + " Block #" + block.getInstruction().getIndex() + ".svg").toFile();
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
        File outputFile = path.resolve("complete.svg").toFile();
        DataFlowGraphService.convert(outputFile, result);
    }

    @Test
    void testLoop1(TestInfo testInfo) throws IOException, ExecutionException {
        check(testInfo, builder -> {
            builder.withModule("foo", moduleBuilder -> {
                moduleBuilder.withProcedure("bar", routineBuilder -> {
                    routineBuilder.withParameterGroup(false, parameterGroupBuilder -> {
                        parameterGroupBuilder.withParameter("n", ParameterType.INPUT, RapidPrimitiveType.NUMBER);
                    });
                    routineBuilder.withCode(codeBuilder -> {
                        ReferenceExpression i = codeBuilder.createVariable("i", RapidPrimitiveType.NUMBER);
                        ReferenceExpression n = codeBuilder.getArgument("n");
                        codeBuilder.assign(i, codeBuilder.literal(3));
                        codeBuilder.loop(codeBuilder.binary(BinaryOperator.LESS_THAN, i, n), loopBuilder -> {
                                       loopBuilder.assign(i, loopBuilder.binary(BinaryOperator.ADD, i, loopBuilder.literal(4)));
                                   })
                                   .ifThenElse(codeBuilder.binary(BinaryOperator.EQUAL_TO, i, codeBuilder.literal(15)),
                                           codeBlockBuilder -> {
                                               Expression lower = codeBlockBuilder.binary(BinaryOperator.GREATER_THAN_OR_EQUAL, n, codeBlockBuilder.literal(12));
                                               Expression upper = codeBlockBuilder.binary(BinaryOperator.LESS_THAN_OR_EQUAL, n, codeBlockBuilder.literal(15));
                                               Expression binary = codeBlockBuilder.binary(BinaryOperator.AND, lower, upper);
                                               codeBlockBuilder.ifThenElse(binary, RapidCodeBuilder::returnValue, RapidCodeBuilder::returnValue);
                                           },
                                           RapidCodeBuilder::returnValue);
                    });
                });
            });
        });
    }

    @Test
    void testLoop2(TestInfo testInfo) throws IOException, ExecutionException {
        check(testInfo, builder -> {
            builder.withModule("foo", moduleBuilder -> {
                moduleBuilder.withFunction("bar", RapidPrimitiveType.NUMBER, routineBuilder -> {
                    routineBuilder.withParameterGroup(false, parameterGroupBuilder -> {
                        parameterGroupBuilder.withParameter("A", ParameterType.INPUT, RapidPrimitiveType.NUMBER.createArrayType(1));
                    }).withParameterGroup(false, parameterGroupBuilder -> {
                        parameterGroupBuilder.withParameter("n", ParameterType.INPUT, RapidPrimitiveType.NUMBER);
                    }).withParameterGroup(false, parameterGroupBuilder -> {
                        parameterGroupBuilder.withParameter("x", ParameterType.INPUT, RapidPrimitiveType.NUMBER);
                    }).withCode(codeBuilder -> {
                        ReferenceExpression i = codeBuilder.createVariable("i", RapidPrimitiveType.NUMBER);
                        ReferenceExpression n = codeBuilder.getArgument("n");
                        ReferenceExpression a = codeBuilder.getArgument("A");
                        ReferenceExpression x = codeBuilder.getArgument("x");
                        codeBuilder.loop(codeBuilder.binary(BinaryOperator.LESS_THAN, i, n), blockBuilder -> {
                            IndexExpression index = codeBuilder.index(a, i);
                            blockBuilder.ifThenElse(blockBuilder.binary(BinaryOperator.EQUAL_TO, index, x),
                                    thenConsumer -> {
                                        thenConsumer.returnValue(i);
                                    }, elseConsumer -> {
                                        elseConsumer.assign(i, elseConsumer.binary(BinaryOperator.ADD, i, elseConsumer.literal(1)));
                                    });
                        }).returnValue(codeBuilder.literal(-1));
                    });
                });
            });
        });
    }
}

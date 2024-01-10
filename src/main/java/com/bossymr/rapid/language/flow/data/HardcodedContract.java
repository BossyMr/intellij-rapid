package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.builder.RapidModuleBuilder;
import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.builder.ControlFlowBuilder;
import com.bossymr.rapid.language.flow.expression.Expression;
import com.bossymr.rapid.language.flow.expression.ReferenceExpression;
import com.bossymr.rapid.language.flow.expression.UnaryOperator;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.virtual.VirtualRoutine;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public enum HardcodedContract {

    PRESENT(builder -> builder
            .withRoutine("Present", RoutineType.FUNCTION, RapidPrimitiveType.BOOLEAN, routineBuilder -> routineBuilder
                    .withParameterGroup(false, parameterGroupBuilder -> parameterGroupBuilder
                            .withParameter("OptPar", ParameterType.REFERENCE, RapidPrimitiveType.ANYTYPE))
                    .withCode(codeBuilder -> {
                        Argument argument = Objects.requireNonNull(codeBuilder.getArgument("OptPar"));
                        codeBuilder.returnValue(codeBuilder.unary(UnaryOperator.PRESENT, codeBuilder.getReference(argument)));
                    }))),
    DIM(builder -> builder
            .withRoutine("Dim", RoutineType.FUNCTION, RapidPrimitiveType.NUMBER, routineBuilder -> routineBuilder
                    .withParameterGroup(false, parameterGroupBuilder -> parameterGroupBuilder
                            .withParameter("ArrPar", ParameterType.REFERENCE, RapidPrimitiveType.ANYTYPE)
                            .withParameter("DimNo", ParameterType.INPUT, RapidPrimitiveType.NUMBER))
                    .withCode(codeBuilder -> {
                        ReferenceExpression ArrPar = codeBuilder.getReference(codeBuilder.getArgument("ArrPar"));
                        ReferenceExpression DimNo = codeBuilder.getReference(codeBuilder.getArgument("DimNo"));
                        codeBuilder.test(DimNo, testBuilder -> testBuilder
                                .withCase(List.of(codeBuilder.literal(1)), caseBuilder -> {
                                    Expression expression = caseBuilder.unary(UnaryOperator.DIMENSION, ArrPar);
                                    caseBuilder.returnValue(expression);
                                })
                                .withCase(List.of(codeBuilder.literal(2)), caseBuilder -> {
                                    ReferenceExpression index = caseBuilder.index(ArrPar, caseBuilder.literal(1));
                                    Expression expression = caseBuilder.unary(UnaryOperator.DIMENSION, index);
                                    caseBuilder.returnValue(expression);
                                })
                                .withCase(List.of(codeBuilder.literal(3)), caseBuilder -> {
                                    ReferenceExpression index = caseBuilder.index(caseBuilder.index(ArrPar, caseBuilder.literal(1)), caseBuilder.literal(1));
                                    Expression expression = caseBuilder.unary(UnaryOperator.DIMENSION, index);
                                    caseBuilder.returnValue(expression);
                                })
                                .withDefaultCase(caseBuilder -> caseBuilder.raise(caseBuilder.literal(40241))));
                    })));

    private final @NotNull VirtualRoutine routine;
    private final @NotNull Block block;

    HardcodedContract(@NotNull Consumer<RapidModuleBuilder> consumer) {
        ControlFlowBuilder builder = new ControlFlowBuilder();
        builder.withModule("", consumer);
        Set<Block> controlFlow = builder.getControlFlow();
        if (controlFlow.size() != 1) {
            throw new IllegalArgumentException();
        }
        this.block = controlFlow.iterator().next();
        this.routine = (VirtualRoutine) block.getElement();
    }

    public @NotNull VirtualRoutine getRoutine() {
        return routine;
    }

    public @NotNull Block getBlock() {
        return block;
    }
}

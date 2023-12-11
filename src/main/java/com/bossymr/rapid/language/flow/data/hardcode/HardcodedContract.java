package com.bossymr.rapid.language.flow.data.hardcode;

import com.bossymr.rapid.language.builder.RapidModuleBuilder;
import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.ControlFlow;
import com.bossymr.rapid.language.flow.builder.ControlFlowBuilder;
import com.bossymr.rapid.language.flow.value.UnaryOperator;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public enum HardcodedContract {

    PRESENT(builder -> builder
            .withRoutine("Present", RoutineType.FUNCTION, RapidPrimitiveType.BOOLEAN, routineBuilder -> routineBuilder
                    .withParameterGroup(false, parameterGroupBuilder -> parameterGroupBuilder
                            .withParameter("OptPar", ParameterType.REFERENCE, RapidPrimitiveType.ANYTYPE))
                    .withCode(codeBuilder -> {
                        Argument argument = Objects.requireNonNull(codeBuilder.getArgument("OptPar"));
                        codeBuilder.returnValue(codeBuilder.unary(UnaryOperator.PRESENT, codeBuilder.getReference(argument)));
                    })));

    private final @NotNull Consumer<RapidModuleBuilder> consumer;

    HardcodedContract(@NotNull Consumer<RapidModuleBuilder> consumer) {
        this.consumer = consumer;
    }

    public static @NotNull ControlFlow getControlFlow() {
        return new ControlFlowBuilder()
                .withModule("", builder -> {
                    for (HardcodedContract value : HardcodedContract.values()) {
                        value.getConsumer().accept(builder);
                    }
                })
                .getControlFlow();
    }

    public @NotNull Consumer<RapidModuleBuilder> getConsumer() {
        return consumer;
    }
}

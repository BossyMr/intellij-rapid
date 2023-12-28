package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.RapidBuilder;
import com.bossymr.rapid.language.builder.RapidModuleBuilder;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.BlockDescriptor;
import com.bossymr.rapid.language.symbol.RapidField;
import com.bossymr.rapid.language.symbol.RapidModule;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class ControlFlowBuilder implements RapidBuilder {

    private final @NotNull Map<BlockDescriptor, Block> controlFlow;

    public ControlFlowBuilder() {
        this.controlFlow = new HashMap<>();
    }

    @Override
    public @NotNull ControlFlowBuilder withModule(@NotNull String name, @NotNull Consumer<RapidModuleBuilder> consumer) {
        ControlFlowModuleBuilder builder = new ControlFlowModuleBuilder(name, controlFlow);
        consumer.accept(builder);
        return this;
    }

    @Override
    public @NotNull RapidBuilder withModule(@NotNull RapidModule module) {
        String name = Objects.requireNonNullElse(module.getName(), RapidSymbol.getDefaultText());
        ControlFlowModuleBuilder builder = new ControlFlowModuleBuilder(name, controlFlow);
        for (RapidField field : module.getFields()) {
            builder.withField(field);
        }
        for (RapidRoutine routine : module.getRoutines()) {
            builder.withRoutine(routine);
        }
        return this;
    }

    public @NotNull Set<Block> getControlFlow() {
        return new HashSet<>(controlFlow.values());
    }
}

package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.RapidCodeBuilder;
import com.bossymr.rapid.language.builder.RapidFieldBuilder;
import com.bossymr.rapid.language.flow.Block;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ControlFlowFieldBuilder implements RapidFieldBuilder {

    private final @NotNull Block.FieldBlock block;

    public ControlFlowFieldBuilder(@NotNull Block.FieldBlock block) {
        this.block = block;
    }

    @Override
    public @NotNull RapidFieldBuilder withInitializer(@NotNull Consumer<RapidCodeBuilder> consumer) {
        ControlFlowBlockBuilder builder = new ControlFlowBlockBuilder(block);
        consumer.accept(new ControlFlowCodeBuilder(block, builder));
        if(builder.isInScope()) {
            throw new IllegalArgumentException();
        }
        return this;
    }
}

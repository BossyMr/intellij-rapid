package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.RapidParameterGroupBuilder;
import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.ArgumentGroup;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.virtual.VirtualParameter;
import com.bossymr.rapid.language.symbol.virtual.VirtualParameterGroup;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

public class ControlFlowParameterGroupBuilder implements RapidParameterGroupBuilder {

    private final @NotNull Block block;
    private final @NotNull VirtualParameterGroup parameterGroup;
    private final @NotNull ArgumentGroup argumentGroup;

    public ControlFlowParameterGroupBuilder(@NotNull Block block, @NotNull VirtualParameterGroup parameterGroup, @NotNull ArgumentGroup argumentGroup) {
        this.block = block;
        this.parameterGroup = parameterGroup;
        this.argumentGroup = argumentGroup;
    }

    @Override
    public @NotNull RapidParameterGroupBuilder withParameter(@NotNull String name, @NotNull ParameterType parameterType, @NotNull RapidType valueType) {
        VirtualParameter parameter = new VirtualParameter(parameterGroup, parameterType, name, valueType);
        Argument argument = block.createArgument(name, valueType, parameterType);
        argumentGroup.arguments().add(argument);
        parameterGroup.getParameters().add(parameter);
        return this;
    }
}

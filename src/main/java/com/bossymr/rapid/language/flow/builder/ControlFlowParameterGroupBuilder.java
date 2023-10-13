package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.RapidParameterGroupBuilder;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.virtual.VirtualParameter;
import com.bossymr.rapid.language.symbol.virtual.VirtualParameterGroup;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

public class ControlFlowParameterGroupBuilder implements RapidParameterGroupBuilder {

    private final @NotNull VirtualParameterGroup parameterGroup;

    public ControlFlowParameterGroupBuilder(@NotNull VirtualParameterGroup parameterGroup) {
        this.parameterGroup = parameterGroup;
    }

    @Override
    public @NotNull RapidParameterGroupBuilder withParameter(@NotNull String name, @NotNull ParameterType parameterType, @NotNull RapidType valueType) {
        VirtualParameter parameter = new VirtualParameter(parameterGroup, parameterType, name, valueType);
        parameterGroup.getParameters().add(parameter);
        return this;
    }
}

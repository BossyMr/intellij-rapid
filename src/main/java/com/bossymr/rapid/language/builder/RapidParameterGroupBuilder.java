package com.bossymr.rapid.language.builder;

import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

public interface RapidParameterGroupBuilder {

    @NotNull RapidParameterGroupBuilder withParameter(@NotNull String name,
                                                      @NotNull ParameterType parameterType,
                                                      @NotNull RapidType valueType);
}

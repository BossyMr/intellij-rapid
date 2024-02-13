package com.bossymr.rapid.language.builder;

import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

/**
 * A builder for {@code Rapid} parameter groups.
 */
public interface RapidParameterGroupBuilder {

    /**
     * Adds a parameter to this parameter group.
     *
     * @param name the name of the parameter.
     * @param parameterType the type of the parameter.
     * @param valueType the value type of the parameter.
     * @return this builder.
     */
    @NotNull RapidParameterGroupBuilder withParameter(@NotNull String name,
                                                      @NotNull ParameterType parameterType,
                                                      @NotNull RapidType valueType);
}

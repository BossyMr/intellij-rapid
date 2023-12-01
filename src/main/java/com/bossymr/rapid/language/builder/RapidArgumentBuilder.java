package com.bossymr.rapid.language.builder;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.value.Expression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A builder for a {@code Rapid} routine call.
 */
public interface RapidArgumentBuilder {

    /**
     * Adds a new required argument to this routine call.
     *
     * @param expression the value of the argument.
     * @return this builder.
     */
    @NotNull RapidArgumentBuilder withRequiredArgument(@NotNull Expression expression);

    /**
     * Adds a new optional argument to this routine call.
     *
     * @param name the name of the argument.
     * @param expression the value of the argument.
     * @return this builder.
     */
    @NotNull RapidArgumentBuilder withOptionalArgument(@NotNull String name, @NotNull Expression expression);

    /**
     * Adds a new conditional argument to this routine call.
     *
     * @param name the name of the argument.
     * @param argument the value of the argument.
     * @return this builder.
     */
    @NotNull RapidArgumentBuilder withConditionalArgument(@NotNull String name, @NotNull Argument argument);

}

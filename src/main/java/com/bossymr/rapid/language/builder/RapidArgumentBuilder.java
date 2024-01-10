package com.bossymr.rapid.language.builder;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.expression.Expression;
import org.jetbrains.annotations.NotNull;

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
     * @param name the name of the parameter which this argument refers to.
     * @param expression the value of the argument.
     * @return this builder.
     */
    @NotNull RapidArgumentBuilder withOptionalArgument(@NotNull String name, @NotNull Expression expression);

    /**
     * Adds a new conditional argument to this routine call.
     *
     * @param name the name of the parameter which this argument refers to.
     * @param argument the argument, if this argument is present this argument is added to the function call, otherwise
     * it is left out.
     * @return this builder.
     */
    @NotNull RapidArgumentBuilder withConditionalArgument(@NotNull String name, @NotNull Argument argument);

}

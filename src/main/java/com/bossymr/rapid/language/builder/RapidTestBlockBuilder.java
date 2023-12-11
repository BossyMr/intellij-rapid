package com.bossymr.rapid.language.builder;

import com.bossymr.rapid.language.flow.value.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * A builder for a {@code Rapid} test block.
 */
public interface RapidTestBlockBuilder {

    /**
     * Adds a new test case to this test block.
     *
     * @param conditions the conditions.
     * @param consumer the handler which can define the body of the test case.
     * @return this builder.
     */
    @NotNull RapidTestBlockBuilder withCase(@NotNull List<Expression> conditions,
                                            @NotNull Consumer<RapidCodeBlockBuilder> consumer);

    /**
     * Adds a new default case to this test block.
     *
     * @param consumer the handler which can define the body of the test case.
     * @return this builder.
     */
    @NotNull RapidTestBlockBuilder withDefaultCase(@NotNull Consumer<RapidCodeBlockBuilder> consumer);

}

package com.bossymr.rapid.language.builder;

import com.bossymr.rapid.language.psi.BlockType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * A builder for {@code Rapid} routines.
 */
public interface RapidRoutineBuilder {

    /**
     * Adds a parameter group to this routine.
     *
     * @param isOptional whether the parameter group is optional.
     * @param consumer the handler which can define the parameter group.
     * @return this builder.
     */
    @NotNull RapidRoutineBuilder withParameterGroup(boolean isOptional, @NotNull Consumer<RapidParameterGroupBuilder> consumer);

    /**
     * Adds a code block to this routine.
     *
     * @param consumer the handler which can define the code block.
     * @return this builder.
     */
    default @NotNull RapidRoutineBuilder withCode(@NotNull Consumer<RapidCodeBlockBuilder> consumer) {
        return withCode(BlockType.STATEMENT_LIST, consumer);
    }

    /**
     * Adds a code block to this routine.
     *
     * @param codeType the type of the code block.
     * @param consumer the handler which can define the code block.
     * @return this builder.
     */
    @NotNull RapidRoutineBuilder withCode(@NotNull BlockType codeType,
                                          @NotNull Consumer<RapidCodeBlockBuilder> consumer);

    /**
     * Adds an error handler to this routine.
     *
     * @param exceptions the list of exceptions which this error handler can accept, or an empty list if this error
     * handler can accept any exceptions.
     * @param consumer the handler which can define the code block.
     * @return this builder.
     */
    @NotNull RapidRoutineBuilder withCode(@NotNull List<Integer> exceptions,
                                          @NotNull Consumer<RapidCodeBlockBuilder> consumer);
}

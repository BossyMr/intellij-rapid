package com.bossymr.rapid.language.flow.conditon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code Value} represents a value.
 */
public sealed interface Value {

    sealed interface Variable extends Value {

        /**
         * A {@code Variable} represents the value of a local variable.
         *
         * @param index the field.
         */
        record Local(int index) implements Variable {}

        record Field(@Nullable String moduleName, @NotNull String name) implements Variable {}

        record Index(@NotNull Value.Variable variable, Value index) implements Variable {}

    }

    /**
     * A {@code Constant} represents a constant value.
     *
     * @param value the value.
     */
    record Constant(@NotNull Object value) implements Value {}
}

package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code Value} represents a value.
 */
public sealed interface Value {

    void accept(@NotNull ControlFlowVisitor visitor);

    @Nullable RapidType type();

    sealed interface Variable extends Value {

        @Override
        @NotNull RapidType type();

        /**
         * A {@code Variable} represents the value of a local variable.
         *
         * @param index the field.
         */
        record Local(@NotNull RapidType type, int index) implements Variable {
            @Override
            public void accept(@NotNull ControlFlowVisitor visitor) {
                visitor.visitLocalVariableValue(this);
            }
        }

        record Field(@NotNull RapidType type, @Nullable String moduleName, @NotNull String name) implements Variable {
            @Override
            public void accept(@NotNull ControlFlowVisitor visitor) {
                visitor.visitFieldVariableValue(this);
            }
        }

        record Index(@NotNull RapidType type, @NotNull Value.Variable variable, Value index) implements Variable {
            @Override
            public void accept(@NotNull ControlFlowVisitor visitor) {
                visitor.visitIndexVariableValue(this);
            }
        }

        record Component(@NotNull RapidType type, @NotNull Value.Variable variable, @NotNull String component) implements Variable {
            @Override
            public void accept(@NotNull ControlFlowVisitor visitor) {
                visitor.visitComponentVariableValue(this);
            }
        }

    }

    /**
     * A {@code Constant} represents a constant value.
     *
     * @param value the value.
     */
    record Constant(@NotNull RapidType type, @NotNull Object value) implements Value {
        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
            visitor.visitConstantValue(this);
        }
    }

    record Error() implements Value {
        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
            visitor.visitErrorValue(this);
        }

        @Override
        public @Nullable RapidType type() {
            return null;
        }
    }
}

package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface VariableKey {

    static @NotNull VariableKey createField(@Nullable String name, @Nullable FieldType fieldType) {
        return new VariableKey() {

            private Variable variable;

            @Override
            public @NotNull Variable create(@NotNull Block currentBlock, @NotNull RapidType type, @Nullable Object initialValue) {
                Variable variable = new Variable(currentBlock.getNextIndex(), initialValue, fieldType, type, name);
                currentBlock.variables().put(variable.index(), variable);
                return this.variable = variable;
            }

            @Override
            public @NotNull Variable retrieve() {
                return Objects.requireNonNull(variable);
            }
        };
    }

    static @NotNull VariableKey createVariable() {
        return createField(null, null);
    }

    @NotNull Variable create(@NotNull Block currentBlock, @NotNull RapidType type, @Nullable Object initialValue);

    @NotNull Variable retrieve();

}

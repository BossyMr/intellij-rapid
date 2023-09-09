package com.bossymr.rapid.language.flow.parser;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.Variable;
import com.bossymr.rapid.language.flow.value.ReferenceValue;
import com.bossymr.rapid.language.flow.value.VariableValue;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface VariableKey {

    static @NotNull VariableKey createField(@Nullable String name, @Nullable FieldType fieldType) {
        return new VariableKey() {

            private ReferenceValue value;

            @Override
            public @NotNull ReferenceValue create(@NotNull Block currentBlock, @NotNull RapidType type) {
                Variable variable = currentBlock.createVariable(name, fieldType, type);
                return this.value = new VariableValue(variable);
            }

            @Override
            public @Nullable ReferenceValue retrieve() {
                return value;
            }
        };
    }

    static @NotNull VariableKey createVariable() {
        return createField(null, null);
    }

    static @NotNull VariableKey useVariable(@NotNull ReferenceValue variable) {
        return new VariableKey() {

            private @Nullable ReferenceValue referenceValue;

            @Override
            public @NotNull ReferenceValue create(@NotNull Block currentBlock, @NotNull RapidType type) {
                if (!(variable.getType().isAssignable(type))) {
                    return referenceValue = new VariableValue(currentBlock.createVariable(null, null, type));
                }
                return referenceValue = variable;
            }

            @Override
            public @Nullable ReferenceValue retrieve() {
                return referenceValue;
            }
        };
    }

    @NotNull ReferenceValue create(@NotNull Block currentBlock, @NotNull RapidType type);

    @Nullable ReferenceValue retrieve();

}

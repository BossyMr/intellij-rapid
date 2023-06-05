package com.bossymr.rapid.language.flow.parser;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.Variable;
import com.bossymr.rapid.language.flow.conditon.Value;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface VariableKey {

    static @NotNull VariableKey createField(@Nullable String name, @Nullable FieldType fieldType) {
        return new VariableKey() {

            private Value.Variable value;

            @Override
            public @NotNull Value.Variable create(@NotNull Block currentBlock, @NotNull RapidType type, @Nullable Object initialValue) {
                Variable variable = currentBlock.createVariable(name, fieldType, type, initialValue);
                return this.value = new Value.Variable.Local(type, variable.index());
            }

            @Override
            public @Nullable Value.Variable retrieve() {
                return value;
            }
        };
    }

    static @NotNull VariableKey createVariable() {
        return createField(null, null);
    }

    @NotNull Value.Variable create(@NotNull Block currentBlock, @NotNull RapidType type, @Nullable Object initialValue);

    @Nullable Value.Variable retrieve();

}

package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.Field;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code ReferenceValue} represents the value of a local variable.
 *
 * @param field the field.
 */
public record VariableValue(@NotNull Field field) implements ReferenceValue {
    @Override
    public @NotNull RapidType getType() {
        return field.type();
    }

    @Override
    public void accept(@NotNull ControlFlowVisitor visitor) {
        visitor.visitLocalVariableValue(this);
    }
}

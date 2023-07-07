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
public record VariableReference(@NotNull RapidType type, @NotNull Field field) implements ReferenceValue {
    @Override
    public <T> T accept(@NotNull ControlFlowVisitor<T> visitor) {
        return visitor.visitLocalVariableValue(this);
    }
}

package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.psi.RapidElement;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A {@code ReferenceValue} represents a variable.
 *
 * @param index the index of the variable.
 * @param element the element.
 * @param fieldType the type of the variable, or {@code null} if this variable is an intermediate variable.
 * @param type the value type of the variable.
 * @param name the name of the variable, or {@code null} if this variable is an intermediate variable.
 */
public record Variable(
        int index,
        @Nullable RapidElement element,
        @Nullable FieldType fieldType,
        @NotNull RapidType type,
        @Nullable String name
) implements Field {

    public void accept(@NotNull ControlFlowVisitor visitor) {
        visitor.visitVariable(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return index == variable.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }
}

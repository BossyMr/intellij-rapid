package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@code Argument} represents an argument in a routine.
 *
 * @param index the index of the variable in the routine.
 * @param parameterType the parameter type.
 * @param type the value type.
 * @param name the name of the parameter.
 */
public record Argument(
        int index,
        @NotNull ParameterType parameterType,
        @NotNull RapidType type,
        @NotNull String name
) implements Field {

    public void accept(@NotNull ControlFlowVisitor visitor) {
        visitor.visitArgument(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Argument argument = (Argument) o;
        return index == argument.index && parameterType == argument.parameterType && Objects.equals(type, argument.type) && Objects.equals(name, argument.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, parameterType, type, name);
    }
}

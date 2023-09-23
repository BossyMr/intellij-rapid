package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Argument implements Field {
    private final int index;
    private final @NotNull ParameterType parameterType;
    private final @NotNull RapidType type;
    private final @NotNull String name;

    public Argument(int index, @NotNull ParameterType parameterType, @NotNull RapidType type, @NotNull String name) {
        this.index = index;
        this.parameterType = parameterType;
        this.type = type;
        this.name = name;
    }

    public @Nullable ArgumentGroup getArgumentGroup(@NotNull Block block) {
        for (ArgumentGroup argumentGroup : block.getArgumentGroups()) {
            if (argumentGroup.arguments().contains(this)) {
                return argumentGroup;
            }
        }
        return null;
    }

    @Override
    public int getIndex() {
        return index;
    }

    public @NotNull ParameterType getParameterType() {
        return parameterType;
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitArgument(this);
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

    @Override
    public String toString() {
        return "Argument{" +
                "index=" + index +
                ", parameterType=" + parameterType +
                ", type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}

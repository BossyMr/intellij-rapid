package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RapidParameter;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.model.Pointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class Argument implements Field {

    private final int index;
    private final @NotNull ParameterType parameterType;
    private final @NotNull RapidType type;
    private final @NotNull String name;
    private final @Nullable Pointer<? extends RapidParameter> parameter;
    private final @Nullable List<Expression> arraySize;

    public Argument(int index, @NotNull ParameterType parameterType, @NotNull RapidType type, @NotNull String name, @Nullable RapidParameter parameter, @Nullable List<Expression> arraySize) {
        this.index = index;
        this.parameterType = parameterType;
        this.type = type;
        this.name = name;
        this.parameter = parameter != null ? parameter.createPointer() : null;
        this.arraySize = arraySize;
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

    @Override
    public @Nullable List<Expression> getArraySize() {
        return arraySize;
    }

    @Override
    public @Nullable RapidParameter getVariable() {
        return parameter != null ? parameter.dereference() : null;
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

package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.RapidField;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Variable implements Field {

    private final int index;
    private final @Nullable FieldType fieldType;
    private final @NotNull RapidType type;
    private final @Nullable String name;

    public Variable(int index, @Nullable FieldType fieldType, @NotNull RapidType type, @Nullable String name) {
        this.index = index;
        this.fieldType = fieldType;
        this.type = type;
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public @Nullable FieldType getFieldType() {
        return fieldType;
    }

    public @NotNull RapidType getType() {
        return type;
    }

    public @Nullable String getName() {
        return name;
    }

    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitVariable(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return index == variable.index && fieldType == variable.fieldType && Objects.equals(type, variable.type) && Objects.equals(name, variable.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, fieldType, type, name);
    }

    @Override
    public String toString() {
        return "Variable{" +
                "index=" + index +
                ", fieldType=" + fieldType +
                ", type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}

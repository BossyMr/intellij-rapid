package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.RapidField;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.model.Pointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class Variable implements Field {

    private final int index;
    private final @Nullable FieldType fieldType;
    private final @NotNull RapidType type;
    private @Nullable String name;
    private final @Nullable Pointer<? extends RapidField> field;
    private final @Nullable List<Expression> arraySize;

    public Variable(int index, @Nullable FieldType fieldType, @NotNull RapidType type, @Nullable String name, @Nullable RapidField field, @Nullable List<Expression> arraySize) {
        this.index = index;
        this.fieldType = fieldType;
        this.type = type;
        this.name = name;
        this.field = field != null ? field.createPointer() : null;
        this.arraySize = arraySize;
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

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Override
    public @Nullable RapidField getVariable() {
        return field != null ? field.dereference() : null;
    }

    @Override
    public @Nullable List<Expression> getArraySize() {
        return arraySize;
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

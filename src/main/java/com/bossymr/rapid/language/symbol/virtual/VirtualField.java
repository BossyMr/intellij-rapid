package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.RapidField;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.Visibility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VirtualField implements RapidField, VirtualSymbol {

    private final @NotNull FieldType fieldType;
    private final @NotNull String moduleName, name;
    private final @NotNull RapidType type;
    private final boolean isModifiable;

    public VirtualField(@NotNull FieldType fieldType, @NotNull String name, @NotNull RapidType type, boolean isModifiable) {
        this(fieldType, "", name, type, isModifiable);
    }

    public VirtualField(@NotNull FieldType fieldType, @NotNull String moduleName, @NotNull String name, @NotNull RapidType type, boolean isModifiable) {
        this.fieldType = fieldType;
        this.moduleName = moduleName;
        this.name = name;
        this.type = type;
        this.isModifiable = isModifiable;
    }

    public @NotNull String getModuleName() {
        return moduleName;
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public @NotNull FieldType getFieldType() {
        return fieldType;
    }

    @Override
    public @Nullable RapidExpression getInitializer() {
        return null;
    }

    @Override
    public boolean hasInitializer() {
        return false;
    }

    @Override
    public @NotNull Visibility getVisibility() {
        return Visibility.GLOBAL;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull VirtualPointer<VirtualField> createPointer() {
        return new VirtualPointer<>(this, getClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualField that = (VirtualField) o;
        return isModifiable == that.isModifiable && fieldType == that.fieldType && Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldType, name, type, isModifiable);
    }

    @Override
    public String toString() {
        return "VirtualField{" +
                "attribute=" + fieldType +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", isModifiable=" + isModifiable +
                '}';
    }
}

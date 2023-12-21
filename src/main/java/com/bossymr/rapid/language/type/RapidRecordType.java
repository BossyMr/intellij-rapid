package com.bossymr.rapid.language.type;

import com.bossymr.rapid.language.symbol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidRecordType implements RapidType {

    private final @NotNull RapidPointer<? extends RapidRecord> record;

    public RapidRecordType(@NotNull RapidRecord record) {
        this.record = record.createPointer();
    }

    @Override
    public @Nullable RapidRecord getStructure() {
        return record.dereference();
    }

    @Override
    public @Nullable RapidRecord getRootStructure() {
        return record.dereference();
    }

    @Override
    public @NotNull ValueType getValueType() {
        ValueType valueType = ValueType.VALUE_TYPE;
        RapidRecord structure = getStructure();
        if (structure == null) {
            return ValueType.UNKNOWN;
        }
        for (RapidComponent component : structure.getComponents()) {
            RapidType componentType = component.getType();
            if (componentType == null) {
                return ValueType.UNKNOWN;
            }
            ValueType componentValueType = componentType.getValueType();
            if (componentValueType == ValueType.UNKNOWN) {
                return ValueType.UNKNOWN;
            }
            if (componentValueType == ValueType.NON_VALUE_TYPE) {
                return ValueType.NON_VALUE_TYPE;
            }
            if (componentValueType == ValueType.SEMI_VALUE_TYPE) {
                valueType = ValueType.SEMI_VALUE_TYPE;
            }
        }
        return valueType;
    }

    @Override
    public @NotNull String getText() {
        RapidRecord dereference = record.dereference();
        if (dereference == null) {
            return RapidSymbol.getDefaultText();
        }
        String name = dereference.getName();
        if (name == null) {
            return RapidSymbol.getDefaultText();
        }
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidRecordType that = (RapidRecordType) o;
        return Objects.equals(record, that.record);
    }

    @Override
    public int hashCode() {
        return Objects.hash(record);
    }

    @Override
    public String toString() {
        return getPresentableText();
    }
}

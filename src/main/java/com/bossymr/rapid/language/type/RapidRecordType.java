package com.bossymr.rapid.language.type;

import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.symbol.ValueType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RapidRecordType implements RapidType {

    private final @NotNull RapidRecord record;

    public RapidRecordType(@NotNull RapidRecord record) {
        this.record = record;
    }

    @Override
    public @NotNull RapidRecord getStructure() {
        return record;
    }

    @Override
    public @NotNull RapidRecord getActualStructure() {
        return record;
    }

    @Override
    public @NotNull ValueType getValueType() {
        ValueType valueType = ValueType.VALUE_TYPE;
        for (RapidComponent component : getStructure().getComponents()) {
            RapidType componentType = component.getType();
            ValueType componentValueType = componentType.getValueType();
            if(componentValueType == ValueType.UNKNOWN) {
                return ValueType.UNKNOWN;
            }
            if(componentValueType == ValueType.NON_VALUE_TYPE) {
                return ValueType.NON_VALUE_TYPE;
            }
            if(componentValueType == ValueType.SEMI_VALUE_TYPE) {
                valueType = ValueType.SEMI_VALUE_TYPE;
            }
        }
        return valueType;
    }

    @Override
    public @NotNull String getText() {
        return Objects.requireNonNullElse(record.getName(), RapidPrimitiveType.getDefaultText());
    }
}

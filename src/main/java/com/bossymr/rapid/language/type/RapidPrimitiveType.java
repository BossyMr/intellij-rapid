package com.bossymr.rapid.language.type;

import com.bossymr.rapid.language.symbol.ValueType;
import com.bossymr.rapid.language.symbol.virtual.VirtualAtomic;
import com.bossymr.rapid.language.symbol.virtual.VirtualRecord;
import com.bossymr.rapid.language.symbol.virtual.VirtualStructure;
import org.jetbrains.annotations.NotNull;

public enum RapidPrimitiveType implements RapidType {

    NUMBER(new VirtualAtomic("num")),
    DOUBLE(new VirtualAtomic("dnum")),
    BOOLEAN(new VirtualAtomic("bool")),
    STRING(new VirtualAtomic("string")),

    POSITION(VirtualRecord.newBuilder("pos")
            .withComponent("x", NUMBER)
            .withComponent("y", NUMBER)
            .withComponent("z", NUMBER)
            .build()),

    ORIENTATION(VirtualRecord.newBuilder("orient")
            .withComponent("q1", NUMBER)
            .withComponent("q2", NUMBER)
            .withComponent("q3", NUMBER)
            .withComponent("q4", NUMBER)
            .build()),

    POSE(VirtualRecord.newBuilder("pose")
            .withComponent("trans", POSITION)
            .withComponent("rot", ORIENTATION)
            .build()),

    ANYTYPE(new VirtualAtomic("ANYTYPE#")) {
        @Override
        public boolean isAssignable(@NotNull RapidType type) {
            return true;
        }
    };
    private final @NotNull VirtualStructure symbol;

    RapidPrimitiveType(@NotNull VirtualStructure symbol) {
        this.symbol = symbol;
    }

    @Override
    public @NotNull VirtualStructure getStructure() {
        return symbol;
    }

    @Override
    public @NotNull VirtualStructure getActualStructure() {
        return getStructure();
    }

    @Override
    public @NotNull ValueType getValueType() {
        return ValueType.VALUE_TYPE;
    }

    @Override
    public @NotNull String getText() {
        return getStructure().getName();
    }

    @Override
    public String toString() {
        return "RapidPrimitiveType{" +
                "symbol=" + symbol +
                '}';
    }
}

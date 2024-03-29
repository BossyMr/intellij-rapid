package com.bossymr.rapid.language.type;

import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.symbol.ValueType;
import com.bossymr.rapid.language.symbol.virtual.VirtualAtomic;
import com.bossymr.rapid.language.symbol.virtual.VirtualRecord;
import com.bossymr.rapid.language.symbol.virtual.VirtualStructure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum RapidPrimitiveType implements RapidType {

    NUMBER(new VirtualAtomic("num")) {
        @Override
        public boolean isAssignable(@NotNull RapidType type) {
            if(type.equals(RapidPrimitiveType.DOUBLE)) {
                return true;
            }
            return super.isAssignable(type);
        }
    },

    DOUBLE(new VirtualAtomic("dnum")) {
        @Override
        public boolean isAssignable(@NotNull RapidType type) {
            if(type.equals(RapidPrimitiveType.NUMBER)) {
                return true;
            }
            return super.isAssignable(type);
        }
    },

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
        public @NotNull RapidType createArrayType(int dimensions) {
            return ANYTYPE;
        }

        @Override
        public @NotNull RapidType createArrayType(int dimensions, @Nullable List<RapidExpression> length) {
            return ANYTYPE;
        }

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
    public boolean isRecord() {
        return symbol instanceof RapidRecord;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public @NotNull VirtualStructure getStructure() {
        return symbol;
    }

    @Override
    public @NotNull VirtualStructure getRootStructure() {
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
        return getPresentableText();
    }
}

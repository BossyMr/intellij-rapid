package com.bossymr.flow.value.type;

public final class FloatType implements ValueType, NumericType {

    private final byte length;

    public FloatType(byte length) {
        this.length = length;
    }

    @Override
    public byte getLength() {
        return length;
    }
}

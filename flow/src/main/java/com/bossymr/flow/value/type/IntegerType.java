package com.bossymr.flow.value.type;

public final class IntegerType implements ValueType, NumericType {

    private final byte length;

    public IntegerType(byte length) {
        this.length = length;
    }

    @Override
    public byte getLength() {
        return length;
    }
}

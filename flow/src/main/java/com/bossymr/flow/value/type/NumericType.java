package com.bossymr.flow.value.type;

public sealed interface NumericType permits IntegerType, FloatType {
    byte getLength();
}

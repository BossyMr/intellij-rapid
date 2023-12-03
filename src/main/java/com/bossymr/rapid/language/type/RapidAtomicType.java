package com.bossymr.rapid.language.type;

import com.bossymr.rapid.language.symbol.RapidAtomic;
import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.ValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A {@code RapidAtomicType} represents an atomic type.
 */
public class RapidAtomicType implements RapidType {

    private final @NotNull RapidAtomic atomic;

    public RapidAtomicType(@NotNull RapidAtomic atomic) {
        this.atomic = atomic;
    }

    public @Nullable RapidType getUnderlyingType() {
        return atomic.getAssociatedType();
    }

    @Override
    public @NotNull RapidAtomic getStructure() {
        return atomic;
    }

    @Override
    public @Nullable RapidStructure getRootStructure() {
        RapidType underlyingType = getUnderlyingType();
        if(underlyingType != null) {
            return underlyingType.getRootStructure();
        } else {
            return getStructure();
        }
    }

    @Override
    public @NotNull ValueType getValueType() {
        if(getUnderlyingType() == null) {
            return ValueType.VALUE_TYPE;
        } else {
            return ValueType.NON_VALUE_TYPE;
        }
    }

    @Override
    public @NotNull String getText() {
        return atomic.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidAtomicType that = (RapidAtomicType) o;
        return Objects.equals(atomic.getName(), that.atomic.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(atomic.getName());
    }

    @Override
    public String toString() {
        return getPresentableText();
    }
}

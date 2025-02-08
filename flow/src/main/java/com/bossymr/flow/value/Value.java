package com.bossymr.flow.value;

import com.bossymr.flow.value.type.ValueType;

/**
 * A {@code Value} represents a value.
 */
public interface Value {

    /**
     * Returns the type of the value.
     *
     * @return the type of the value.
     */
    ValueType getType();

}

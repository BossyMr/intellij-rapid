package com.bossymr.flow.value;

import com.bossymr.flow.expression.Expression;
import com.bossymr.flow.type.ValueType;

/**
 * A {@code Value} represents a value.
 */
public interface Value extends Expression {

    /**
     * Returns the type of the value.
     *
     * @return the type of the value.
     */
    ValueType getType();

}

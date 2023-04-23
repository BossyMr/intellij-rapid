package com.bossymr.rapid.language.symbol;

/**
 * A {@code ValueType} is used to classify a structure.
 *
 * @see RapidType#getValueType()
 */
public enum ValueType {

    /**
     * A value type represents a literal value.
     * <p>
     * The built-in atomic types {@code num}, {@code dnum}, {@code bool} and {@code string} are value types. A record
     * which only contains components which are value types is a value type. An alias which is defined upon a value type
     * is also itself value type.
     */
    VALUE_TYPE,

    /**
     * A non-value type represents a value which cannot be represented as literal.
     * <p>
     * A record which contains a component which is a non-value type is a non-value type and an alias which is defined
     * upon a non-value type is also a non-value type.
     */
    NON_VALUE_TYPE,

    /**
     * A semi-value type can be represented as both a non-value type and as an associated value type. A semi-value type
     * is viewed as a value-type when used in value context, and as a non-value type otherwise.
     * <h2>Example</h2>
     * <pre>{@code
     * VAR signaldi sig1;
     * ...
     * ! use digital input sig1 as value object
     * IF sig1 = 1 THEN
     * ...
     * ...
     * ! use digital input sig1 as non-value object
     * IF DInput(sig1) = 1 THEN
     * ...
     * }</pre>
     */
    SEMI_VALUE_TYPE
}

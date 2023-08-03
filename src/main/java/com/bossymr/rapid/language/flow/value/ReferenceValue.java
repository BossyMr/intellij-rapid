package com.bossymr.rapid.language.flow.value;

/**
 * A {@code ReferenceValue} represents a variable reference.
 */
sealed public interface ReferenceValue extends Value permits IndexValue, ComponentValue, FieldValue, VariableValue, ReferenceSnapshot {}

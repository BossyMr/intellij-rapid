package com.bossymr.rapid.language.flow.value;

sealed public interface ReferenceValue extends Value permits ComponentValue, FieldValue, IndexValue, VariableValue, VariableSnapshot {}

package com.bossymr.rapid.language.flow.value;

sealed public interface ReferenceValue extends Value permits ComponentReference, FieldReference, IndexReference, VariableReference, VariableSnapshot {}

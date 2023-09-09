package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

public sealed interface Value permits ConstantValue, ErrorValue, ReferenceValue {

    <R> R accept(@NotNull ControlFlowVisitor<R> visitor);

    @NotNull RapidType getType();

}

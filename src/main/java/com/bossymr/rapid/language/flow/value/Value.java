package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code Value} represents a value.
 */
public sealed interface Value permits ConstantValue, ErrorValue, ReferenceValue {

    <T> T accept(@NotNull ControlFlowVisitor<T> visitor);

    @NotNull RapidType type();

}

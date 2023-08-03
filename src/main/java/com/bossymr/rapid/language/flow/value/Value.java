package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

/**
 * A value represents either a constant value or a variable. Alternatively, a value can also represent an error element,
 * if the actual value could not be determined.
 */
public sealed interface Value permits ConstantValue, ErrorValue, ReferenceValue {

    void accept(@NotNull ControlFlowVisitor visitor);

    @NotNull RapidType getType();

}

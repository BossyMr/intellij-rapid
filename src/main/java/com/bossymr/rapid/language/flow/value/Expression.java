package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

/**
 * An expression represents an expression.
 */
public interface Expression {

    <R> R accept(@NotNull ControlFlowVisitor<R> visitor);

}

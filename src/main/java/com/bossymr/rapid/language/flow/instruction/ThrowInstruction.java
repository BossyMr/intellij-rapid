package com.bossymr.rapid.language.flow.instruction;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ThrowInstruction extends Instruction {

    private final @Nullable Expression exceptionValue;

    public ThrowInstruction(@NotNull Block block, @Nullable PsiElement element, @Nullable Expression exceptionValue) {
        super(block, element);
        if (exceptionValue != null && !(RapidPrimitiveType.NUMBER.isAssignable(exceptionValue.getType()))) {
            throw new IllegalArgumentException("Invalid exception type: " + exceptionValue.getType());
        }
        this.exceptionValue = exceptionValue;
    }

    public @Nullable Expression getExceptionValue() {
        return exceptionValue;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitThrowInstruction(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ThrowInstruction that = (ThrowInstruction) o;
        return Objects.equals(exceptionValue, that.exceptionValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), exceptionValue);
    }

    @Override
    public String toString() {
        return "throw" + (exceptionValue != null ? " " + exceptionValue : "");
    }
}

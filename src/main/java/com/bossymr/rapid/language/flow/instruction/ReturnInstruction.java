package com.bossymr.rapid.language.flow.instruction;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.expression.Expression;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ReturnInstruction extends Instruction {

    private final @Nullable Expression returnValue;

    public ReturnInstruction(@NotNull Block block, @Nullable PsiElement element, @Nullable Expression returnValue) {
        super(block, element);
        if (block.getReturnType() == null && returnValue != null) {
            throw new IllegalArgumentException("Cannot return value of type: " + returnValue.getType() + " from method: " + block);
        }
        if (block.getReturnType() != null) {
            if (returnValue == null) {
                throw new IllegalArgumentException("Cannot return without return value from method: " + block);
            }
            if (!(block.getReturnType().isAssignable(returnValue.getType()))) {
                throw new IllegalArgumentException("Cannot return value of type: " + returnValue.getType() + " from method: " + block);
            }
        }
        this.returnValue = returnValue;
    }

    public @Nullable Expression getReturnValue() {
        return returnValue;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitReturnInstruction(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ReturnInstruction that = (ReturnInstruction) o;
        return Objects.equals(returnValue, that.returnValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), returnValue);
    }

    @Override
    public String toString() {
        return "return" + (returnValue != null ? " " + returnValue : "");
    }
}

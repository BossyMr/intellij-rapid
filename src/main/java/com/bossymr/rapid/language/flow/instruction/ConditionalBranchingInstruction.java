package com.bossymr.rapid.language.flow.instruction;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.expression.Expression;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ConditionalBranchingInstruction extends Instruction {

    private final @NotNull Expression condition;

    public ConditionalBranchingInstruction(@NotNull Block block, @Nullable PsiElement element, @NotNull Expression condition) {
        super(block, element);
        if (!(RapidPrimitiveType.BOOLEAN.isAssignable(condition.getType()))) {
            throw new IllegalArgumentException("Invalid condition type: " + condition.getType());
        }
        this.condition = condition;
    }

    public Instruction getTrue() {
        if(getSuccessors().isEmpty()) {
            return null;
        }
        return getSuccessors().get(0);
    }

    public Instruction getFalse() {
        if(getSuccessors().size() < 2 ) {
            return null;
        }
        return getSuccessors().get(1);
    }

    public @NotNull Expression getCondition() {
        return condition;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitConditionalBranchingInstruction(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConditionalBranchingInstruction that = (ConditionalBranchingInstruction) o;
        return Objects.equals(condition, that.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), condition);
    }

    @Override
    public String toString() {
        return condition + "?";
    }
}

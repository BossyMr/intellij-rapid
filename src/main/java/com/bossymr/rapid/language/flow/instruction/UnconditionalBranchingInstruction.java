package com.bossymr.rapid.language.flow.instruction;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class UnconditionalBranchingInstruction extends Instruction {

    private final @NotNull ControlFlowLabel label;

    public UnconditionalBranchingInstruction(@NotNull Block block, @Nullable PsiElement element, @NotNull ControlFlowLabel label) {
        super(block, element);
        this.label = label;
    }

    @Override
    public @NotNull List<Instruction> getSuccessors() {
        return List.of(getSuccessor());
    }

    public @NotNull Instruction getSuccessor() {
        return Objects.requireNonNull(label.getInstruction());
    }

    public @NotNull ControlFlowLabel getLabel() {
        return label;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitUnconditionalBranchingInstruction(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UnconditionalBranchingInstruction that = (UnconditionalBranchingInstruction) o;
        return Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), label);
    }

    @Override
    public String toString() {
        return "goto " + label;
    }
}

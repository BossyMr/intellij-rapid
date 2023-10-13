package com.bossymr.rapid.language.flow.instruction;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction.ConditionalBranchingInstruction;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An {@code Instruction} represents a statement.
 */
public sealed interface Instruction permits BranchingInstruction, LinearInstruction {

    /**
     * Returns the {@code PsiElement} which this {@code Instruction} represents. For example, if this instruction is a
     * {@link ConditionalBranchingInstruction} but will always branch to a specific block, this element will be
     * annotated with a warning.
     *
     * @return the element which this instruction represents, or {@code null} if this instruction represents a
     * fall-through instruction.
     */
    @Nullable PsiElement element();

    @Nullable Instruction next();

    <R> R accept(@NotNull ControlFlowVisitor<R> visitor);

}

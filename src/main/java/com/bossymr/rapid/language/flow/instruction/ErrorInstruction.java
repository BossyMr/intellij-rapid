package com.bossymr.rapid.language.flow.instruction;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ErrorInstruction extends Instruction {

    public ErrorInstruction(@NotNull Block block, @Nullable PsiElement element) {
        super(block, element);
    }

    public @Nullable Instruction getSuccessor() {
        if (getSuccessors().isEmpty()) {
            return null;
        }
        return getSuccessors().get(0);
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitErrorInstruction(this);
    }

    @Override
    public String toString() {
        return "error";
    }
}

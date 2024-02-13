package com.bossymr.rapid.language.flow.instruction;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TryNextInstruction extends Instruction {

    public TryNextInstruction(@NotNull Block block, @Nullable PsiElement element) {
        super(block, element);
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitTryNextInstruction(this);
    }

    @Override
    public String toString() {
        return "trynext";
    }
}

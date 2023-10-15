package com.bossymr.rapid.language.flow.instruction;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RetryInstruction extends Instruction {

    public RetryInstruction(@Nullable PsiElement element) {
        super(element);
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitRetryInstruction(this);
    }

    @Override
    public String toString() {
        return "retry";
    }
}

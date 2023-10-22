package com.bossymr.rapid.language.flow.instruction;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * An {@code Instruction} represents a statement.
 */
public abstract class Instruction {

    private final @NotNull Block block;

    private final @NotNull List<Instruction> successors = new ArrayList<>(1);
    private final @NotNull List<Instruction> predecessors = new ArrayList<>(1);

    private final @Nullable SmartPsiElementPointer<PsiElement> pointer;

    protected Instruction(@NotNull Block block, @Nullable PsiElement element) {
        this.block = block;
        if (element != null) {
            pointer = SmartPointerManager.createPointer(element);
        } else {
            pointer = null;
        }
    }

    public int getIndex() {
        int index = getBlock().getInstructions().indexOf(this);
        if (index < 0) {
            throw new IllegalStateException();
        }
        return index;
    }

    public @NotNull Block getBlock() {
        return block;
    }

    public @NotNull List<Instruction> getSuccessors() {
        return successors;
    }

    public @NotNull List<Instruction> getPredecessors() {
        return predecessors;
    }

    public @Nullable PsiElement getElement() {
        if (pointer != null) {
            return pointer.getElement();
        } else {
            return null;
        }
    }

    public abstract <R> R accept(@NotNull ControlFlowVisitor<R> visitor);
}

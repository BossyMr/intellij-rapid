package com.bossymr.rapid.language.flow.instruction;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * An {@code Instruction} represents a statement.
 */
public abstract class Instruction {

    private final @NotNull List<Instruction> successors = new ArrayList<>(1);
    private final @NotNull List<Instruction> predecessors = new ArrayList<>(1);

    private final @Nullable SmartPsiElementPointer<PsiElement> pointer;

    protected Instruction(@Nullable PsiElement element) {
        if (element != null) {
            pointer = SmartPointerManager.createPointer(element);
        } else {
            pointer = null;
        }
    }

    public @NotNull List<Instruction> getSuccessors() {
        return successors;
    }

    public @NotNull List<Instruction> getPredecessors() {
        return predecessors;
    }

    public @Nullable PsiElement getElement() {
        if(pointer != null) {
            return pointer.getElement();
        } else {
            return null;
        }
    }

    public abstract <R> R accept(@NotNull ControlFlowVisitor<R> visitor);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instruction that = (Instruction) o;
        return Objects.equals(successors, that.successors) && Objects.equals(predecessors, that.predecessors) && Objects.equals(pointer, that.pointer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(successors, predecessors, pointer);
    }
}

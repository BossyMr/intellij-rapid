package com.bossymr.rapid.language.flow.instruction;

import com.bossymr.rapid.language.builder.Label;
import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LabelInstruction extends Instruction implements Label {

    private final @Nullable String name;

    public LabelInstruction(@Nullable PsiElement element, @Nullable String name) {
        super(element);
        this.name = name;
    }

    @Override
    public @Nullable String getName() {
        return name;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitLabel(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LabelInstruction that = (LabelInstruction) o;
        if(name == null || that.name == null) {
            return false;
        }
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), (name != null ? name : System.identityHashCode(this)));
    }

    @Override
    public String toString() {
        return (name != null ? name : hashCode()) + ":";
    }
}

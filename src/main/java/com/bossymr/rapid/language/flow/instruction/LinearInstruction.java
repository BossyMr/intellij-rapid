package com.bossymr.rapid.language.flow.instruction;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.conditon.Expression;
import com.bossymr.rapid.language.flow.conditon.Value;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code LinearInstruction} represents an instruction which will move the program pointer to the next instruction.
 */
public sealed interface LinearInstruction extends Instruction {

    /**
     * Assigns the specified value to the specified field.
     *
     * @param variable the field.
     * @param value the value.
     */
    record AssignmentInstruction(@NotNull PsiElement element, @NotNull Value.Variable variable, @NotNull Expression value) implements LinearInstruction {
        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
            visitor.visitAssignmentInstruction(this);
        }
    }

    /**
     * Connects the specified interrupt with a trap with the specified value.
     *
     * @param variable the interrupt.
     * @param routine the routine.
     */
    record ConnectInstruction(@NotNull PsiElement element, @NotNull Value.Variable variable, @NotNull Value routine) implements LinearInstruction {
        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
            visitor.visitConnectInstruction(this);
        }
    }
}
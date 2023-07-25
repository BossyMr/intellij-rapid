package com.bossymr.rapid.language.flow.instruction;

import com.bossymr.rapid.language.flow.ArgumentDescriptor;
import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.value.ReferenceValue;
import com.bossymr.rapid.language.flow.value.Value;
import com.bossymr.rapid.language.symbol.RapidType;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A {@code BranchingInstruction} represents an instruction which will move the program pointer to another instruction,
 * and not necessarily the next instruction.
 */
public sealed interface BranchingInstruction extends Instruction {

    /**
     * A {@code ConditionalBranchingInstruction} is an instruction which can move the program pointer to one of two
     * instructions, depending on the specified value.
     *
     * @param value the value (must be a boolean).
     * @param onSuccess the instruction to go to if the specified value is {@code true}.
     * @param onFailure the instruction to go to if the specified value is {@code false}.
     */
    record ConditionalBranchingInstruction(@NotNull PsiElement element, @NotNull ReferenceValue value, @NotNull BasicBlock onSuccess,
                                           @NotNull BasicBlock onFailure) implements BranchingInstruction {

        public ConditionalBranchingInstruction {
            assert value.getType().isAssignable(RapidType.BOOLEAN);
        }

        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
            visitor.visitConditionalBranchingInstruction(this);
        }
    }

    /**
     * An {@code UnconditionalBranchingInstruction} is an instruction which always points to a specific instruction.
     *
     * @param next the instruction to go to.
     */
    record UnconditionalBranchingInstruction(@Nullable PsiElement element, @NotNull BasicBlock next) implements BranchingInstruction {
        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
            visitor.visitUnconditionalBranchingInstruction(this);
        }
    }

    /**
     * A {@code RetryInstruction} will move the program pointer back to the instruction that failed and caused the
     * program pointer to go to this instruction.
     */
    record RetryInstruction(@NotNull PsiElement element) implements BranchingInstruction {
        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
            visitor.visitRetryInstruction(this);
        }
    }

    /**
     * A {@code TryNextInstruction} will move the program pointer to the instruction after the instruction which failed
     * and caused the program pointer to go to this instruction.
     */
    record TryNextInstruction(@NotNull PsiElement element) implements BranchingInstruction {
        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
            visitor.visitTryNextInstruction(this);
        }
    }

    /**
     * A {@code ReturnInstruction} will move the program pointer to the {@link CallInstruction CallInstruction} which
     * called this routine, or exit the program if the current routine is the entry point.
     */
    record ReturnInstruction(@Nullable PsiElement element, @Nullable Value value) implements BranchingInstruction {

        public ReturnInstruction {
            if (element != null && value == null) {
                throw new AssertionError();
            }
        }

        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
            visitor.visitReturnInstruction(this);
        }
    }

    /**
     * An {@code ExitInstruction} will exit the program.
     */
    record ExitInstruction(@NotNull PsiElement element) implements BranchingInstruction {
        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
            visitor.visitExitInstruction(this);
        }
    }

    /**
     * A {@code ThrowInstruction} will propagate an exception.
     *
     * @param exception the exception.
     */
    record ThrowInstruction(@NotNull PsiElement element, @Nullable Value exception) implements BranchingInstruction {
        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
            visitor.visitThrowInstruction(this);
        }
    }

    /**
     * An {@code ErrorInstruction} indicates that a syntax or semantic error has occurred and the remainder of the
     * control flow graph might be invalid.
     */
    record ErrorInstruction(@Nullable PsiElement element, @Nullable BasicBlock next) implements BranchingInstruction {
        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
            visitor.visitErrorInstruction(this);
        }
    }

    /**
     * A {@code CallInstruction} will call the routine with the specified name. If the specified routine returns
     * successfully, the program pointer will go to the next instruction. If the specified routine does not return
     * successfully, the program pointer will move up the stack. If the parameter {@code onFailure} is specified, it
     * will go to that instruction, otherwise, it will find the instruction which called the function, and go to the
     * parameter {@code onFailure} if specified, and so on.
     *
     * @param routine the name of the routine to invoke, in the format "moduleName:routineName".
     * @param arguments the parameters to call this routine with, a value in the map might be {@code null} if the
     * argument is only present.
     * @param returnValue the field to store the return value of this routine.
     */
    record CallInstruction(@NotNull PsiElement element, @NotNull Value routine, @NotNull Map<ArgumentDescriptor, Value> arguments,
                           @Nullable ReferenceValue returnValue, @NotNull BasicBlock next) implements BranchingInstruction {

        @Override
        public void accept(@NotNull ControlFlowVisitor visitor) {
            visitor.visitCallInstruction(this);
        }
    }

}

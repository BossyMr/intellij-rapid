package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.Value;
import com.bossymr.rapid.language.psi.StatementListType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A {@code BasicBlock} represents a set of {@link LinearInstruction instructions} which are always executed
 * sequentially. A block always ends with a {@link BranchingInstruction terminator}.
 */
public sealed abstract class BasicBlock {

    private final @NotNull Block block;
    private final @NotNull List<LinearInstruction> instructions;
    private BranchingInstruction terminator;

    protected BasicBlock(@NotNull Block block) {
        this.block = block;
        this.instructions = new ArrayList<>();
    }

    public @NotNull Block getBlock() {
        return block;
    }

    public int getIndex() {
        return getBlock().getBasicBlocks().indexOf(this);
    }

    public abstract @Nullable StatementListType getScopeType();

    public @NotNull List<LinearInstruction> getInstructions() {
        return instructions;
    }

    public @NotNull BranchingInstruction getTerminator() {
        if (terminator == null) {
            throw new IllegalStateException("BasicBlock: " + getIndex() + " is not completed");
        }
        return terminator;
    }

    public void setTerminator(@NotNull BranchingInstruction instruction) {
        if (terminator != null) {
            throw new IllegalStateException("BasicBlock: " + getIndex() + " is already completed");
        }
        this.terminator = instruction;
    }

    public void accept(@NotNull ControlFlowVisitor visitor) {
        visitor.visitBasicBlock(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicBlock basicBlock = (BasicBlock) o;
        return getBlock() == basicBlock.getBlock() && getIndex() == basicBlock.getIndex();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIndex());
    }

    @Override
    public String toString() {
        return "BasicBlock{" +
                "index=" + getIndex() +
                '}';
    }

    public static final class EntryBasicBlock extends BasicBlock {

        private final @NotNull StatementListType scopeType;

        public EntryBasicBlock(@NotNull Block block, @NotNull StatementListType scopeType) {
            super(block);
            if (scopeType == StatementListType.ERROR_CLAUSE) {
                throw new IllegalArgumentException();
            }
            this.scopeType = scopeType;
        }

        @Override
        public @NotNull StatementListType getScopeType() {
            return scopeType;
        }
    }

    public static final class ErrorBasicBlock extends BasicBlock {

        private final @Nullable List<Value> exceptions;

        public ErrorBasicBlock(@NotNull Block block, @Nullable List<Value> exceptions) {
            super(block);
            this.exceptions = exceptions;
        }

        @Override
        public @NotNull StatementListType getScopeType() {
            return StatementListType.ERROR_CLAUSE;
        }

        public @Nullable List<Value> getExceptions() {
            return exceptions;
        }
    }

    public static final class IntermediateBasicBlock extends BasicBlock {

        public IntermediateBasicBlock(@NotNull Block block) {
            super(block);
        }

        @Override
        public @Nullable StatementListType getScopeType() {
            return null;
        }
    }
}

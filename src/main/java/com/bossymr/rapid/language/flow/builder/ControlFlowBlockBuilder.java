package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ControlFlowBlockBuilder {

    private final @NotNull List<Instruction> instructions = new ArrayList<>();

    private final @NotNull Block block;
    private Scope currentScope;

    public ControlFlowBlockBuilder(@NotNull Block block) {
        this.block = block;
    }

    public boolean isInScope() {
        return currentScope != null;
    }

    public @NotNull List<Instruction> getInstructions() {
        return instructions;
    }

    public @NotNull Scope enterScope() {
        if(currentScope != null) {
            throw new IllegalStateException();
        }
        Scope scope = new Scope();
        currentScope = scope;
        return scope;
    }

    public @NotNull Scope exitScope() {
        if(currentScope == null) {
            throw new IllegalStateException();
        }
        Scope scope = currentScope;
        currentScope = null;
        return scope;
    }

    public void continueScope(@NotNull Instruction instruction) {
        if(currentScope == null) {
            return;
        }
        Scope scope = currentScope;
        Instruction tail = scope.getTail();
        if (tail != null) {
            tail.getSuccessors().add(instruction);
            instruction.getPredecessors().add(tail);
        }
        if (tail == null) {
            scope.setHead(instruction);
            for (Instruction predecessor : scope.getPredecessors()) {
                predecessor.getSuccessors().add(instruction);
                instruction.getPredecessors().add(predecessor);
            }
        }
        scope.setTail(instruction);
        instructions.add(instruction);
        block.getInstructions().add(instruction);
    }

    public static class Scope {

        private final @NotNull Set<Instruction> predecessors = new HashSet<>();

        private @Nullable Instruction head, tail;

        public @NotNull Set<Instruction> getPredecessors() {
            return predecessors;
        }

        public @Nullable Instruction getHead() {
            return head;
        }

        public void setHead(@Nullable Instruction head) {
            this.head = head;
        }

        public @Nullable Instruction getTail() {
            return tail;
        }

        public void setTail(@Nullable Instruction tail) {
            this.tail = tail;
        }
    }
}

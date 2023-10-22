package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.instruction.ControlFlowLabel;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ControlFlowBlockBuilder {

    private final @NotNull Deque<Predicate<Instruction>> commands = new ArrayDeque<>();

    private final @NotNull Block block;
    private Scope currentScope;

    public ControlFlowBlockBuilder(@NotNull Block block) {
        this.block = block;
    }

    public void addCommand(@NotNull Predicate<Instruction> command) {
        commands.addFirst(command);
    }

    public void addCommand(@NotNull Consumer<Instruction> command) {
        addCommand(instruction -> {
            command.accept(instruction);
            return true;
        });
    }

    public boolean isInScope() {
        return currentScope != null;
    }

    public @NotNull Scope enterScope() {
        if (currentScope != null) {
            throw new IllegalStateException();
        }
        Scope scope = new Scope();
        currentScope = scope;
        return scope;
    }

    public @Nullable Scope exitScope() {
        if (currentScope == null) {
            return null;
        }
        commands.clear();
        Scope scope = currentScope;
        currentScope = null;
        return scope;
    }

    public void goTo(@NotNull ControlFlowLabel instruction) {
        Instruction label = instruction.getInstruction();
        Set<Instruction> predecessors = Set.copyOf(currentScope.getPredecessors());
        if (label == null) {
            List<Predicate<Instruction>> copy = List.copyOf(commands);
            Scope scope = currentScope;
            exitScope();
            addCommand(next -> {
                Instruction newLabel = instruction.getInstruction();
                if (newLabel != null) {
                    if (scope.getTail() != null) {
                        scope.getTail().getSuccessors().add(newLabel);
                        newLabel.getPredecessors().add(scope.getTail());
                    } else {
                        for (Instruction predecessor : scope.getPredecessors()) {
                            predecessor.getSuccessors().add(newLabel);
                            newLabel.getPredecessors().add(predecessor);
                        }
                    }
                    copy.forEach(command -> command.test(newLabel));
                    return true;
                }
                return false;
            });
            return;
        }
        Instruction tail = currentScope.getTail();
        if (tail != null) {
            tail.getSuccessors().add(label);
            label.getPredecessors().add(tail);
        } else {
            for (Instruction predecessor : predecessors) {
                predecessor.getSuccessors().add(label);
                label.getPredecessors().add(predecessor);
            }
        }
        commands.removeIf(command -> command.test(label));
        exitScope();
    }


    public void continueScope(@NotNull Instruction instruction) {
        if (currentScope == null) {
            return;
        }
        Instruction tail = currentScope.getTail();
        if (tail != null) {
            tail.getSuccessors().add(instruction);
            instruction.getPredecessors().add(tail);
        }
        if (tail == null) {
            currentScope.setHead(instruction);
            for (Instruction predecessor : currentScope.getPredecessors()) {
                predecessor.getSuccessors().add(instruction);
                instruction.getPredecessors().add(predecessor);
            }
        }
        currentScope.setTail(instruction);
        block.getInstructions().add(instruction);
        commands.removeIf(command -> command.test(instruction));
        commands.clear();
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

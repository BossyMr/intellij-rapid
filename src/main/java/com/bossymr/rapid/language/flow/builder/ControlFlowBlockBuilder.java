package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.Field;
import com.bossymr.rapid.language.flow.data.snapshots.ErrorExpression;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.instruction.ControlFlowLabel;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.flow.value.SnapshotExpression;
import com.bossymr.rapid.language.psi.RapidExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ControlFlowBlockBuilder {

    private final @NotNull Deque<Command> commands = new ArrayDeque<>();
    private final @NotNull Block block;
    private @Nullable Scope currentScope;

    public ControlFlowBlockBuilder(@NotNull Block block) {
        this.block = block;
    }

    public void addCommand(@NotNull Command command) {
        commands.addFirst(command);
    }

    public void addCommand(@NotNull Consumer<Instruction> command) {
        addCommand(instruction -> {
            command.accept(instruction);
            return true;
        });
    }

    public @Nullable Scope exitScope() {
        Scope scope = currentScope;
        currentScope = null;
        return scope;
    }

    public void enterScope() {
        currentScope = new Scope(new ArrayDeque<>());
    }

    public void enterScope(@NotNull Scope scope) {
        if(this.currentScope != null) {
            currentScope = currentScope.merge(scope);
        } else {
            currentScope = scope;
        }
    }

    public boolean isInScope() {
        return currentScope != null;
    }

    public void goTo(@NotNull ControlFlowLabel instruction) {
        if (currentScope == null) {
            return;
        }
        Instruction label = instruction.getInstruction();
        if (label == null) {
            Scope copy = currentScope != null ? currentScope.copy() : new Scope(new ArrayDeque<>());
            addCommand(nextInstruction -> {
                Instruction currentLabel = instruction.getInstruction();
                if (currentLabel == null) {
                    return false;
                }
                copy.commands().removeIf(command -> command.process(currentLabel));
                return true;
            });
        } else {
            commands.removeIf(command -> command.process(label));
            if (currentScope != null) {
                currentScope.commands().removeIf(command -> command.process(label));
            }
        }
        exitScope();
    }


    public void continueScope(@NotNull Instruction instruction) {
        if(currentScope == null) {
            return;
        }
        block.getInstructions().add(instruction);
        commands.removeIf(command -> command.process(instruction));
        if (currentScope != null) {
            currentScope.commands().removeIf(command -> command.process(instruction));
        } else {
            currentScope = new Scope(new ArrayDeque<>());
        }
        currentScope.commands().addFirst(nextInstruction -> {
            nextInstruction.getPredecessors().add(instruction);
            instruction.getSuccessors().add(nextInstruction);
            return true;
        });
    }

    public record Scope(@NotNull Deque<Command> commands) {

        public @NotNull Scope merge(@NotNull Scope scope) {
            Scope copy = copy();
            copy.commands().addAll(scope.commands());
            return copy;
        }

        public @NotNull Scope copy() {
            return new Scope(new ArrayDeque<>(commands));
        }
    }

    @FunctionalInterface
    public interface Command {
        boolean process(@NotNull Instruction instruction);
    }
}
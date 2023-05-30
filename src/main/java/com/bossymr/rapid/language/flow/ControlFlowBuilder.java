package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.conditon.Value;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.psi.StatementListType;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.RoutineType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A {@code ControlFlowBuilder} is used to create a control flow graph.
 */
public class ControlFlowBuilder {

    private final @NotNull ControlFlow controlFlow = new ControlFlow();

    private Block currentBlock;
    private Scope currentScope;
    private Map<String, Scope> currentLabels;
    private Deque<VariableKey> currentVariables;

    public @NotNull Block enterFunction(@NotNull String moduleName, @NotNull String name, @Nullable RapidType returnType, @NotNull RoutineType routineType, boolean hasArguments) {
        Block block = new Block.FunctionBlock(moduleName, name, returnType, new ArrayList<>(), new HashMap<>(), new HashMap<>(), hasArguments ? new ArrayList<>() : null, routineType);
        enterBlock(block);
        return block;
    }

    public @NotNull Block enterField(@NotNull String moduleName, @NotNull String name, @NotNull RapidType returnType, @NotNull FieldType fieldType) {
        Block block = new Block.FieldBlock(moduleName, name, returnType, new ArrayList<>(), new HashMap<>(), new HashMap<>(), fieldType);
        enterBlock(block);
        return block;
    }

    private void enterBlock(@NotNull Block block) {
        if (currentBlock != null) {
            throw new IllegalStateException("Cannot visit block: " + block + " as previous block: " + currentBlock + " is still active");
        }
        this.currentBlock = block;
        this.currentLabels = new HashMap<>();
        this.currentVariables = new ArrayDeque<>();
    }

    public void exitBlock() {
        if (currentBlock == null) {
            throw new IllegalStateException("Cannot exit block as no block is currently active");
        }
        if (currentScope != null) {
            throw new IllegalStateException("Cannot exit block as current scope: " + currentScope + " is still active");
        }
        controlFlow.insertBlock(currentBlock);
        this.currentBlock = null;
        this.currentLabels = null;
        this.currentVariables = null;
    }

    public void enterScope(@NotNull StatementListType scopeType) {
        if (currentBlock == null) {
            throw new IllegalStateException("Cannot enter scope as no block is currently active");
        }
        if (currentScope != null) {
            throw new IllegalStateException("Cannot enter scope as previous scope: " + currentScope + " is still active");
        }
        Scope scope = new Scope.EntryScope(scopeType, currentBlock.scopes().size(), new ArrayList<>());
        currentBlock.scopes().add(scope);
        this.currentScope = scope;
    }

    public void enterScope(@Nullable List<Value> exceptions) {
        if (currentBlock == null) {
            throw new IllegalStateException("Cannot enter scope as no block is currently active");
        }
        if (currentScope != null) {
            throw new IllegalStateException("Cannot enter scope as previous scope: " + currentScope + " is still active");
        }
        Scope scope = new Scope.ErrorScope(currentBlock.scopes().size(), exceptions, new ArrayList<>());
        currentBlock.scopes().add(scope);
        this.currentScope = scope;
    }

    public void enterScope(@NotNull Scope scope) {
        if (currentBlock == null) {
            throw new IllegalStateException("Cannot enter scope as no block is currently active");
        }
        if (currentScope != null) {
            throw new IllegalStateException("Cannot enter scope as previous scope: " + currentScope + " is still active");
        }
        if (!(scope.instructions().isEmpty())) {
            throw new IllegalStateException("Cannot enter scope: " + scope + " as scope is already complete");
        }
        this.currentScope = scope;
    }


    public void exitScope(@NotNull BranchingInstruction branchingInstruction) {
        if (currentScope == null) {
            throw new IllegalStateException("Cannot exit scope as no scope is currently active");
        }
        currentScope.instructions().add(branchingInstruction);
        currentScope = null;
    }

    public void failScope() {
        if (currentScope == null) {
            throw new IllegalStateException("Cannot exit scope as no scope is currently active");
        }
        Scope scope = createScope();
        currentScope.instructions().add(new BranchingInstruction.ErrorInstruction(scope));
        currentScope = scope;
    }

    public void continueScope(@NotNull LinearInstruction linearInstruction) {
        if (currentScope == null) {
            throw new IllegalStateException("Cannot continue scope as no scope is currently active");
        }
        currentScope.instructions().add(linearInstruction);
    }

    public boolean isInsideScope() {
        return currentScope != null;
    }

    public void withArgumentGroup(@NotNull ArgumentGroup argumentGroup) {
        if (!(currentBlock instanceof Block.FunctionBlock)) {
            throw new IllegalStateException("Cannot add argument: " + argumentGroup + " to: " + currentBlock);
        }
        List<ArgumentGroup> arguments = currentBlock.arguments();
        if (arguments == null) {
            throw new IllegalStateException("Cannot add argument: " + argumentGroup + " to: " + currentBlock);
        }
        arguments.add(argumentGroup);
    }

    public @NotNull Argument createArgument(@NotNull String name, @NotNull RapidType type, @NotNull ParameterType parameterType) {
        if (!(currentBlock instanceof Block.FunctionBlock)) {
            throw new IllegalStateException("Cannot add argument: " + name + " to: " + currentBlock);
        }
        return new Argument(currentBlock.getNextIndex(), parameterType, type, name);
    }

    public @NotNull Scope createScope() {
        if (currentBlock == null) {
            throw new IllegalStateException("Could not create scope as no block is currently active");
        }
        Scope scope = new Scope.IntermediateScope(currentBlock.scopes().size(), new ArrayList<>());
        currentBlock.scopes().add(scope);
        return scope;
    }

    public void pushVariable(@NotNull VariableKey variableKey) {
        if (currentVariables == null) {
            throw new IllegalStateException("Could not push variable as no block is currently active");
        }
        currentVariables.addLast(variableKey);
    }

    public @NotNull Variable popVariable(@NotNull RapidType type, @Nullable Object initialValue) {
        if (currentVariables == null) {
            throw new IllegalStateException("Could not pop variable as no block is currently active");
        }
        VariableKey variableKey = currentVariables.removeLast();
        return variableKey.create(currentBlock, type, initialValue);
    }

    public void enterLabel(@NotNull String name) {
        if (currentLabels == null) {
            throw new IllegalStateException("Could not retrieve label as no block is currently active");
        }
        if (currentLabels.containsKey(name)) {
            Scope scope = currentLabels.get(name);
            exitScope(new BranchingInstruction.UnconditionalBranchingInstruction(scope));
            if (scope.instructions().isEmpty()) {
                enterScope(scope);
            }
        } else {
            Scope scope = createScope();
            currentLabels.put(name, scope);
            exitScope(new BranchingInstruction.UnconditionalBranchingInstruction(scope));
            enterScope(scope);
        }
    }

    public @Nullable Argument getArgumentInBlock(@NotNull String name) {
        if (currentBlock == null) {
            throw new IllegalStateException("Could not find argument: " + name + " as no block is currently active");
        }
        List<ArgumentGroup> arguments = currentBlock.arguments();
        if (arguments == null) {
            return null;
        }
        for (ArgumentGroup group : arguments) {
            for (Argument parameter : group.arguments()) {
                if (parameter.name().equalsIgnoreCase(name)) {
                    return parameter;
                }
            }
        }
        return null;
    }

    public @Nullable Variable getVariableInBlock(@NotNull String name) {
        if (currentBlock == null) {
            throw new IllegalStateException("Could not find variable: " + name + " as no block is currently active");
        }
        Collection<Variable> arguments = currentBlock.variables().values();
        for (Variable argument : arguments) {
            if (name.equalsIgnoreCase(argument.name())) {
                return argument;
            }
        }
        return null;
    }

    public @NotNull ControlFlow build() {
        return controlFlow;
    }

}

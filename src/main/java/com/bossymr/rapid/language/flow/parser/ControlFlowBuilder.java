package com.bossymr.rapid.language.flow.parser;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.conditon.Value;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.psi.StatementListType;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@code ControlFlowBuilder} is used to create a control flow graph.
 */
public class ControlFlowBuilder {

    private final @NotNull ControlFlow controlFlow = new ControlFlow();

    private Block currentBlock;
    private BasicBlock currentBasicBlock;
    private Map<String, BasicBlock> currentLabels;

    public @NotNull Block enterFunction(@NotNull String moduleName, @NotNull String name, @Nullable RapidType returnType, @NotNull RoutineType routineType, boolean hasArguments) {
        Block block = new Block.FunctionBlock(moduleName, name, returnType, routineType, hasArguments);
        enterBlock(block);
        return block;
    }

    public @NotNull Block enterField(@NotNull String moduleName, @NotNull String name, @NotNull RapidType returnType, @NotNull FieldType fieldType) {
        Block block = new Block.FieldBlock(moduleName, name, returnType, fieldType);
        enterBlock(block);
        return block;
    }

    private void enterBlock(@NotNull Block block) {
        if (currentBlock != null) {
            throw new IllegalStateException("Cannot visit block: " + block + " as previous block: " + currentBlock + " is still active");
        }
        this.currentBlock = block;
        this.currentLabels = new HashMap<>();
    }

    public void exitBlock() {
        if (currentBlock == null) {
            throw new IllegalStateException("Cannot exit block as no block is currently active");
        }
        if (currentBasicBlock != null) {
            throw new IllegalStateException("Cannot exit block as current scope: " + currentBasicBlock + " is still active");
        }
        controlFlow.setBlock(currentBlock);
        this.currentBlock = null;
        this.currentLabels = null;
    }

    public void enterBasicBlock(@NotNull StatementListType scopeType) {
        if (currentBlock == null) {
            throw new IllegalStateException("Cannot enter basicBlock as no block is currently active");
        }
        if (currentBasicBlock != null) {
            throw new IllegalStateException("Cannot enter basicBlock as previous basicBlock: " + currentBasicBlock + " is still active");
        }
        this.currentBasicBlock = currentBlock.setEntryBlock(scopeType);
    }

    public void enterBasicBlock(@Nullable List<Value> exceptions) {
        if (currentBlock == null) {
            throw new IllegalStateException("Cannot enter basicBlock as no block is currently active");
        }
        if (currentBasicBlock != null) {
            throw new IllegalStateException("Cannot enter basicBlock as previous basicBlock: " + currentBasicBlock + " is still active");
        }
        this.currentBasicBlock = currentBlock.setErrorClause(exceptions);
    }

    public @NotNull Value.Variable createVariable(@NotNull VariableKey variableKey, @NotNull RapidType type, @Nullable Object initialValue) {
        if (currentBlock == null) {
            throw new IllegalStateException("Cannot create variable as no block is currently active");
        }
        return variableKey.create(currentBlock, type, initialValue);
    }

    public void enterBasicBlock(@NotNull BasicBlock basicBlock) {
        if (currentBlock == null) {
            throw new IllegalStateException("Cannot enter basicBlock as no block is currently active");
        }
        if (currentBasicBlock != null) {
            throw new IllegalStateException("Cannot enter basicBlock as previous basicBlock: " + currentBasicBlock + " is still active");
        }
        if (!(basicBlock.getInstructions().isEmpty())) {
            throw new IllegalStateException("Cannot enter basicBlock: " + basicBlock + " as basicBlock is already complete");
        }
        this.currentBasicBlock = basicBlock;
    }


    public void exitBasicBlock(@NotNull BranchingInstruction branchingInstruction) {
        if (currentBasicBlock == null) {
            throw new IllegalStateException("Cannot exit scope as no scope is currently active");
        }
        currentBasicBlock.setTerminator(branchingInstruction);
        currentBasicBlock = null;
    }

    public void failScope(@Nullable PsiElement element) {
        if (currentBasicBlock == null) {
            throw new IllegalStateException("Cannot exit basicBlock as no basicBlock is currently active");
        }
        BasicBlock basicBlock = element != null ? createBasicBlock() : null;
        currentBasicBlock.setTerminator(new BranchingInstruction.ErrorInstruction(element, basicBlock));
        currentBasicBlock = basicBlock;
    }

    public void continueScope(@NotNull LinearInstruction linearInstruction) {
        if (currentBasicBlock == null) {
            throw new IllegalStateException("Cannot continue scope as no scope is currently active");
        }
        currentBasicBlock.getInstructions().add(linearInstruction);
    }

    public boolean isInsideScope() {
        return currentBasicBlock != null;
    }

    public void withArgumentGroup(@NotNull ArgumentGroup argumentGroup) {
        if (!(currentBlock instanceof Block.FunctionBlock functionBlock)) {
            throw new IllegalStateException("Cannot add argument: " + argumentGroup + " to: " + currentBlock);
        }
        List<ArgumentGroup> arguments = functionBlock.getArgumentGroups();
        arguments.add(argumentGroup);
    }

    public @NotNull Argument createArgument(@NotNull String name, @NotNull RapidType type, @NotNull ParameterType parameterType) {
        if (!(currentBlock instanceof Block.FunctionBlock)) {
            throw new IllegalStateException("Cannot add argument: " + name + " to: " + currentBlock);
        }
        return currentBlock.createArgument(name, type, parameterType);
    }

    public @NotNull BasicBlock createBasicBlock() {
        if (currentBlock == null) {
            throw new IllegalStateException("Could not create basicBlock as no block is currently active");
        }
        return currentBlock.createBasicBlock();
    }

    public void enterLabel(@NotNull PsiElement element, @NotNull String name) {
        if (currentLabels == null) {
            throw new IllegalStateException("Could not retrieve label as no block is currently active");
        }
        if (currentLabels.containsKey(name)) {
            BasicBlock basicBlock = currentLabels.get(name);
            exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(element, basicBlock));
            if (basicBlock.getInstructions().isEmpty()) {
                enterBasicBlock(basicBlock);
            }
        } else {
            BasicBlock basicBlock = createBasicBlock();
            currentLabels.put(name, basicBlock);
            exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(element, basicBlock));
            enterBasicBlock(basicBlock);
        }
    }

    public @Nullable Argument findArgument(@NotNull String name) {
        if (!(currentBlock instanceof Block.FunctionBlock functionBlock)) {
            throw new IllegalStateException("Could not find argument: " + name + " as no function block is currently active");
        }
        return functionBlock.findArgument(name);
    }

    public @Nullable Variable findVariable(@NotNull String name) {
        if (currentBlock == null) {
            throw new IllegalStateException("Could not find variable: " + name + " as no block is currently active");
        }
        return currentBlock.findVariable(name);
    }

    public @NotNull ControlFlow build() {
        return controlFlow;
    }

}

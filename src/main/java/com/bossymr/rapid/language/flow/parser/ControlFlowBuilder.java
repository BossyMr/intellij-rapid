package com.bossymr.rapid.language.flow.parser;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.flow.value.VariableExpression;
import com.bossymr.rapid.language.psi.StatementListType;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.bossymr.rapid.language.symbol.physical.PhysicalParameter;
import com.bossymr.rapid.language.symbol.physical.PhysicalParameterGroup;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.symbol.virtual.VirtualField;
import com.bossymr.rapid.language.symbol.virtual.VirtualParameter;
import com.bossymr.rapid.language.symbol.virtual.VirtualParameterGroup;
import com.bossymr.rapid.language.symbol.virtual.VirtualRoutine;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A {@code ControlFlowBuilder} is used to create a control flow graph.
 */
public class ControlFlowBuilder {

    private final @NotNull Project project;
    private final @NotNull Map<BlockDescriptor, Block> controlFlow = new HashMap<>();

    private Block currentBlock;
    private BasicBlock currentBasicBlock;
    private Map<String, BasicBlock> currentLabels;

    public ControlFlowBuilder(@NotNull Project project) {
        this.project = project;
    }

    public void enterFunction(@NotNull PhysicalRoutine routine, @NotNull String moduleName) {
        VirtualRoutine virtualRoutine = new VirtualRoutine(routine.getRoutineType(), moduleName, Objects.requireNonNull(routine.getName()), routine.getType(), new ArrayList<>());
        List<PhysicalParameterGroup> parameters = routine.getParameters();
        if (parameters != null) {
            for (PhysicalParameterGroup parameterGroup : parameters) {
                VirtualParameterGroup virtualParameterGroup = new VirtualParameterGroup(virtualRoutine, parameterGroup.isOptional(), new ArrayList<>());
                for (PhysicalParameter parameter : parameterGroup.getParameters()) {
                    virtualParameterGroup.getParameters().add(new VirtualParameter(virtualParameterGroup, parameter.getParameterType(), Objects.requireNonNull(parameter.getName()), Objects.requireNonNull(parameter.getType())));
                }
            }
        }
        Block block = new Block.FunctionBlock(virtualRoutine, moduleName);
        enterBlock(block);
    }

    public void enterField(@NotNull PhysicalField field, @NotNull String moduleName) {
        VirtualField virtualField = new VirtualField(field.getFieldType(), moduleName, Objects.requireNonNull(field.getName()), Objects.requireNonNull(field.getType()), field.isModifiable());
        Block block = new Block.FieldBlock(virtualField, moduleName);
        enterBlock(block);
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
        controlFlow.put(BlockDescriptor.getBlockKey(currentBlock), currentBlock);
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

    public void enterBasicBlock(@Nullable List<Integer> exceptions) {
        if (currentBlock == null) {
            throw new IllegalStateException("Cannot enter basicBlock as no block is currently active");
        }
        if (currentBasicBlock != null) {
            throw new IllegalStateException("Cannot enter basicBlock as previous basicBlock: " + currentBasicBlock + " is still active");
        }
        this.currentBasicBlock = currentBlock.setErrorClause(exceptions);
    }

    public @NotNull ReferenceExpression createVariable(@NotNull RapidType type) {
        return createVariable(null, null, type);
    }

    public @NotNull ReferenceExpression createVariable(@Nullable String name, @Nullable FieldType fieldType, @NotNull RapidType type) {
        if (currentBlock == null) {
            throw new IllegalStateException("Cannot create variable as no block is currently active");
        }
        Variable variable = currentBlock.createVariable(name, fieldType, type);
        return new VariableExpression(variable);
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
        currentBlock.getBasicBlocks().add(currentBasicBlock);
        currentBasicBlock = null;
    }

    public void failScope(@Nullable PsiElement element) {
        if (currentBasicBlock == null) {
            throw new IllegalStateException("Cannot exit basicBlock as no basicBlock is currently active");
        }
        BasicBlock basicBlock = element != null ? createBasicBlock() : null;
        exitBasicBlock(new BranchingInstruction.ErrorInstruction(element, basicBlock));
        if (basicBlock != null) {
            enterBasicBlock(basicBlock);
        }
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

    public @NotNull Argument findArgument(int index) {
        if (!(currentBlock instanceof Block.FunctionBlock functionBlock)) {
            throw new IllegalStateException("Could not find argument with index: " + index + " as no function block is currently active");
        }
        return functionBlock.findArgument(index);
    }

    public @Nullable Variable findVariable(@NotNull String name) {
        if (currentBlock == null) {
            throw new IllegalStateException("Could not find variable: " + name + " as no block is currently active");
        }
        return currentBlock.findVariable(name);
    }

    public @NotNull ControlFlow build() {
        return new ControlFlow(project, controlFlow);
    }

}

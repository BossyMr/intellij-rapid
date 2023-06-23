package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.conditon.Value;
import com.bossymr.rapid.language.flow.constraint.ConstantConstraint;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.constraint.MissingConstraint;
import com.bossymr.rapid.language.flow.constraint.OpenConstraint;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataFlowElementVisitor extends ControlFlowVisitor {

    private final @NotNull Map<Block, DataFlowFunction> functionValue = new HashMap<>();
    private DataFlowBlock currentBlock;

    private @NotNull DataFlowBlock createBlock(@NotNull Block block, @NotNull BasicBlock basicBlock, @NotNull DataFlowBlock predecessor) {
        DataFlowBlock dataFlowBlock = new DataFlowBlock(block, basicBlock);
        dataFlowBlock.predecessors().add(predecessor);
        predecessor.successors().add(dataFlowBlock);
        return dataFlowBlock;
    }

    public @NotNull DataFlow getDataFlow() {
        return null;
    }

    @Override
    public void visitFunctionBlock(@NotNull Block.FunctionBlock functionBlock) {
        super.visitFunctionBlock(functionBlock);
        for (ArgumentGroup argumentGroup : functionBlock.getArgumentGroups()) {
            for (Argument argument : argumentGroup.arguments()) {
                Constraint constraint;
                if (argumentGroup.isOptional()) {
                    constraint = new MissingConstraint(argument.type(), new OpenConstraint(argument.type()));
                } else {
                    constraint = new OpenConstraint(argument.type());
                }
                currentBlock.constraints().put(new Value.Variable.Local(argument.type(), argument.index()), constraint);
            }
        }
    }

    @Override
    public void visitBlock(@NotNull Block block) {
        currentBlock = new DataFlowBlock(block, block.getEntryBlock());
        for (Variable variable : block.getVariables()) {
            Constraint constraint = variable.value() != null ? new ConstantConstraint(variable.type(), Set.of(variable.value())) : Constraint.any(variable.type());
            currentBlock.constraints().put(new Value.Variable.Local(variable.type(), variable.index()), constraint);
        }
    }

    @Override
    public void visitBasicBlock(@NotNull BasicBlock basicBlock) {
        for (LinearInstruction instruction : basicBlock.getInstructions()) {
            instruction.accept(this);
        }
        basicBlock.getTerminator().accept(this);
    }

    @Override
    public void visitExitInstruction(@NotNull BranchingInstruction.ExitInstruction instruction) {
        // Do not do anything.
        // Let the current block exit without a successor.
    }

    @Override
    public void visitAssignmentInstruction(@NotNull LinearInstruction.AssignmentInstruction instruction) {
        Value.Variable variable = instruction.variable();
        if (DataFlowExpressionVisitor.containsVariable(instruction.value())) {
            currentBlock.expressions().put(variable, instruction.value());
        }
        currentBlock.constraints().put(variable, currentBlock.getConstraint(variable.type(), instruction.value()));
    }

    @Override
    public void visitReturnInstruction(@NotNull BranchingInstruction.ReturnInstruction instruction) {
        Block block = currentBlock.block();
        if (!(block instanceof Block.FunctionBlock functionBlock)) {
            Value value = instruction.value();
            if (value == null) {
                throw new IllegalStateException();
            }
            Constraint constraint = currentBlock.getConstraint(value);
            ((DataFlowPhysicalFunction) functionValue.get(block)).putOutput(Map.of(), new DataFlowFunctionOutput.Succeed(constraint));
        } else if (functionBlock.getReturnType() != null) {
            Value returnValue = instruction.value();
            if (returnValue == null) {
                throw new IllegalStateException();
            }
            Map<Integer, Constraint> value = getArgumentConstraints(block);
            ((DataFlowPhysicalFunction) functionValue.get(block)).putOutput(value, new DataFlowFunctionOutput.Succeed(currentBlock.getConstraint(returnValue)));
        }
    }

    @Override
    public void visitUnconditionalBranchingInstruction(@NotNull BranchingInstruction.UnconditionalBranchingInstruction instruction) {
        currentBlock = createBlock(currentBlock.block(), instruction.next(), currentBlock);
        instruction.next().accept(this);
    }

    @Override
    public void visitConditionalBranchingInstruction(@NotNull BranchingInstruction.ConditionalBranchingInstruction instruction) {
        Value.Variable value = instruction.value();

    }

    @Override
    public void visitThrowInstruction(@NotNull BranchingInstruction.ThrowInstruction instruction) {
        // Data flow is currently not supported for raise instructions.
        // The exact specification is complicated (see error recovery with long jump) and would require returning up
        // the stack while retaining the current stack. It would also require every call instruction be split into a
        // separate BasicBlock, as code execution should be able to retry that specific instruction (although technically
        // it shouldn't have an impact if the entire block is executed again).
        Block block = currentBlock.block();
        Map<Integer, Constraint> argumentConstraints = getArgumentConstraints(block);
        ((DataFlowPhysicalFunction) functionValue.get(block)).putOutput(argumentConstraints, new DataFlowFunctionOutput.Fail());
    }

    private @NotNull Map<Integer, Constraint> getArgumentConstraints(@NotNull Block block) {
        List<ArgumentGroup> argumentGroups = block.getArgumentGroups();
        List<Argument> arguments = argumentGroups.stream()
                .map(ArgumentGroup::arguments)
                .flatMap(List::stream)
                .toList();
        Map<Integer, Constraint> value = new HashMap<>();
        for (int i = 0; i < arguments.size(); i++) {
            Argument argument = arguments.get(i);
            Value.Variable.Local field = new Value.Variable.Local(argument.type(), argument.index());
            Constraint constraint = currentBlock.getConstraint(field);
            value.put(i, constraint);
        }
        return value;
    }

    @Override
    public void visitRetryInstruction(@NotNull BranchingInstruction.RetryInstruction instruction) {
        // See #visitThrowInstruction(...)
    }

    @Override
    public void visitTryNextInstruction(@NotNull BranchingInstruction.TryNextInstruction instruction) {
        // See #visitThrowInstruction(...)
    }
}

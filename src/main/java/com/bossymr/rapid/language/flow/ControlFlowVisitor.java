package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.*;
import org.jetbrains.annotations.NotNull;

public class ControlFlowVisitor {

    public void visitControlFlow(@NotNull ControlFlow controlFlow) {}

    public void visitFunctionBlock(@NotNull Block.FunctionBlock functionBlock) {
        visitBlock(functionBlock);
    }

    public void visitFieldBlock(@NotNull Block.FieldBlock fieldBlock) {
        visitBlock(fieldBlock);
    }

    public void visitBlock(@NotNull Block block) {}

    public void visitBasicBlock(@NotNull BasicBlock basicBlock) {}

    public void visitArgumentGroup(@NotNull ArgumentGroup argumentGroup) {}

    public void visitArgument(@NotNull Argument argument) {}

    public void visitVariable(@NotNull Variable variable) {}

    public void visitAssignmentInstruction(@NotNull LinearInstruction.AssignmentInstruction instruction) {
        visitLinearInstruction(instruction);
    }

    public void visitConnectInstruction(@NotNull LinearInstruction.ConnectInstruction instruction) {
        visitLinearInstruction(instruction);
    }

    public void visitLinearInstruction(@NotNull LinearInstruction instruction) {
        visitInstruction(instruction);
    }

    public void visitConditionalBranchingInstruction(@NotNull BranchingInstruction.ConditionalBranchingInstruction instruction) {
        visitBranchingInstruction(instruction);
    }

    public void visitUnconditionalBranchingInstruction(@NotNull BranchingInstruction.UnconditionalBranchingInstruction instruction) {
        visitBranchingInstruction(instruction);
    }

    public void visitRetryInstruction(@NotNull BranchingInstruction.RetryInstruction instruction) {
        visitBranchingInstruction(instruction);
    }

    public void visitTryNextInstruction(@NotNull BranchingInstruction.TryNextInstruction instruction) {
        visitBranchingInstruction(instruction);
    }

    public void visitReturnInstruction(@NotNull BranchingInstruction.ReturnInstruction instruction) {
        visitBranchingInstruction(instruction);
    }

    public void visitExitInstruction(@NotNull BranchingInstruction.ExitInstruction instruction) {
        visitBranchingInstruction(instruction);
    }

    public void visitThrowInstruction(@NotNull BranchingInstruction.ThrowInstruction instruction) {
        visitBranchingInstruction(instruction);
    }

    public void visitErrorInstruction(@NotNull BranchingInstruction.ErrorInstruction instruction) {
        visitBranchingInstruction(instruction);
    }

    public void visitCallInstruction(@NotNull BranchingInstruction.CallInstruction instruction) {
        visitBranchingInstruction(instruction);
    }

    public void visitBranchingInstruction(@NotNull BranchingInstruction instruction) {
        visitInstruction(instruction);
    }

    public void visitInstruction(@NotNull Instruction instruction) {}

    public void visitLocalVariableValue(@NotNull VariableValue value) {
        visitVariableValue(value);
    }

    public void visitFieldVariableValue(@NotNull FieldValue value) {
        visitVariableValue(value);
    }

    public void visitIndexVariableValue(@NotNull IndexValue value) {
        visitVariableValue(value);
    }

    public void visitComponentVariableValue(@NotNull ComponentValue component) {
        visitVariableValue(component);
    }

    public void visitVariableValue(@NotNull ReferenceValue value) {
        visitValue(value);
    }

    public void visitConstantValue(@NotNull ConstantValue value) {
        visitValue(value);
    }

    public void visitErrorValue(@NotNull ErrorValue value) {
        visitValue(value);
    }

    public void visitValue(@NotNull Value value) {}

    public void visitVariableExpression(@NotNull VariableExpression expression) {
        visitExpression(expression);
    }

    public void visitAggregateExpression(@NotNull AggregateExpression expression) {
        visitExpression(expression);
    }

    public void visitBinaryExpression(@NotNull BinaryExpression expression) {
        visitExpression(expression);
    }

    public void visitUnaryExpression(@NotNull UnaryExpression expression) {
        visitExpression(expression);
    }

    public void visitExpression(@NotNull Expression expression) {}
}

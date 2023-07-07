package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.*;
import org.jetbrains.annotations.NotNull;

public class ControlFlowVisitor<T> {

    public T visitControlFlow(@NotNull ControlFlow controlFlow) {
        return null;
    }

    public T visitFunctionBlock(@NotNull Block.FunctionBlock functionBlock) {
        return visitBlock(functionBlock);
    }

    public T visitFieldBlock(@NotNull Block.FieldBlock fieldBlock) {
        return visitBlock(fieldBlock);
    }

    public T visitBlock(@NotNull Block block) {
        return null;
    }

    public T visitBasicBlock(@NotNull BasicBlock basicBlock) {
        return null;
    }

    public T visitArgumentGroup(@NotNull ArgumentGroup argumentGroup) {
        return null;
    }

    public T visitArgument(@NotNull Argument argument) {
        return null;
    }

    public T visitVariable(@NotNull Variable variable) {
        return null;
    }

    public T visitAssignmentInstruction(@NotNull LinearInstruction.AssignmentInstruction instruction) {
        return visitLinearInstruction(instruction);
    }

    public T visitConnectInstruction(@NotNull LinearInstruction.ConnectInstruction instruction) {
        return visitLinearInstruction(instruction);
    }

    public T visitLinearInstruction(@NotNull LinearInstruction instruction) {
        return visitInstruction(instruction);
    }

    public T visitConditionalBranchingInstruction(@NotNull BranchingInstruction.ConditionalBranchingInstruction instruction) {
        return visitBranchingInstruction(instruction);
    }

    public T visitUnconditionalBranchingInstruction(@NotNull BranchingInstruction.UnconditionalBranchingInstruction instruction) {
        return visitBranchingInstruction(instruction);
    }

    public T visitRetryInstruction(@NotNull BranchingInstruction.RetryInstruction instruction) {
        return visitBranchingInstruction(instruction);
    }

    public T visitTryNextInstruction(@NotNull BranchingInstruction.TryNextInstruction instruction) {
        return visitBranchingInstruction(instruction);
    }

    public T visitReturnInstruction(@NotNull BranchingInstruction.ReturnInstruction instruction) {
        return visitBranchingInstruction(instruction);
    }

    public T visitExitInstruction(@NotNull BranchingInstruction.ExitInstruction instruction) {
        return visitBranchingInstruction(instruction);
    }

    public T visitThrowInstruction(@NotNull BranchingInstruction.ThrowInstruction instruction) {
        return visitBranchingInstruction(instruction);
    }

    public T visitErrorInstruction(@NotNull BranchingInstruction.ErrorInstruction instruction) {
        return visitBranchingInstruction(instruction);
    }

    public T visitCallInstruction(@NotNull BranchingInstruction.CallInstruction instruction) {
        return visitBranchingInstruction(instruction);
    }

    public T visitBranchingInstruction(@NotNull BranchingInstruction instruction) {
        return visitInstruction(instruction);
    }

    public T visitInstruction(@NotNull Instruction instruction) {
        return null;
    }

    public T visitLocalVariableValue(@NotNull VariableReference value) {
        return visitVariableValue(value);
    }

    public T visitFieldVariableValue(@NotNull FieldReference value) {
        return visitVariableValue(value);
    }

    public T visitIndexVariableValue(@NotNull IndexReference value) {
        return visitVariableValue(value);
    }

    public T visitComponentVariableValue(@NotNull ComponentReference component) {
        return visitVariableValue(component);
    }

    public T visitVariableValue(@NotNull ReferenceValue value) {
        return visitValue(value);
    }

    public T visitConstantValue(@NotNull ConstantValue value) {
        return visitValue(value);
    }

    public T visitErrorValue(@NotNull ErrorValue value) {
        return visitValue(value);
    }

    public T visitValue(@NotNull Value value) {
        return null;
    }

    public T visitVariableExpression(@NotNull VariableExpression expression) {
        return visitExpression(expression);
    }

    public T visitAggregateExpression(@NotNull AggregateExpression expression) {
        return visitExpression(expression);
    }

    public T visitBinaryExpression(@NotNull BinaryExpression expression) {
        return visitExpression(expression);
    }

    public T visitUnaryExpression(@NotNull UnaryExpression expression) {
        return visitExpression(expression);
    }

    public T visitExpression(@NotNull Expression expression) {
        return null;
    }
}

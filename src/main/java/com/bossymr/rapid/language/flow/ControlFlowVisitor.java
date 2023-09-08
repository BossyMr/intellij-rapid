package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.data.PathCounter;
import com.bossymr.rapid.language.flow.data.snapshots.ArraySnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.RecordSnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.*;
import org.jetbrains.annotations.NotNull;

public class ControlFlowVisitor<R> {

    public R visitControlFlow(@NotNull ControlFlow controlFlow) {
        return null;
    }

    public R visitFunctionBlock(@NotNull Block.FunctionBlock functionBlock) {
        return visitBlock(functionBlock);
    }

    public R visitFieldBlock(@NotNull Block.FieldBlock fieldBlock) {
        return visitBlock(fieldBlock);
    }

    public R visitBlock(@NotNull Block block) {
        return null;
    }

    public R visitBasicBlock(@NotNull BasicBlock basicBlock) {
        return null;
    }

    public R visitArgumentGroup(@NotNull ArgumentGroup argumentGroup) {
        return null;
    }

    public R visitArgument(@NotNull Argument argument) {
        return null;
    }

    public R visitVariable(@NotNull Variable variable) {
        return null;
    }

    public R visitAssignmentInstruction(@NotNull LinearInstruction.AssignmentInstruction instruction) {
        return visitLinearInstruction(instruction);
    }

    public R visitConnectInstruction(@NotNull LinearInstruction.ConnectInstruction instruction) {
        return visitLinearInstruction(instruction);
    }

    public R visitLinearInstruction(@NotNull LinearInstruction instruction) {
        return visitInstruction(instruction);
    }

    public R visitConditionalBranchingInstruction(@NotNull BranchingInstruction.ConditionalBranchingInstruction instruction) {
        return visitBranchingInstruction(instruction);
    }

    public R visitUnconditionalBranchingInstruction(@NotNull BranchingInstruction.UnconditionalBranchingInstruction instruction) {
        return visitBranchingInstruction(instruction);
    }

    public R visitRetryInstruction(@NotNull BranchingInstruction.RetryInstruction instruction) {
        return visitBranchingInstruction(instruction);
    }

    public R visitTryNextInstruction(@NotNull BranchingInstruction.TryNextInstruction instruction) {
        return visitBranchingInstruction(instruction);
    }

    public R visitReturnInstruction(@NotNull BranchingInstruction.ReturnInstruction instruction) {
        return visitBranchingInstruction(instruction);
    }

    public R visitExitInstruction(@NotNull BranchingInstruction.ExitInstruction instruction) {
        return visitBranchingInstruction(instruction);
    }

    public R visitThrowInstruction(@NotNull BranchingInstruction.ThrowInstruction instruction) {
        return visitBranchingInstruction(instruction);
    }

    public R visitErrorInstruction(@NotNull BranchingInstruction.ErrorInstruction instruction) {
        return visitBranchingInstruction(instruction);
    }

    public R visitCallInstruction(@NotNull BranchingInstruction.CallInstruction instruction) {
        return visitBranchingInstruction(instruction);
    }

    public R visitBranchingInstruction(@NotNull BranchingInstruction instruction) {
        return visitInstruction(instruction);
    }

    public R visitInstruction(@NotNull Instruction instruction) {
        return null;
    }

    public R visitLocalVariableValue(@NotNull VariableValue value) {
        return visitReferenceValue(value);
    }

    public R visitFieldVariableValue(@NotNull FieldValue value) {
        return visitReferenceValue(value);
    }

    public R visitComponentVariableValue(@NotNull ComponentValue component) {
        return visitReferenceValue(component);
    }

    public R visitIndexValue(@NotNull IndexValue value) {
        return visitReferenceValue(value);
    }

    public R visitReferenceValue(@NotNull ReferenceValue value) {
        return visitValue(value);
    }

    public R visitConstantValue(@NotNull ConstantValue value) {
        return visitValue(value);
    }

    public R visitErrorValue(@NotNull ErrorValue value) {
        return visitValue(value);
    }

    public R visitValue(@NotNull Value value) {
        return null;
    }

    public R visitValueExpression(@NotNull ValueExpression expression) {
        return visitExpression(expression);
    }

    public R visitAggregateExpression(@NotNull AggregateExpression expression) {
        return visitExpression(expression);
    }

    public R visitBinaryExpression(@NotNull BinaryExpression expression) {
        return visitExpression(expression);
    }

    public R visitUnaryExpression(@NotNull UnaryExpression expression) {
        return visitExpression(expression);
    }

    public R visitExpression(@NotNull Expression expression) {
        return null;
    }

    public R visitCondition(@NotNull Condition condition) {
        return null;
    }

    public R visitArraySnapshot(@NotNull ArraySnapshot snapshot) {
        return visitSnapshot(snapshot);
    }

    public R visitRecordSnapshot(@NotNull RecordSnapshot snapshot) {
        return visitSnapshot(snapshot);
    }

    public R visitVariableSnapshot(VariableSnapshot snapshot) {
        return visitSnapshot(snapshot);
    }

    public R visitSnapshot(@NotNull ReferenceSnapshot snapshot) {
        return null;
    }

    public R visitPathCounter(@NotNull PathCounter pathCounter) {
        return visitSnapshot(pathCounter);
    }
}

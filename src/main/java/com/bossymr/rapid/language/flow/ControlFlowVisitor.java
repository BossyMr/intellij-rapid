package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.expression.*;
import com.bossymr.rapid.language.flow.instruction.*;
import org.jetbrains.annotations.NotNull;

public class ControlFlowVisitor<R> {

    public R visitFunctionBlock(@NotNull Block.FunctionBlock functionBlock) {
        return visitBlock(functionBlock);
    }

    public R visitFieldBlock(@NotNull Block.FieldBlock fieldBlock) {
        return visitBlock(fieldBlock);
    }

    public R visitBlock(@NotNull Block block) {
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

    public R visitAssignmentInstruction(@NotNull AssignmentInstruction instruction) {
        return visitInstruction(instruction);
    }

    public R visitConnectInstruction(@NotNull ConnectInstruction instruction) {
        return visitInstruction(instruction);
    }

    public R visitConditionalBranchingInstruction(@NotNull ConditionalBranchingInstruction instruction) {
        return visitInstruction(instruction);
    }

    public R visitRetryInstruction(@NotNull RetryInstruction instruction) {
        return visitInstruction(instruction);
    }

    public R visitTryNextInstruction(@NotNull TryNextInstruction instruction) {
        return visitInstruction(instruction);
    }

    public R visitReturnInstruction(@NotNull ReturnInstruction instruction) {
        return visitInstruction(instruction);
    }

    public R visitExitInstruction(@NotNull ExitInstruction instruction) {
        return visitInstruction(instruction);
    }

    public R visitThrowInstruction(@NotNull ThrowInstruction instruction) {
        return visitInstruction(instruction);
    }

    public R visitErrorInstruction(@NotNull ErrorInstruction instruction) {
        return visitInstruction(instruction);
    }

    public R visitCallInstruction(@NotNull CallInstruction instruction) {
        return visitInstruction(instruction);
    }

    public R visitInstruction(@NotNull Instruction instruction) {
        return null;
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

    public R visitSnapshotExpression(@NotNull SnapshotExpression snapshot) {
        return visitReferenceExpression(snapshot);
    }

    public R visitConstantExpression(@NotNull LiteralExpression expression) {
        return visitExpression(expression);
    }

    public R visitIndexExpression(@NotNull IndexExpression expression) {
        return visitReferenceExpression(expression);
    }

    public R visitComponentExpression(@NotNull ComponentExpression expression) {
        return visitReferenceExpression(expression);
    }

    public R visitVariableExpression(@NotNull VariableExpression expression) {
        return visitReferenceExpression(expression);
    }

    public R visitReferenceExpression(@NotNull ReferenceExpression expression) {
        return visitExpression(expression);
    }

    public R visitFunctionCallExpression(@NotNull FunctionCallExpression expression) {
        return visitExpression(expression);
    }
}

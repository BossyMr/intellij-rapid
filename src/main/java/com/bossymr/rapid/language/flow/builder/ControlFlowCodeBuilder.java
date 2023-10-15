package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.RapidCodeBuilder;
import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.Variable;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.instruction.ReturnInstruction;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ControlFlowCodeBuilder implements RapidCodeBuilder {

    protected final @NotNull Block block;
    protected final @NotNull ControlFlowBlockBuilder builder;

    public ControlFlowCodeBuilder(@NotNull Block block, @NotNull ControlFlowBlockBuilder builder) {
        this.block = block;
        this.builder = builder;
    }

    public @NotNull ReferenceExpression createVariable(@NotNull RapidType type) {
        return createVariable(null, null, type);
    }

    public @NotNull ReferenceExpression createVariable(@Nullable String name, @Nullable FieldType fieldType, @NotNull RapidType type) {
        Variable variable = block.createVariable(name, fieldType, type);
        return new VariableExpression(variable);
    }

    @Override
    public @NotNull ReferenceExpression getVariable(@NotNull String name) {
        Variable variable = block.findVariable(name);
        if (variable == null) {
            throw new IllegalArgumentException();
        }
        return new VariableExpression(variable);
    }

    @Override
    public @NotNull ReferenceExpression getArgument(@NotNull String name) {
        Argument argument = block.findArgument(name);
        if (argument == null) {
            throw new IllegalArgumentException();
        }
        return new VariableExpression(argument);
    }

    @Override
    public @NotNull ReferenceExpression getField(@NotNull String moduleName, @NotNull String name, @NotNull RapidType valueType) {
        return new FieldExpression(valueType, moduleName, name);
    }

    @Override
    public @NotNull Expression aggregate(@Nullable RapidAggregateExpression element, @NotNull RapidType aggregateType, @NotNull List<? extends Expression> expressions) {
        return new AggregateExpression(element, aggregateType, expressions);
    }

    @Override
    public @NotNull Expression literal(@Nullable RapidLiteralExpression element, @NotNull Object value) {
        return new ConstantExpression(element, value);
    }

    @Override
    public @NotNull Expression binary(@Nullable RapidBinaryExpression element, @NotNull BinaryOperator operator, @NotNull Expression left, @NotNull Expression right) {
        return new BinaryExpression(element, operator, left, right);
    }

    @Override
    public @NotNull Expression unary(@Nullable RapidUnaryExpression element, @NotNull UnaryOperator operator, @NotNull Expression expression) {
        return new UnaryExpression(element, operator, expression);
    }

    @Override
    public @NotNull Expression error(@Nullable RapidElement element, @NotNull RapidType type) {
        return new VariableSnapshot(type);
    }

    @Override
    public void returnValue(@Nullable RapidReturnStatement statement, @Nullable Expression expression) {
        builder.continueScope(new ReturnInstruction(statement, expression));
    }
}

package com.bossymr.rapid.language.flow.condition;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * A {@code Condition} represents a condition which is always fulfilled.
 */
public class Condition {

    private final @NotNull ReferenceValue variable;
    private final @NotNull ConditionType conditionType;
    private @NotNull Expression expression;

    public Condition(@NotNull ReferenceValue variable, @NotNull ConditionType conditionType, @NotNull Expression expression) {
        if (expression instanceof AggregateExpression) {
            throw new IllegalArgumentException("Cannot create condition with aggregate expression");
        }
        this.variable = variable;
        this.conditionType = conditionType;
        this.expression = expression;
    }

    public static @NotNull Condition create(@NotNull LinearInstruction.AssignmentInstruction instruction) {
        return new Condition(instruction.variable(), ConditionType.EQUALITY, instruction.value());
    }

    @Contract(pure = true)
    public @NotNull List<Condition> getVariants() {
        List<Condition> result = new ArrayList<>();
        result.add(this);
        expression.accept(new SolveVisitor(result));
        return result;
    }

    @Contract(pure = true)
    public @NotNull Condition negate() {
        return new Condition(variable, conditionType.negate(), expression);
    }

    @Contract(pure = true)
    public @NotNull Condition flip() {
        return new Condition(variable, conditionType.flip(), expression);
    }

    public @NotNull ReferenceValue getVariable() {
        return variable;
    }

    public @NotNull ConditionType getConditionType() {
        return conditionType;
    }

    public @NotNull Expression getExpression() {
        return expression;
    }

    private void setExpression(@NotNull Expression expression) {
        this.expression = expression;
    }

    public boolean contains(@NotNull ReferenceValue variable) {
        AtomicBoolean atomicBoolean = new AtomicBoolean();
        expression.accept(ExpressionVisitor.visit(result -> atomicBoolean.set(atomicBoolean.get() || result.equals(variable))));
        return atomicBoolean.get();
    }

    public void iterate(@NotNull Function<ReferenceValue, ReferenceValue> function) {
        expression.accept(ExpressionVisitor.iterate(function, this::setExpression));
    }

    public void replace(@NotNull ReferenceValue target, @NotNull ReferenceValue variable) {
        expression.accept(ExpressionVisitor.iterate(result -> {
            if (result.equals(target)) {
                return variable;
            } else {
                return result;
            }
        }, this::setExpression));
    }

    public @NotNull List<ReferenceValue> getVariables() {
        List<ReferenceValue> variables = new ArrayList<>();
        expression.accept(ExpressionVisitor.visit(variables::add));
        return variables;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Condition condition = (Condition) o;
        return Objects.equals(variable, condition.variable) && conditionType == condition.conditionType && Objects.equals(expression, condition.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable, conditionType, expression);
    }

    @Override
    public String toString() {
        return "Condition{" +
                "variable=" + variable +
                ", conditionType=" + conditionType +
                ", expression=" + expression +
                '}';
    }

    private class SolveVisitor extends ControlFlowVisitor {

        private final @NotNull List<Condition> conditions;

        public SolveVisitor(@NotNull List<Condition> conditions) {
            this.conditions = conditions;
        }

        @Override
        public void visitVariableExpression(@NotNull VariableExpression expression) {
            if (expression.value() instanceof ReferenceValue value) {
                conditions.add(new Condition(value, conditionType.flip(), new VariableExpression(variable)));
            }
            super.visitVariableExpression(expression);
        }

        @Override
        public void visitAggregateExpression(@NotNull AggregateExpression expression) {
            List<Value> values = expression.values();
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i) instanceof ReferenceValue value) {
                    IndexValue index = new IndexValue(variable, new ConstantValue(RapidType.NUMBER, i));
                    conditions.add(new Condition(value, ConditionType.EQUALITY, new VariableExpression(index)));
                }
            }
            super.visitAggregateExpression(expression);
        }

        @Override
        public void visitBinaryExpression(@NotNull BinaryExpression expression) {
            if (expression.left() instanceof ReferenceValue value) {
                Condition condition = switch (expression.operator()) {
                    case ADD ->
                            new Condition(value, conditionType.flip(), new BinaryExpression(BinaryOperator.SUBTRACT, variable, expression.right()));
                    case SUBTRACT ->
                            new Condition(value, conditionType.flip(), new BinaryExpression(BinaryOperator.ADD, variable, expression.right()));
                    case MULTIPLY ->
                            new Condition(value, conditionType.flip(), new BinaryExpression(BinaryOperator.DIVIDE, variable, expression.right()));
                    case DIVIDE ->
                            new Condition(value, conditionType.flip(), new BinaryExpression(BinaryOperator.MULTIPLY, variable, expression.right()));
                    default -> null;
                };
                if (condition != null) {
                    conditions.add(condition);
                }
            }
            if (expression.right() instanceof ReferenceValue value) {
                Condition condition = switch (expression.operator()) {
                    case ADD ->
                            new Condition(value, conditionType.flip(), new BinaryExpression(BinaryOperator.SUBTRACT, variable, expression.left()));
                    case SUBTRACT ->
                            new Condition(value, conditionType.flip(), new BinaryExpression(BinaryOperator.ADD, expression.left(), variable));
                    case MULTIPLY ->
                            new Condition(value, conditionType.flip(), new BinaryExpression(BinaryOperator.DIVIDE, variable, expression.right()));
                    case DIVIDE ->
                            new Condition(value, conditionType.flip(), new BinaryExpression(BinaryOperator.DIVIDE, expression.left(), variable));
                    default -> null;
                };
                if (condition != null) {
                    conditions.add(condition);
                }
            }
            super.visitBinaryExpression(expression);
        }

        @Override
        public void visitUnaryExpression(@NotNull UnaryExpression expression) {
            if (expression.value() instanceof ReferenceValue value) {
                conditions.add(new Condition(value, conditionType.flip(), new UnaryExpression(expression.operator(), variable)));
            }
            super.visitUnaryExpression(expression);
        }
    }
}

package com.bossymr.rapid.language.flow.condition;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
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
        this.variable = variable;
        this.conditionType = conditionType;
        this.expression = expression;
    }

    public static @NotNull Condition create(@NotNull LinearInstruction.AssignmentInstruction instruction) {
        return new Condition(instruction.variable(), ConditionType.EQUALITY, instruction.value());
    }

    @Contract(pure = true)
    public @NotNull List<Condition> solve() {
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

    @Contract(pure = true)
    public @NotNull Condition copy() {
        return new Condition(variable, conditionType, expression);
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
        expression.accept(new ExpressionVisitor(result -> {
            atomicBoolean.set(atomicBoolean.get() || result.equals(variable));
            return result;
        }));
        return atomicBoolean.get();
    }

    public void replace(@NotNull ReferenceValue target, @NotNull ReferenceValue variable) {
        expression.accept(new ExpressionVisitor(result -> result.equals(target) ? variable : result));
    }

    public @NotNull List<ReferenceValue> collect() {
        List<ReferenceValue> variables = new ArrayList<>();
        expression.accept(new ExpressionVisitor(result -> {
            variables.add(result);
            return result;
        }));
        return variables;
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
                    IndexReference index = new IndexReference(value.type(), variable, new ConstantValue(RapidType.NUMBER, i));
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
                conditions.add(new Condition(value, conditionType.flip(), new VariableExpression(variable)));
            }
            super.visitUnaryExpression(expression);
        }
    }

    private class ExpressionVisitor extends ControlFlowVisitor {

        private final @NotNull Function<ReferenceValue, ReferenceValue> function;

        public ExpressionVisitor(@NotNull Function<ReferenceValue, ReferenceValue> function) {
            this.function = function;
        }

        @Override
        public void visitVariableExpression(@NotNull VariableExpression expression) {
            Value value = computeValue(expression.value());
            if (!value.equals(variable)) {
                setExpression(new VariableExpression(value));
            }
            super.visitVariableExpression(expression);
        }

        @Override
        public void visitAggregateExpression(@NotNull AggregateExpression expression) {
            List<Value> values = expression.values().stream()
                    .map(this::computeValue).toList();
            if (!values.equals(expression.values())) {
                setExpression(new AggregateExpression(values));
            }
            super.visitAggregateExpression(expression);
        }

        private @NotNull Value computeValue(@NotNull Value value) {
            if (!(value instanceof ReferenceValue cast)) {
                return value;
            }
            return function.apply(cast);
        }

        @Override
        public void visitBinaryExpression(@NotNull BinaryExpression expression) {
            Value left = computeValue(expression.left());
            Value right = computeValue(expression.right());
            if (!left.equals(expression.left()) || !right.equals(expression.right())) {
                setExpression(new BinaryExpression(expression.operator(), left, right));
            }
            super.visitBinaryExpression(expression);
        }

        @Override
        public void visitUnaryExpression(@NotNull UnaryExpression expression) {
            Value value = computeValue(expression.value());
            if (!value.equals(expression.value())) {
                setExpression(new UnaryExpression(expression.operator(), expression.value()));
            }
            super.visitUnaryExpression(expression);
        }
    }

}

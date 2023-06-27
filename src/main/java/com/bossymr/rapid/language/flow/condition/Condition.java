package com.bossymr.rapid.language.flow.condition;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.Operator;
import com.bossymr.rapid.language.flow.value.Value;
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

    private final @NotNull Value.Variable variable;
    private final @NotNull ConditionType conditionType;
    private @NotNull Expression expression;

    public Condition(@NotNull Value.Variable variable, @NotNull ConditionType conditionType, @NotNull Expression expression) {
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

    public @NotNull Value.Variable getVariable() {
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

    public boolean contains(@NotNull Value.Variable variable) {
        AtomicBoolean atomicBoolean = new AtomicBoolean();
        expression.accept(new ExpressionVisitor(result -> {
            atomicBoolean.set(atomicBoolean.get() || result.equals(variable));
            return result;
        }));
        return atomicBoolean.get();
    }

    public void replace(@NotNull Value.Variable target, @NotNull Value.Variable variable) {
        expression.accept(new ExpressionVisitor(result -> result.equals(target) ? variable : result));
    }

    public @NotNull List<Value.Variable> collect() {
        List<Value.Variable> variables = new ArrayList<>();
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
        public void visitVariableExpression(@NotNull Expression.Variable expression) {
            if (expression.value() instanceof Value.Variable value) {
                conditions.add(new Condition(value, conditionType.flip(), new Expression.Variable(variable)));
            }
            super.visitVariableExpression(expression);
        }

        @Override
        public void visitAggregateExpression(@NotNull Expression.Aggregate expression) {
            List<Value> values = expression.values();
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i) instanceof Value.Variable value) {
                    Value.Variable.Index index = new Value.Variable.Index(value.type(), variable, new Value.Constant(RapidType.NUMBER, i));
                    conditions.add(new Condition(value, ConditionType.EQUALITY, new Expression.Variable(index)));
                }
            }
            super.visitAggregateExpression(expression);
        }

        @Override
        public void visitBinaryExpression(@NotNull Expression.Binary expression) {
            if (expression.left() instanceof Value.Variable value) {
                Condition condition = switch (expression.operator()) {
                    case ADD ->
                            new Condition(value, conditionType.flip(), new Expression.Binary(Operator.BinaryOperator.SUBTRACT, variable, expression.right()));
                    case SUBTRACT ->
                            new Condition(value, conditionType.flip(), new Expression.Binary(Operator.BinaryOperator.ADD, variable, expression.right()));
                    case MULTIPLY ->
                            new Condition(value, conditionType.flip(), new Expression.Binary(Operator.BinaryOperator.DIVIDE, variable, expression.right()));
                    case DIVIDE ->
                            new Condition(value, conditionType.flip(), new Expression.Binary(Operator.BinaryOperator.MULTIPLY, variable, expression.right()));
                    default -> null;
                };
                if (condition != null) {
                    conditions.add(condition);
                }
            }
            if (expression.right() instanceof Value.Variable value) {
                Condition condition = switch (expression.operator()) {
                    case ADD ->
                            new Condition(value, conditionType.flip(), new Expression.Binary(Operator.BinaryOperator.SUBTRACT, variable, expression.left()));
                    case SUBTRACT ->
                            new Condition(value, conditionType.flip(), new Expression.Binary(Operator.BinaryOperator.ADD, expression.left(), variable));
                    case MULTIPLY ->
                            new Condition(value, conditionType.flip(), new Expression.Binary(Operator.BinaryOperator.DIVIDE, variable, expression.right()));
                    case DIVIDE ->
                            new Condition(value, conditionType.flip(), new Expression.Binary(Operator.BinaryOperator.DIVIDE, expression.left(), variable));
                    default -> null;
                };
                if (condition != null) {
                    conditions.add(condition);
                }
            }
            super.visitBinaryExpression(expression);
        }

        @Override
        public void visitUnaryExpression(@NotNull Expression.Unary expression) {
            if (expression.value() instanceof Value.Variable value) {
                conditions.add(new Condition(value, conditionType.flip(), new Expression.Variable(variable)));
            }
            super.visitUnaryExpression(expression);
        }
    }

    private class ExpressionVisitor extends ControlFlowVisitor {

        private final @NotNull Function<Value.Variable, Value.Variable> function;

        public ExpressionVisitor(@NotNull Function<Value.Variable, Value.Variable> function) {
            this.function = function;
        }

        @Override
        public void visitVariableExpression(@NotNull Expression.Variable expression) {
            Value value = computeValue(expression.value());
            if (!value.equals(variable)) {
                setExpression(new Expression.Variable(value));
            }
            super.visitVariableExpression(expression);
        }

        @Override
        public void visitAggregateExpression(@NotNull Expression.Aggregate expression) {
            List<Value> values = expression.values().stream()
                    .map(this::computeValue).toList();
            if (!values.equals(expression.values())) {
                setExpression(new Expression.Aggregate(values));
            }
            super.visitAggregateExpression(expression);
        }

        private @NotNull Value computeValue(@NotNull Value value) {
            if (!(value instanceof Value.Variable cast)) {
                return value;
            }
            return function.apply(cast);
        }

        @Override
        public void visitBinaryExpression(@NotNull Expression.Binary expression) {
            Value left = computeValue(expression.left());
            Value right = computeValue(expression.right());
            if (!left.equals(expression.left()) || !right.equals(expression.right())) {
                setExpression(new Expression.Binary(expression.operator(), left, right));
            }
            super.visitBinaryExpression(expression);
        }

        @Override
        public void visitUnaryExpression(@NotNull Expression.Unary expression) {
            Value value = computeValue(expression.value());
            if (!value.equals(expression.value())) {
                setExpression(new Expression.Unary(expression.operator(), expression.value()));
            }
            super.visitUnaryExpression(expression);
        }
    }

}

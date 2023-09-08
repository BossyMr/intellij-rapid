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
        this.variable = variable;
        this.conditionType = conditionType;
        this.expression = expression;
    }

    public static @NotNull Condition create(@NotNull LinearInstruction.AssignmentInstruction instruction) {
        return new Condition(instruction.variable(), ConditionType.EQUALITY, instruction.value());
    }

    public static @NotNull Condition create(@NotNull Condition condition, @NotNull Function<ReferenceValue, ReferenceValue> variableMapper, @NotNull Function<ReferenceValue, Value> expressionMapper) {
        return new Condition(variableMapper.apply(condition.getVariable()), condition.getConditionType(), ExpressionVisitor.modify(condition.getExpression(), expressionMapper));
    }

    public static @NotNull Condition create(@NotNull Condition condition, @NotNull Function<ReferenceValue, Value> mapper) {
        return create(condition, (variable) -> {
            Value value = mapper.apply(variable);
            if (!(value instanceof ReferenceValue referenceValue)) {
                throw new IllegalArgumentException("Cannot replace variable for condition: " + condition + " to: " + value);
            }
            return referenceValue;
        }, mapper);
    }

    @Contract(pure = true)
    public @NotNull List<Condition> getVariants() {
        ArrayList<Condition> conditions = new ArrayList<>(expression.accept(new SolveVisitor()));
        conditions.add(this);
        return conditions;
    }

    @Contract(pure = true)
    public @NotNull Condition negate() {
        return new Condition(variable, conditionType.negate(), expression);
    }

    public @NotNull ReferenceValue getVariable() {
        return variable;
    }

    public @NotNull ConditionType getConditionType() {
        return conditionType;
    }

    @Contract(pure = true)
    public @NotNull Expression getExpression() {
        return expression;
    }

    private void setExpression(@NotNull Expression expression) {
        this.expression = expression;
    }

    public boolean contains(@NotNull ReferenceValue variable) {
        AtomicBoolean atomicBoolean = new AtomicBoolean();
        ExpressionVisitor.iterate(expression, result -> atomicBoolean.set(atomicBoolean.get() || result.equals(variable)));
        return atomicBoolean.get();
    }

    @Contract(pure = true)
    public @NotNull Condition modify(@NotNull Function<ReferenceValue, Value> mapper) {
        Condition condition = new Condition(getVariable(), getConditionType(), getExpression());
        condition.setExpression(ExpressionVisitor.modify(condition.getExpression(), mapper));
        return condition;
    }

    public @NotNull List<ReferenceValue> getVariables() {
        List<ReferenceValue> variables = new ArrayList<>();
        ExpressionVisitor.iterate(expression, variables::add);
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

    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitCondition(this);
    }

    private class SolveVisitor extends ControlFlowVisitor<List<Condition>> {

        @Override
        public @NotNull List<Condition> visitValueExpression(@NotNull ValueExpression expression) {
            if (expression.value() instanceof ReferenceValue value) {
                return List.of(new Condition(value, conditionType.flip(), new ValueExpression(variable)));
            }
            return List.of();
        }

        @Override
        public @NotNull List<Condition> visitAggregateExpression(@NotNull AggregateExpression expression) {
            List<Value> values = expression.values();
            List<Condition> conditions = new ArrayList<>();
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i) instanceof ReferenceValue value) {
                    IndexValue index = new IndexValue(variable, ConstantValue.of(RapidType.NUMBER, i));
                    conditions.add(new Condition(value, ConditionType.EQUALITY, new ValueExpression(index)));
                }
            }
            return conditions;
        }

        @Override
        public @NotNull List<Condition> visitBinaryExpression(@NotNull BinaryExpression expression) {
            List<Condition> conditions = new ArrayList<>();
            if (expression.left() instanceof ReferenceValue value) {
                BinaryExpression binaryExpression = switch (expression.operator()) {
                    case ADD -> new BinaryExpression(BinaryOperator.SUBTRACT, variable, expression.right());
                    case SUBTRACT -> new BinaryExpression(BinaryOperator.ADD, variable, expression.right());
                    case MULTIPLY -> new BinaryExpression(BinaryOperator.DIVIDE, variable, expression.right());
                    case DIVIDE -> new BinaryExpression(BinaryOperator.MULTIPLY, variable, expression.right());
                    default -> null;
                };
                if (binaryExpression != null) {
                    conditions.add(new Condition(value, conditionType.flip(), binaryExpression));
                }
            }
            if (expression.right() instanceof ReferenceValue value) {
                BinaryExpression binaryExpression = switch (expression.operator()) {
                    case ADD -> new BinaryExpression(BinaryOperator.SUBTRACT, variable, expression.left());
                    case SUBTRACT -> new BinaryExpression(BinaryOperator.ADD, expression.left(), variable);
                    case MULTIPLY -> new BinaryExpression(BinaryOperator.DIVIDE, variable, expression.right());
                    case DIVIDE -> new BinaryExpression(BinaryOperator.DIVIDE, expression.left(), variable);
                    default -> null;
                };
                if (binaryExpression != null) {
                    conditions.add(new Condition(value, conditionType.flip(), binaryExpression));
                }
            }
            return conditions;
        }

        @Override
        public @NotNull List<Condition> visitUnaryExpression(@NotNull UnaryExpression expression) {
            if (expression.value() instanceof ReferenceValue value) {
                return List.of(new Condition(value, conditionType.flip(), new UnaryExpression(expression.operator(), variable)));
            }
            return List.of();
        }
    }
}

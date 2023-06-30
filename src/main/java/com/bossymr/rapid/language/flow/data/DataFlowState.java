package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.constraint.*;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * A {@code DataFlowState} represents the state of the program at a specific point.
 * <pre>{@code
 * Block 0:
 * 0: if(value) -> (true: 1, false: 2)
 *
 * Block 1:     // State:
 * 0: x = 0;    // x1 = 0;
 * 1: z = 0;    // z1 = 0;
 * 2: goto 3;
 *
 * Block 2:     // State:
 * 0: x = 1;    // x1 = 0;
 * 1: z = 1;    // z1 = 1;
 * 2: goto 3;
 *
 * Block 3:                         // State 1:         State 2:
 * 0: y = (x == 0);                 // y = (x == 0);    y = (x == 0);
 * 1: if(y) -> (true: 4, false: 5)  // x1 = 0;          x1 = 1;
 *                                  // z1 = 0;          z1 = 1;
 *
 * Block 4:     // State:
 *              // x1 = 0;
 *              // z1 = 0;
 * }</pre>
 */
public record DataFlowState(@NotNull List<Condition> conditions, @NotNull Map<ReferenceValue, ReferenceValue> snapshots) {

    /**
     * Returns the newest snapshot of the specified value.
     * <p>
     * The below example shows how snapshots are used. After a variable is modified, other variables can still reference
     * the state of the variable before it was modified.
     * <pre>{@code
     * 0: x = y + 5     // x1 = y1 + 5
     * 1: z = y + x     // z1 = y1 + x1
     * 2: y = 2         // y2 = 2
     * }</pre>
     *
     * @param value the variable.
     * @return the newest snapshot variable of the specified variable.
     */
    public @NotNull ReferenceValue getValue(@NotNull ReferenceValue value) {
        return snapshots.getOrDefault(value, value);
    }

    /**
     * Returns all conditions which apply to the specified variable.
     *
     * @param value the variable.
     * @return all conditions which apply to the specified variable.
     */
    public @NotNull List<Condition> getConditions(@NotNull ReferenceValue value) {
        // If the variable is not a snapshot, fetch the latest snapshot for the variable.
        ReferenceValue referenceValue = getValue(value);
        return conditions().stream()
                .filter(condition -> condition.getVariable().equals(referenceValue))
                .toList();
    }

    /**
     * Calculates the constraint of the specified condition.
     *
     * @param condition the condition.
     * @return the constraint.
     */
    public @NotNull Constraint getConstraint(@NotNull Condition condition) {
        Constraint expression = getConstraint(condition.getExpression());
        return switch (condition.getConditionType()) {
            case EQUALITY -> expression;
            case INEQUALITY -> {
                if (expression instanceof NumericConstraint numericConstraint) {
                    Double point = numericConstraint.getPoint();
                    if (point != null) {
                        yield numericConstraint.negate();
                    }
                    yield NumericConstraint.any();
                }
                if (expression instanceof BooleanConstraint booleanConstraint) {
                    yield booleanConstraint.negate();
                }
                if (expression instanceof StringConstraint stringConstraint) {
                    if (stringConstraint.sequences().size() == 1) {
                        yield stringConstraint.negate();
                    } else {
                        yield new InverseStringConstraint(Optionality.PRESENT, Set.of());
                    }
                }
                if (expression instanceof InverseStringConstraint) {
                    yield new InverseStringConstraint(Optionality.PRESENT, Set.of());
                }
                if (expression instanceof OpenConstraint) {
                    yield new OpenConstraint(Optionality.PRESENT);
                }
                throw new AssertionError();
            }
            case LESS_THAN -> {
                if (!(expression instanceof NumericConstraint numericConstraint)) {
                    throw new IllegalStateException();
                }
                yield NumericConstraint.lessThan(numericConstraint.getMaximum().value());
            }
            case LESS_THAN_OR_EQUAL -> {
                if (!(expression instanceof NumericConstraint numericConstraint)) {
                    throw new IllegalStateException();
                }
                yield NumericConstraint.lessThanOrEqual(numericConstraint.getMaximum().value());
            }
            case GREATER_THAN -> {
                if (!(expression instanceof NumericConstraint numericConstraint)) {
                    throw new IllegalStateException();
                }
                yield NumericConstraint.greaterThan(numericConstraint.getMinimum().value());
            }
            case GREATER_THAN_OR_EQUAL -> {
                if (!(expression instanceof NumericConstraint numericConstraint)) {
                    throw new IllegalStateException();
                }
                yield NumericConstraint.greaterThanOrEqual(numericConstraint.getMinimum().value());
            }
        };
    }


    /**
     * Calculates the constraint of the value of the specified expression.
     *
     * @param expression the expression.
     * @return the constraint.
     */
    public @NotNull Constraint getConstraint(@NotNull Expression expression) {
        ExpressionVisitor visitor = new ExpressionVisitor();
        expression.accept(visitor);
        return visitor.getResult();
    }

    /**
     * Calculates the constraint of the specified value.
     *
     * @param value the value.
     * @return the constraint.
     */
    public @NotNull Constraint getConstraint(@NotNull Value value) {
        if (value instanceof ReferenceValue variable) {
            return getConstraint(variable);
        }
        if (value instanceof ErrorValue) {
            return Constraint.getTopConstraint(value.type());
        }
        if (value instanceof ConstantValue constant) {
            Object object = constant.value();
            if (value.type().isAssignable(RapidType.STRING)) {
                return new StringConstraint(Optionality.PRESENT, Set.of(object.toString()));
            }
            if (value.type().isAssignable(RapidType.NUMBER)) {
                return NumericConstraint.equalTo((double) object);
            }
            if (value.type().isAssignable(RapidType.BOOLEAN)) {
                BooleanConstraint.BooleanValue booleanValue = BooleanConstraint.BooleanValue.get((boolean) object);
                return new BooleanConstraint(Optionality.PRESENT, booleanValue);
            }
        }
        throw new AssertionError();
    }

    /**
     * Calculates the constraint of the specified variable.
     *
     * @param variable the variable.
     * @return the constraint.
     */
    public @NotNull Constraint getConstraint(@NotNull ReferenceValue variable) {
        List<Condition> conditions = getConditions(variable);
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Constraint constraint = getConstraint(conditions.get(0));
        for (int i = 1; i < conditions.size(); i++) {
            constraint = constraint.or(getConstraint(conditions.get(i)));
        }
        return constraint;
    }

    private class ExpressionVisitor extends ControlFlowVisitor {

        private Constraint constraint;

        public Constraint getResult() {
            return constraint;
        }

        @Override
        public void visitVariableExpression(@NotNull VariableExpression expression) {
            constraint = getConstraint(expression.value());
        }

        @Override
        public void visitAggregateExpression(@NotNull AggregateExpression expression) {
            throw new IllegalStateException();
        }

        @Override
        public void visitBinaryExpression(@NotNull BinaryExpression expression) {
            Constraint left = getConstraint(expression.left());
            Constraint right = getConstraint(expression.right());
            constraint = calculateConstraint(expression.operator(), left, right);
        }

        private @NotNull Constraint calculateConstraint(@NotNull BinaryOperator operator, @NotNull Constraint left, @NotNull Constraint right) {
            return switch (operator) {
                case ADD, SUBTRACT, MULTIPLY, DIVIDE, INTEGER_DIVIDE, MODULO -> {
                    if (!(left instanceof NumericConstraint leftRange) || !(right instanceof NumericConstraint rightRange)) {
                        throw new AssertionError();
                    }
                    yield calculateNumberConstraint(operator, leftRange, rightRange);
                }
                case LESS_THAN, EQUAL_TO, GREATER_THAN, LESS_THAN_OR_EQUAL, NOT_EQUAL_TO, GREATER_THAN_OR_EQUAL -> {
                    if (!(left instanceof NumericConstraint leftRange) || !(right instanceof NumericConstraint rightRange)) {
                        throw new AssertionError();
                    }
                    yield calculateBooleanRangeConstraint(operator, leftRange, rightRange);
                }
                case AND, XOR, OR -> {
                    if (!(left instanceof BooleanConstraint leftConstant) || !(right instanceof BooleanConstraint rightConstant)) {
                        throw new AssertionError();
                    }
                    Boolean leftValue = leftConstant.getValue().get();
                    Boolean rightValue = rightConstant.getValue().get();
                    boolean bothTrue = Boolean.TRUE.equals(leftValue) && Boolean.TRUE.equals(rightValue);
                    boolean bothFalse = Boolean.FALSE.equals(leftValue) && Boolean.FALSE.equals(rightValue);
                    boolean anyTrue = Boolean.TRUE.equals(leftValue) || Boolean.TRUE.equals(rightValue);
                    boolean anyFalse = Boolean.FALSE.equals(leftValue) || Boolean.FALSE.equals(rightValue);
                    if (operator == BinaryOperator.OR) {
                        if (anyTrue) {
                            yield new BooleanConstraint(BooleanConstraint.BooleanValue.ALWAYS_TRUE);
                        }
                        if (bothFalse) {
                            yield new BooleanConstraint(BooleanConstraint.BooleanValue.ALWAYS_FALSE);
                        }
                    }
                    if (operator == BinaryOperator.AND) {
                        if (bothTrue) {
                            yield new BooleanConstraint(BooleanConstraint.BooleanValue.ALWAYS_TRUE);
                        }
                        if (anyFalse) {
                            yield new BooleanConstraint(BooleanConstraint.BooleanValue.ALWAYS_FALSE);
                        }
                    }
                    if (operator == BinaryOperator.XOR) {
                        if (bothTrue) {
                            yield new BooleanConstraint(BooleanConstraint.BooleanValue.ALWAYS_FALSE);
                        }
                        if (bothFalse) {
                            yield new BooleanConstraint(BooleanConstraint.BooleanValue.ALWAYS_FALSE);
                        }
                        if (anyTrue) {
                            yield new BooleanConstraint(BooleanConstraint.BooleanValue.ALWAYS_FALSE);
                        }
                    }
                    yield BooleanConstraint.any();
                }
            };
        }

        private @NotNull NumericConstraint calculateNumberConstraint(@NotNull BinaryOperator operator, @NotNull NumericConstraint left, @NotNull NumericConstraint right) {
            return switch (operator) {
                case ADD -> calculateForOperator(left, right, Double::sum);
                case SUBTRACT -> calculateForOperator(left, right, (a, b) -> a - b);
                case MULTIPLY -> calculateForOperator(left, right, (a, b) -> a * b);
                case DIVIDE -> calculateForOperator(left, right, (a, b) -> a / b);
                case INTEGER_DIVIDE ->
                        calculateForOperator(left, right, (a, b) -> ((double) (a.intValue() / b.intValue())));
                case MODULO -> calculateForOperator(left, right, (a, b) -> a % b);
                default -> throw new IllegalStateException("Unexpected value: " + operator);
            };
        }

        private @NotNull NumericConstraint calculateForOperator(@NotNull NumericConstraint left, @NotNull NumericConstraint right, @NotNull BiFunction<Double, Double, Double> newValue) {
            NumericConstraint rangeConstraint = new NumericConstraint(Optionality.PRESENT);
            for (NumericConstraint.Range leftRange : left.getRanges()) {
                for (NumericConstraint.Range rightRange : right.getRanges()) {
                    NumericConstraint.Bound lower = new NumericConstraint.Bound(leftRange.lower().isInclusive() && rightRange.lower().isInclusive(), newValue.apply(leftRange.lower().value(), rightRange.lower().value()));
                    NumericConstraint.Bound upper = new NumericConstraint.Bound(leftRange.upper().isInclusive() && rightRange.upper().isInclusive(), newValue.apply(leftRange.upper().value(), rightRange.upper().value()));
                    rangeConstraint = rangeConstraint.or(new NumericConstraint(Optionality.PRESENT, lower, upper));
                }
            }
            return rangeConstraint;
        }

        private @NotNull BooleanConstraint calculateBooleanRangeConstraint(@NotNull BinaryOperator operator, @NotNull NumericConstraint left, @NotNull NumericConstraint right) {
            NumericConstraint.Bound leftLower = left.getRanges().get(0).lower();
            NumericConstraint.Bound leftUpper = left.getRanges().get(left.getRanges().size() - 1).upper();
            NumericConstraint.Bound rightLower = right.getRanges().get(0).lower();
            NumericConstraint.Bound rightUpper = right.getRanges().get(right.getRanges().size() - 1).upper();
            NumericConstraint.Range leftRange = new NumericConstraint.Range(leftLower, leftUpper);
            NumericConstraint.Range rightRange = new NumericConstraint.Range(rightLower, rightUpper);
            return switch (operator) {
                case LESS_THAN -> {
                    // is the largest value in left smaller than the smallest value in right
                    Boolean smaller = isSmaller(leftRange, rightRange);
                    if (smaller != null) {
                        yield new BooleanConstraint(BooleanConstraint.BooleanValue.get(smaller));
                    }
                    yield BooleanConstraint.any();
                }
                case LESS_THAN_OR_EQUAL -> {
                    Boolean equal = isEqual(leftRange, rightRange);
                    if (equal != null) {
                        yield new BooleanConstraint(BooleanConstraint.BooleanValue.get(equal));
                    }
                    Boolean smaller = isSmaller(leftRange, rightRange);
                    if (smaller != null) {
                        yield new BooleanConstraint(BooleanConstraint.BooleanValue.get(smaller));
                    }
                    yield BooleanConstraint.any();
                }
                case EQUAL_TO -> {
                    Boolean value = isEqual(leftRange, rightRange);
                    if (value != null) {
                        yield new BooleanConstraint(BooleanConstraint.BooleanValue.get(value));
                    }
                    yield BooleanConstraint.any();
                }
                case NOT_EQUAL_TO -> {
                    Boolean value = isEqual(leftRange, rightRange);
                    if (value != null) {
                        yield new BooleanConstraint(BooleanConstraint.BooleanValue.get(!value));
                    }
                    yield BooleanConstraint.any();
                }
                case GREATER_THAN -> {
                    Boolean smaller = isSmaller(leftRange, rightRange);
                    if (smaller != null) {
                        yield new BooleanConstraint(BooleanConstraint.BooleanValue.get(!smaller));
                    }
                    yield BooleanConstraint.any();
                }
                case GREATER_THAN_OR_EQUAL -> {
                    Boolean equal = isEqual(leftRange, rightRange);
                    if (equal != null) {
                        yield new BooleanConstraint(BooleanConstraint.BooleanValue.get(equal));
                    }
                    Boolean smaller = isSmaller(leftRange, rightRange);
                    if (smaller != null) {
                        yield new BooleanConstraint(BooleanConstraint.BooleanValue.get(!smaller));
                    }
                    yield BooleanConstraint.any();
                }
                default -> throw new IllegalStateException("Unexpected value: " + operator);
            };
        }

        private @Nullable Boolean isSmaller(@NotNull NumericConstraint.Range left, @NotNull NumericConstraint.Range right) {
            if (left.upper().value() < right.lower().value()) {
                return true;
            }
            if (left.upper().value() == right.lower().value()) {
                if (left.upper().isInclusive() && right.lower().isInclusive()) {
                    return null;
                }
                return left.upper().isInclusive() || right.lower().isInclusive();
            }
            if (left.lower().value() > right.upper().value()) {
                return false;
            }
            if (left.lower().value() == right.upper().value()) {
                if (left.lower().isInclusive() && right.upper().isInclusive()) {
                    return null;
                }
                return left.upper().isInclusive() || right.lower().isInclusive();
            }
            return null;
        }

        private @Nullable Boolean isEqual(@NotNull NumericConstraint.Range left, @NotNull NumericConstraint.Range right) {
            Double leftValue = null, rightValue = null;
            if (left.lower().value() == left.upper().value() && left.lower().isInclusive() && left.upper().isInclusive()) {
                leftValue = left.lower().value();
            }
            if (right.lower().value() == right.upper().value() && right.lower().isInclusive() && right.upper().isInclusive()) {
                rightValue = right.lower().value();
            }
            if (leftValue != null && rightValue != null) {
                return leftValue.equals(rightValue);
            } else {
                return null;
            }
        }

        @Override
        public void visitUnaryExpression(@NotNull UnaryExpression expression) {
            Constraint value = getConstraint(expression.value());
            switch (expression.operator()) {
                case NOT -> {
                    if (!(value instanceof BooleanConstraint booleanConstraint)) {
                        throw new AssertionError();
                    }
                    constraint = booleanConstraint.negate();
                }
                case NEGATE -> {
                    if (!(value instanceof NumericConstraint numericConstraint)) {
                        throw new AssertionError();
                    }
                    NumericConstraint copy = new NumericConstraint(Optionality.PRESENT);
                    for (NumericConstraint.Range range : numericConstraint.getRanges()) {
                        NumericConstraint.Bound lower = new NumericConstraint.Bound(range.upper().isInclusive(), -range.upper().value());
                        NumericConstraint.Bound upper = new NumericConstraint.Bound(range.lower().isInclusive(), -range.lower().value());
                        copy = copy.or(new NumericConstraint(Optionality.PRESENT, lower, upper));
                    }
                    constraint = copy;
                }
            }
        }
    }
}

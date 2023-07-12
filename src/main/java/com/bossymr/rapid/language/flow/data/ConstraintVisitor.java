package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.constraint.BooleanConstraint;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.constraint.NumericConstraint;
import com.bossymr.rapid.language.flow.constraint.Optionality;
import com.bossymr.rapid.language.flow.value.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiFunction;

public class ConstraintVisitor extends ControlFlowVisitor {

    private final @NotNull DataFlowState state;
    private Constraint constraint;

    public ConstraintVisitor(@NotNull DataFlowState state) {
        this.state = state;
    }

    public @NotNull Constraint getResult() {
        Objects.requireNonNull(constraint);
        return constraint;
    }

    @Override
    public void visitVariableExpression(@NotNull VariableExpression expression) {
        constraint = state.getConstraint(expression.value());
    }

    @Override
    public void visitAggregateExpression(@NotNull AggregateExpression expression) {
        throw new IllegalStateException("Cannot visit condition with aggregate expression");
    }

    @Override
    public void visitBinaryExpression(@NotNull BinaryExpression expression) {
        Constraint left = state.getConstraint(expression.left());
        Constraint right = state.getConstraint(expression.right());
        BinaryOperator operator = expression.operator();
        constraint = switch (operator) {
            case ADD, SUBTRACT, MULTIPLY, DIVIDE, INTEGER_DIVIDE, MODULO -> {
                if (!(left instanceof NumericConstraint leftRange) || !(right instanceof NumericConstraint rightRange)) {
                    throw new IllegalStateException("Invalid expression: " + expression);
                }
                yield getNumericBinaryConstraint(operator, leftRange, rightRange);
            }
            case LESS_THAN, EQUAL_TO, GREATER_THAN, LESS_THAN_OR_EQUAL, NOT_EQUAL_TO, GREATER_THAN_OR_EQUAL -> {
                if (!(left instanceof NumericConstraint leftRange) || !(right instanceof NumericConstraint rightRange)) {
                    throw new IllegalStateException("Invalid expression: " + expression);
                }
                yield getBooleanBinaryConstraint(operator, leftRange, rightRange);
            }
            case AND, XOR, OR -> {
                if (!(left instanceof BooleanConstraint leftConstant) || !(right instanceof BooleanConstraint rightConstant)) {
                    throw new IllegalStateException("Invalid expression: " + expression);
                }
                yield getLogicalBinaryConstraint(operator, leftConstant, rightConstant);
            }
        };
    }

    private @NotNull BooleanConstraint getLogicalBinaryConstraint(@NotNull BinaryOperator operator, @NotNull BooleanConstraint left, @NotNull BooleanConstraint right) {
        if (left.equals(BooleanConstraint.noValue()) || right.equals(BooleanConstraint.noValue())) {
            return BooleanConstraint.noValue();
        }
        Boolean leftValue = left.getBooleanValue().orElse(null);
        Boolean rightValue = right.getBooleanValue().orElse(null);
        boolean bothTrue = Boolean.TRUE.equals(leftValue) && Boolean.TRUE.equals(rightValue);
        boolean bothFalse = Boolean.FALSE.equals(leftValue) && Boolean.FALSE.equals(rightValue);
        boolean anyTrue = Boolean.TRUE.equals(leftValue) || Boolean.TRUE.equals(rightValue);
        boolean anyFalse = Boolean.FALSE.equals(leftValue) || Boolean.FALSE.equals(rightValue);
        return switch (operator) {
            case AND -> {
                if (anyFalse) {
                    yield BooleanConstraint.withValue(false);
                }
                if (bothTrue) {
                    yield BooleanConstraint.withValue(true);
                }
                yield BooleanConstraint.any();
            }
            case XOR -> {
                if (bothTrue || bothFalse) {
                    yield BooleanConstraint.withValue(false);
                }
                if (anyTrue) {
                    yield BooleanConstraint.withValue(true);
                }
                yield BooleanConstraint.any();
            }
            case OR -> {
                if (bothFalse) {
                    yield BooleanConstraint.withValue(false);
                }
                if (anyTrue) {
                    yield BooleanConstraint.withValue(true);
                }
                yield BooleanConstraint.any();
            }
            default -> BooleanConstraint.any();
        };
    }

    private @NotNull NumericConstraint getNumericBinaryConstraint(@NotNull BinaryOperator operator, @NotNull NumericConstraint left, @NotNull NumericConstraint right) {
        return getConstraintForFunction(left, right, getFunctionForOperator(operator));
    }

    private BiFunction<Double, Double, Double> getFunctionForOperator(@NotNull BinaryOperator operator) {
        return switch (operator) {
            case ADD -> Double::sum;
            case SUBTRACT -> (a, b) -> a - b;
            case MULTIPLY -> (a, b) -> a * b;
            case DIVIDE -> (a, b) -> a / b;
            case INTEGER_DIVIDE -> (a, b) -> (double) (a.intValue() / b.intValue());
            case MODULO -> (a, b) -> a % b;
            default -> throw new IllegalStateException("Unexpected value: " + operator);
        };
    }

    private @NotNull NumericConstraint getConstraintForFunction(@NotNull NumericConstraint left, @NotNull NumericConstraint right, @NotNull BiFunction<Double, Double, Double> computation) {
        NumericConstraint constraint = new NumericConstraint(Optionality.PRESENT);
        for (NumericConstraint.Range leftRange : left.getRanges()) {
            for (NumericConstraint.Range rightRange : right.getRanges()) {
                boolean lowerInclusive = leftRange.lower().isInclusive() && rightRange.lower().isInclusive();
                Double lowerValue = computation.apply(leftRange.lower().value(), rightRange.lower().value());
                boolean upperInclusive = leftRange.upper().isInclusive() && rightRange.upper().isInclusive();
                Double upperValue = computation.apply(leftRange.upper().value(), rightRange.upper().value());
                constraint = constraint.or(new NumericConstraint(Optionality.PRESENT, new NumericConstraint.Bound(lowerInclusive, lowerValue), new NumericConstraint.Bound(upperInclusive, upperValue)));
            }
        }
        return constraint;
    }

    private @NotNull BooleanConstraint getBooleanBinaryConstraint(@NotNull BinaryOperator operator, @NotNull NumericConstraint left, @NotNull NumericConstraint right) {
        NumericConstraint.Range leftRange = new NumericConstraint.Range(left.getMinimum(), left.getMaximum());
        NumericConstraint.Range rightRange = new NumericConstraint.Range(right.getMinimum(), right.getMaximum());
        return switch (operator) {
            case LESS_THAN -> leftRange.isSmaller(rightRange)
                    .map(BooleanConstraint::withValue)
                    .orElseGet(BooleanConstraint::any);
            case LESS_THAN_OR_EQUAL -> leftRange.isEqual(leftRange)
                    .or(() -> leftRange.isSmaller(rightRange))
                    .map(BooleanConstraint::withValue)
                    .orElseGet(BooleanConstraint::any);
            case EQUAL_TO -> leftRange.isEqual(rightRange)
                    .map(BooleanConstraint::withValue)
                    .orElseGet(BooleanConstraint::any);
            case NOT_EQUAL_TO -> leftRange.isEqual(rightRange)
                    .map(value -> !(value))
                    .map(BooleanConstraint::withValue)
                    .orElseGet(BooleanConstraint::any);
            case GREATER_THAN -> rightRange.isSmaller(leftRange)
                    .or(() -> rightRange.isEqual(leftRange))
                    .map(BooleanConstraint::withValue)
                    .orElseGet(BooleanConstraint::any);
            case GREATER_THAN_OR_EQUAL -> rightRange.isSmaller(leftRange)
                    .map(BooleanConstraint::withValue)
                    .orElseGet(BooleanConstraint::any);
            default -> throw new IllegalStateException("Unexpected value: " + operator);
        };
    }

    @Override
    public void visitUnaryExpression(@NotNull UnaryExpression expression) {
        Constraint value = state.getConstraint(expression.value());
        switch (expression.operator()) {
            case NOT -> {
                if (!(value instanceof BooleanConstraint booleanConstraint)) {
                    throw new IllegalStateException("Invalid expression: " + expression);
                }
                constraint = booleanConstraint.negate();
            }
            case NEGATE -> {
                if (!(value instanceof NumericConstraint numericConstraint)) {
                    throw new IllegalStateException("Invalid expression: " + expression);
                }
                constraint = Constraint.or(numericConstraint.getRanges().stream()
                        .map(range -> {
                            NumericConstraint.Bound lowerBound = new NumericConstraint.Bound(range.upper().isInclusive(), -range.upper().value());
                            NumericConstraint.Bound upperBound = new NumericConstraint.Bound(range.lower().isInclusive(), -range.lower().value());
                            return new NumericConstraint(Optionality.PRESENT, lowerBound, upperBound);
                        }).toList());
            }
        }
    }
}

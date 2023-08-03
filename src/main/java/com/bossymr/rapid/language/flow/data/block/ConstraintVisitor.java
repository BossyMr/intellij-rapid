package com.bossymr.rapid.language.flow.data.block;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.constraint.BooleanConstraint;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.constraint.NumericConstraint;
import com.bossymr.rapid.language.flow.constraint.Optionality;
import com.bossymr.rapid.language.flow.value.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

public class ConstraintVisitor extends ControlFlowVisitor {

    private final @NotNull DataFlowState state;
    private final @NotNull Set<ReferenceValue> visited;
    private @Nullable Constraint constraint;

    public ConstraintVisitor(@NotNull DataFlowState state, @NotNull Set<ReferenceValue> visited) {
        this.state = state;
        this.visited = visited;
    }

    public @NotNull Optional<Constraint> getResult() {
        return Optional.ofNullable(constraint);
    }

    @Override
    public void visitValueExpression(@NotNull ValueExpression expression) {
        constraint = state.getConstraint(expression.value(), visited).orElse(null);
    }

    @Override
    public void visitAggregateExpression(@NotNull AggregateExpression expression) {
        throw new UnsupportedOperationException("Cannot visit: " + expression);
    }

    @Override
    public void visitBinaryExpression(@NotNull BinaryExpression expression) {
        Constraint left = state.getConstraint(expression.left(), visited).orElse(null);
        Constraint right = state.getConstraint(expression.right(), visited).orElse(null);
        if (left == null || right == null) {
            return;
        }
        BinaryOperator operator = expression.operator();
        constraint = switch (operator) {
            case ADD, SUBTRACT, MULTIPLY, DIVIDE, INTEGER_DIVIDE, MODULO -> {
                if (!(left instanceof NumericConstraint leftRange) || !(right instanceof NumericConstraint rightRange)) {
                    throw new IllegalStateException("Invalid expression: " + expression);
                }
                yield getNumericBinaryConstraint(operator, leftRange, rightRange);
            }
            case LESS_THAN, GREATER_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN_OR_EQUAL -> {
                if (!(left instanceof NumericConstraint leftRange) || !(right instanceof NumericConstraint rightRange)) {
                    throw new IllegalStateException("Invalid expression: " + expression);
                }
                yield getMaxBooleanBinaryConstraint(operator, leftRange, rightRange);
            }
            case EQUAL_TO -> isEqual(expression);
            case NOT_EQUAL_TO -> isEqual(expression).negate();
            case AND, XOR, OR -> {
                if (!(left instanceof BooleanConstraint leftConstant) || !(right instanceof BooleanConstraint rightConstant)) {
                    throw new IllegalStateException("Invalid expression: " + expression);
                }
                yield getLogicalBinaryConstraint(operator, leftConstant, rightConstant);
            }
        };
    }

    private @NotNull BooleanConstraint isEqual(@NotNull BinaryExpression binaryExpression) {
        if (binaryExpression.left().equals(binaryExpression.right())) {
            return BooleanConstraint.alwaysTrue();
        }
        Constraint left = state.getConstraint(binaryExpression.left());
        Constraint right = state.getConstraint(binaryExpression.right());
        if (left.getValue().isPresent() && right.getValue().isPresent()) {
            return BooleanConstraint.equalTo(left.getValue().equals(right.getValue()));
        }
        if (left.intersects(right)) {
            return BooleanConstraint.any();
        }
        return BooleanConstraint.alwaysFalse();
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
                    yield BooleanConstraint.equalTo(false);
                }
                if (bothTrue) {
                    yield BooleanConstraint.equalTo(true);
                }
                yield BooleanConstraint.any();
            }
            case XOR -> {
                if (bothTrue || bothFalse) {
                    yield BooleanConstraint.equalTo(false);
                }
                if (anyTrue) {
                    yield BooleanConstraint.equalTo(true);
                }
                yield BooleanConstraint.any();
            }
            case OR -> {
                if (bothFalse) {
                    yield BooleanConstraint.equalTo(false);
                }
                if (anyTrue) {
                    yield BooleanConstraint.equalTo(true);
                }
                yield BooleanConstraint.any();
            }
            default -> BooleanConstraint.any();
        };
    }

    private @NotNull Constraint getNumericBinaryConstraint(@NotNull BinaryOperator operator, @NotNull NumericConstraint left, @NotNull NumericConstraint right) {
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

    private @NotNull Constraint getConstraintForFunction(@NotNull NumericConstraint left, @NotNull NumericConstraint right, @NotNull BiFunction<Double, Double, Double> operation) {
        Constraint constraint = new NumericConstraint(Optionality.ANY_VALUE);
        for (NumericConstraint.Range leftRange : left.getRanges()) {
            for (NumericConstraint.Range rightRange : right.getRanges()) {
                NumericConstraint.Bound a = compute(leftRange.lower(), rightRange.lower(), operation);
                NumericConstraint.Bound b = compute(leftRange.lower(), rightRange.upper(), operation);
                NumericConstraint.Bound c = compute(leftRange.upper(), rightRange.lower(), operation);
                NumericConstraint.Bound d = compute(leftRange.upper(), rightRange.upper(), operation);
                NumericConstraint.Bound max = NumericConstraint.Bound.max(NumericConstraint.Bound.max(a, b), NumericConstraint.Bound.max(c, d));
                NumericConstraint.Bound min = NumericConstraint.Bound.min(NumericConstraint.Bound.min(a, b), NumericConstraint.Bound.min(c, d));
                constraint = constraint.or(new NumericConstraint(Optionality.ANY_VALUE, min, max));
            }
        }
        return constraint;
    }

    private @NotNull NumericConstraint.Bound compute(@NotNull NumericConstraint.Bound a, @NotNull NumericConstraint.Bound b, @NotNull BiFunction<Double, Double, Double> operation) {
        boolean isInclusive = a.isInclusive() && b.isInclusive();
        Double value = operation.apply(a.value(), b.value());
        return new NumericConstraint.Bound(isInclusive, value);
    }

    private @NotNull BooleanConstraint getMaxBooleanBinaryConstraint(@NotNull BinaryOperator operator, @NotNull NumericConstraint left, @NotNull NumericConstraint right) {
        NumericConstraint.Range leftRange = left.getRange().orElse(NumericConstraint.Range.MAXIMUM_RANGE);
        NumericConstraint.Range rightRange = right.getRange().orElse(NumericConstraint.Range.MAXIMUM_RANGE);
        return switch (operator) {
            case LESS_THAN -> leftRange.isSmaller(rightRange)
                    .map(BooleanConstraint::equalTo)
                    .orElseGet(BooleanConstraint::any);
            case LESS_THAN_OR_EQUAL -> leftRange.isEqual(leftRange)
                    .or(() -> leftRange.isSmaller(rightRange))
                    .map(BooleanConstraint::equalTo)
                    .orElseGet(BooleanConstraint::any);
            case GREATER_THAN -> rightRange.isSmaller(leftRange)
                    .or(() -> rightRange.isEqual(leftRange))
                    .map(BooleanConstraint::equalTo)
                    .orElseGet(BooleanConstraint::any);
            case GREATER_THAN_OR_EQUAL -> rightRange.isSmaller(leftRange)
                    .map(BooleanConstraint::equalTo)
                    .orElseGet(BooleanConstraint::any);
            default -> throw new IllegalStateException("Unexpected value: " + operator);
        };
    }

    @Override
    public void visitUnaryExpression(@NotNull UnaryExpression expression) {
        Constraint value = state.getConstraint(expression.value(), visited).orElse(null);
        if (value == null) {
            return;
        }
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
                            return new NumericConstraint(Optionality.ANY_VALUE, lowerBound, upperBound);
                        }).toList());
            }
        }
    }
}

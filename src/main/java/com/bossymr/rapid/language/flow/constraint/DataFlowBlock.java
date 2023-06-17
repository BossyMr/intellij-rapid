package com.bossymr.rapid.language.flow.constraint;

import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.conditon.Expression;
import com.bossymr.rapid.language.flow.conditon.Operator;
import com.bossymr.rapid.language.flow.conditon.Value;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * A {@code DataFlowBlock} represents the possible values of all variables which are in scope after the specified
 * instructions, but before the terminator of the specified block.
 *
 * @param block the block.
 * @param basicBlock the basic block.
 * @param predecessors the predecessors to this block.
 * @param successors the successors to this block.
 * @param expressions the expressions of this block, which can be expanded into constraints.
 * @param constraints the constraints of this block.
 */
public record DataFlowBlock(@NotNull Block block, @NotNull BasicBlock basicBlock, @NotNull Set<DataFlowBlock> predecessors, @NotNull Set<DataFlowBlock> successors, @NotNull Map<Value.Variable, Expression> expressions, @NotNull Map<Value.Variable, Constraint> constraints) {

    public DataFlowBlock(@NotNull Block block, @NotNull BasicBlock basicBlock) {
        this(block, basicBlock, new HashSet<>(), new HashSet<>(), new HashMap<>(), new HashMap<>());
    }

    public @NotNull Constraint getConstraint(@NotNull RapidType type, @NotNull Expression expression) {
        ExpressionVisitor visitor = new ExpressionVisitor(type);
        expression.accept(visitor);
        return visitor.getResult();
    }

    /**
     * Returns the constraint of the specified value.
     *
     * @param value the value.
     * @return the constraint of the specified value.
     */
    public @NotNull Constraint getConstraint(@NotNull Value value) {
        if (value instanceof Value.Variable variable) {
            return getConstraint(variable);
        }
        if (value instanceof Value.Error) {
            // The constraint could be any valid value.
            return new OpenConstraint(RapidType.ANYTYPE);
        }
        if (value instanceof Value.Constant constant) {
            return new ConstantConstraint(constant.type(), Set.of(constant.value()));
        }
        throw new AssertionError();
    }

    /**
     * Returns the constraint of the specified variable.
     *
     * @param field the variable.
     * @return the constraint of the specified variable.
     */
    public @NotNull Constraint getConstraint(@NotNull Value.Variable field) {
        if (constraints.containsKey(field)) {
            return constraints.get(field);
        }
        List<Constraint> values = predecessors().stream()
                .map(block -> getConstraint(field))
                .toList();
        if (values.isEmpty()) {
            return new OpenConstraint(field.type());
        }
        return Constraint.or(values);
    }

    private class ExpressionVisitor extends ControlFlowVisitor {

        private final @NotNull RapidType type;
        private Constraint constraint;

        public ExpressionVisitor(@NotNull RapidType type) {
            this.type = type;
        }

        public Constraint getResult() {
            return constraint;
        }

        @Override
        public void visitVariableExpression(@NotNull Expression.Variable expression) {
            constraint = getConstraint(expression.value());
        }

        @Override
        public void visitAggregateExpression(@NotNull Expression.Aggregate expression) {
            constraint = new OpenConstraint(type);
            RapidStructure targetStructure = type.getTargetStructure();
            if (targetStructure instanceof RapidRecord record) {
                Map<String, Constraint> components = new HashMap<>();
                List<RapidComponent> sections = record.getComponents();
                int size = expression.values().size();
                for (int i = 0; i < size; i++) {
                    components.put(sections.get(i).getName(), getConstraint(expression.values().get(i)));
                }
                constraint = new RecordConstraint(type, components);
            } else {
                constraint = new AggregateConstraint(type, expression.values().stream().map(DataFlowBlock.this::getConstraint).toList());
            }
        }

        @Override
        public void visitBinaryExpression(@NotNull Expression.Binary expression) {
            Constraint left = getConstraint(expression.left());
            Constraint right = getConstraint(expression.right());
            constraint = calculateConstraint(expression.operator(), left, right);
        }

        private @NotNull Constraint calculateConstraint(@NotNull Operator.BinaryOperator operator, @NotNull Constraint left, @NotNull Constraint right) {
            return switch (operator) {
                case ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO -> {
                    if (!(left instanceof RangeConstraint leftRange) || !(right instanceof RangeConstraint rightRange)) {
                        throw new AssertionError();
                    }
                    yield calculateNumberConstraint(operator, leftRange, rightRange);
                }
                case LESS_THAN, EQUAL_TO, GREATER_THAN -> {
                    if (!(left instanceof RangeConstraint leftRange) || !(right instanceof RangeConstraint rightRange)) {
                        throw new AssertionError();
                    }
                    yield calculateBooleanRangeConstraint(operator, leftRange, rightRange);
                }
                case AND, XOR, OR -> {
                    if (!(left instanceof ConstantConstraint leftConstant) || !(right instanceof ConstantConstraint rightConstant)) {
                        throw new AssertionError();
                    }
                    Boolean leftValue = leftConstant.values().size() == 1 ? (Boolean) leftConstant.values().iterator().next() : null;
                    Boolean rightValue = rightConstant.values().size() == 1 ? (Boolean) rightConstant.values().iterator().next() : null;
                    boolean bothTrue = Boolean.TRUE.equals(leftValue) && Boolean.TRUE.equals(rightValue);
                    boolean bothFalse = Boolean.FALSE.equals(leftValue) && Boolean.FALSE.equals(rightValue);
                    boolean anyTrue = Boolean.TRUE.equals(leftValue) || Boolean.TRUE.equals(rightValue);
                    boolean anyFalse = Boolean.FALSE.equals(leftValue) || Boolean.FALSE.equals(rightValue);
                    if (operator == Operator.BinaryOperator.OR) {
                        if (anyTrue) {
                            yield new ConstantConstraint(RapidType.BOOLEAN, Set.of(true));
                        }
                        if (bothFalse) {
                            yield new ConstantConstraint(RapidType.BOOLEAN, Set.of(false));
                        }
                    }
                    if (operator == Operator.BinaryOperator.AND) {
                        if (bothTrue) {
                            yield new ConstantConstraint(RapidType.BOOLEAN, Set.of(true));
                        }
                        if (anyFalse) {
                            yield new ConstantConstraint(RapidType.BOOLEAN, Set.of(false));
                        }
                    }
                    if (operator == Operator.BinaryOperator.XOR) {
                        if (bothTrue) {
                            yield new ConstantConstraint(RapidType.BOOLEAN, Set.of(false));
                        }
                        if (bothFalse) {
                            yield new ConstantConstraint(RapidType.BOOLEAN, Set.of(false));
                        }
                        if (anyTrue) {
                            yield new ConstantConstraint(RapidType.BOOLEAN, Set.of(false));
                        }
                    }
                    yield new ConstantConstraint(RapidType.BOOLEAN, Set.of(true, false));
                }
            };
        }

        private @NotNull RangeConstraint calculateNumberConstraint(@NotNull Operator.BinaryOperator operator, @NotNull RangeConstraint left, @NotNull RangeConstraint right) {
            return switch (operator) {
                case ADD -> calculateForOperator(left, right, Double::sum);
                case SUBTRACT -> calculateForOperator(left, right, (a, b) -> a - b);
                case MULTIPLY -> calculateForOperator(left, right, (a, b) -> a * b);
                case DIVIDE -> calculateForOperator(left, right, (a, b) -> a / b);
                case MODULO -> calculateForOperator(left, right, (a, b) -> a % b);
                default -> throw new IllegalStateException("Unexpected value: " + operator);
            };
        }

        private @NotNull RangeConstraint calculateForOperator(@NotNull RangeConstraint left, @NotNull RangeConstraint right, @NotNull BiFunction<Double, Double, Double> newValue) {
            RangeConstraint rangeConstraint = new RangeConstraint(left.getType());
            for (RangeConstraint.Range leftRange : left.getRanges()) {
                for (RangeConstraint.Range rightRange : right.getRanges()) {
                    RangeConstraint.Bound lower = new RangeConstraint.Bound(leftRange.lower().inclusive() && rightRange.lower().inclusive(), newValue.apply(leftRange.lower().value(), rightRange.lower().value()));
                    RangeConstraint.Bound upper = new RangeConstraint.Bound(leftRange.upper().inclusive() && rightRange.upper().inclusive(), newValue.apply(leftRange.upper().value(), rightRange.upper().value()));
                    RangeConstraint.Range result = new RangeConstraint.Range(lower, upper);
                    rangeConstraint.add(result);
                }
            }
            return rangeConstraint;
        }

        private @NotNull ConstantConstraint calculateBooleanRangeConstraint(@NotNull Operator.BinaryOperator operator, @NotNull RangeConstraint left, @NotNull RangeConstraint right) {
            RangeConstraint.Bound leftLower = left.getRanges().get(0).lower();
            RangeConstraint.Bound leftUpper = left.getRanges().get(left.getRanges().size() - 1).upper();
            RangeConstraint.Bound rightLower = right.getRanges().get(0).lower();
            RangeConstraint.Bound rightUpper = right.getRanges().get(right.getRanges().size() - 1).upper();
            RangeConstraint.Range leftRange = new RangeConstraint.Range(leftLower, leftUpper);
            RangeConstraint.Range rightRange = new RangeConstraint.Range(rightLower, rightUpper);
            return switch (operator) {
                case LESS_THAN -> {
                    // is the largest value in left smaller than the smallest value in right
                    Boolean smaller = isSmaller(leftRange, rightRange);
                    if (smaller != null) {
                        yield new ConstantConstraint(RapidType.BOOLEAN, Set.of(smaller));
                    }
                    yield new ConstantConstraint(RapidType.BOOLEAN, Set.of(true, false));
                }
                case EQUAL_TO -> {
                    // is the leftmost and rightmost value the same.
                    Double leftValue = null, rightValue = null;
                    if (leftLower.value() == leftUpper.value() && leftLower.inclusive() && leftUpper.inclusive()) {
                        leftValue = leftLower.value();
                    }
                    if (rightLower.value() == rightUpper.value() && rightLower.inclusive() && rightUpper.inclusive()) {
                        rightValue = rightLower.value();
                    }
                    if (leftValue != null && rightValue != null) {
                        yield new ConstantConstraint(RapidType.BOOLEAN, Set.of(leftValue.equals(rightValue)));
                    }
                    yield new ConstantConstraint(RapidType.BOOLEAN, Set.of(true, false));
                }
                case GREATER_THAN -> {
                    Boolean smaller = isSmaller(leftRange, rightRange);
                    if (smaller != null) {
                        yield new ConstantConstraint(RapidType.BOOLEAN, Set.of(!smaller));
                    }
                    yield new ConstantConstraint(RapidType.BOOLEAN, Set.of(true, false));
                }
                default -> throw new IllegalStateException("Unexpected value: " + operator);
            };
        }

        private @Nullable Boolean isSmaller(@NotNull RangeConstraint.Range left, @NotNull RangeConstraint.Range right) {
            if (left.upper().value() < right.lower().value()) {
                return true;
            }
            if (left.upper().value() == right.lower().value()) {
                return left.upper().inclusive() != right.lower().inclusive();
            }
            if (left.lower().value() > right.upper().value()) {
                return false;
            }
            if (left.lower().value() == right.upper().value()) {
                return left.lower().inclusive() != right.upper().inclusive();
            }
            return null;
        }

        @Override
        public void visitUnaryExpression(@NotNull Expression.Unary expression) {
            Constraint value = getConstraint(expression.value());
            switch (expression.operator()) {
                case NOT -> {
                    if (!(value instanceof ConstantConstraint constantConstraint)) {
                        throw new AssertionError();
                    }
                    constraint = new ConstantConstraint(RapidType.BOOLEAN, constantConstraint.values().stream().map(object -> !((boolean) object)).collect(Collectors.toSet()));
                }
                case NEGATE -> {
                    if (!(value instanceof RangeConstraint rangeConstraint)) {
                        throw new AssertionError();
                    }
                    RangeConstraint copy = new RangeConstraint(rangeConstraint.getType());
                    for (RangeConstraint.Range range : rangeConstraint.getRanges()) {
                        RangeConstraint.Bound lower = new RangeConstraint.Bound(range.upper().inclusive(), -range.upper().value());
                        RangeConstraint.Bound upper = new RangeConstraint.Bound(range.lower().inclusive(), -range.lower().value());
                        copy.add(new RangeConstraint.Range(lower, upper));
                    }
                    constraint = copy;
                }
            }
        }
    }
}

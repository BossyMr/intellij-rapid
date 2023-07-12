package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.ArgumentGroup;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.Field;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.constraint.*;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A {@code DataFlowState} represents the state of the program at a specific point.
 * <pre>{@code
 * Block 0:
 * 0: if(value) -> (true: 1, false: 2)
 *
 * Block 1:         // State:
 * 0: x = [0, 1];   // x[0]1 = 0;
 * 1: z = 0;        // x[1]1 = 1
 * 2: goto 3;       // z1 = 0;
 *
 * Block 2:         // State:
 * 0: x = [1, 0];   // x[0]1 = 1;
 * 1: z = 1;        // x[1]1 = 0;
 * 2: goto 3;       // z1 = 1;
 *
 * Block 3:                             // State 1:             State 2:
 * 0: y = (x[0] == 0);                  // y1 = (x[0]1 == 0);   y1 = (x[0]1 == 0);
 * 1: if(y) -> (true: 4, false: 5)      // x[0]1 = 0;           x[0]1 = 1;
 *                                      // x[1]1 = 1;           z[1]1 = 0;
 *                                      // z1 = 0;              z1 = 1;
 *
 * Block 4:     // State:
 *              // y1 = (x1 == 0);
 *              // x1 = 0;
 *              // z1 = 0;
 * }</pre>
 */
public class DataFlowState {

    private final @NotNull Block.FunctionBlock functionBlock;

    /**
     * The conditions that store how variables are related.
     */
    private final @NotNull Set<Condition> conditions;

    /**
     * The latest snapshot of each variable.
     * The latest snapshot for a given variable also represents the variable.
     * The variables which are stored are: {@link VariableValue}, {@link ComponentValue}, and {@link IndexValue}.
     */
    private final @NotNull Map<ReferenceValue, VariableSnapshot> snapshots;

    /**
     * The constraints for all variables.
     */
    private final @NotNull Map<VariableSnapshot, Constraint> constraints;

    public DataFlowState(@NotNull Block.FunctionBlock functionBlock) {
        this.functionBlock = functionBlock;
        this.conditions = new HashSet<>();
        this.snapshots = new HashMap<>();
        this.constraints = new HashMap<>();
    }

    public DataFlowState(@NotNull DataFlowState state) {
        this.functionBlock = state.functionBlock;
        this.conditions = state.conditions;
        this.snapshots = state.snapshots;
        this.constraints = state.constraints;
    }

    public void assign(@NotNull ReferenceValue variable, @NotNull Expression expression) {
        if (variable instanceof FieldValue) {
            return;
        }
        // If you assign a value to an argument, it must be present if the program is to continue successfully.
        if (getOptionality(variable) == Optionality.UNKNOWN) {
            solveMutuallyExlusiveArguments(variable, Optionality.MISSING);
        }
        VariableSnapshot snapshot = getSnapshot(variable);
        Condition condition = new Condition(snapshot, ConditionType.EQUALITY, expression);
        prepareCondition(condition);
        conditions.addAll(condition.getVariants());
    }

    public void assign(@NotNull ReferenceValue variable, @NotNull Constraint constraint) {
        if (variable instanceof FieldValue) {
            return;
        }
        if (getOptionality(variable) == Optionality.UNKNOWN) {
            if (constraint.getOptionality() == Optionality.PRESENT) {
                solveMutuallyExlusiveArguments(variable, Optionality.MISSING);
            }
            if (constraint.getOptionality() == Optionality.MISSING) {
                solveMutuallyExlusiveArguments(variable, Optionality.PRESENT);
            }
        }
        VariableSnapshot snapshot = getSnapshot(variable);
        constraints.put(snapshot, constraint);
    }

    public void add(@NotNull Condition condition) {
        ReferenceValue variable = condition.getVariable();
        if (variable instanceof VariableValue reference) {
            VariableSnapshot snapshot;
            if (!(snapshots.containsKey(reference))) {
                snapshot = new VariableSnapshot(reference.field());
                snapshots.put(reference, snapshot);
            } else {
                snapshot = snapshots.get(reference);
            }
            condition = new Condition(snapshot, condition.getConditionType(), condition.getExpression());
        }
        prepareCondition(condition);
        conditions.add(condition);
    }

    /**
     * Simplifies this {@code DataFlowState} by removing all conditions and constraints which are the specified
     * variables are not dependent upon.
     * Snapshots are also remapped according to the specified map.
     * For example, if an argument {@code x} has a snapshot {@code x1}, and the specified map contains {@code x} to
     * {@code y}, the result will specify that the field {@code y} has a snapshot {@code x1}.
     *
     * @param references the snapshots.
     * @param variables the variables to keep.
     */
    public void simplify(@NotNull Map<ReferenceValue, ReferenceValue> references, @NotNull Set<ReferenceValue> variables) {
        conditions.removeIf(condition -> !(variables.contains(condition.getVariable())) && condition.getVariables().stream().noneMatch(variables::contains));
        constraints.keySet().removeIf(snapshot -> !(variables.contains(snapshots.get(snapshot))));
        Map<ReferenceValue, VariableSnapshot> updated = new HashMap<>();
        for (ReferenceValue referenceValue : references.keySet()) {
            updated.put(references.get(referenceValue), snapshots.get(referenceValue));
        }
        snapshots.clear();
        snapshots.putAll(updated);
    }

    public void merge(@NotNull DataFlowState state) {
        conditions.addAll(state.conditions);
        snapshots.putAll(state.snapshots);
        constraints.putAll(state.constraints);
    }

    private @NotNull Optionality getOptionality(@NotNull ReferenceValue referenceValue) {
        VariableSnapshot snapshot = referenceValue instanceof VariableSnapshot variableSnapshot ? variableSnapshot : snapshots.get(referenceValue);
        if (constraints.containsKey(snapshot)) {
            return constraints.get(snapshot).getOptionality();
        }
        return Optionality.PRESENT;
    }

    private void solveMutuallyExlusiveArguments(@NotNull ReferenceValue referenceValue, @NotNull Optionality optionality) {
        assert optionality != Optionality.UNKNOWN;
        Optional<Argument> optionalArgument = getArgument(referenceValue);
        if (optionalArgument.isEmpty()) {
            return;
        }
        Argument argument = optionalArgument.orElseThrow();
        Optional<ArgumentGroup> groupOptional = functionBlock.getArgumentGroups().stream()
                .filter(argumentGroup -> argumentGroup.arguments().contains(argument))
                .findFirst();
        if (groupOptional.isEmpty()) {
            throw new IllegalArgumentException();
        }
        ArgumentGroup group = groupOptional.orElseThrow();
        assert group.isOptional();
        if (optionality == Optionality.PRESENT) {
            // If an argument group consists of A, B, and C. If A is missing, either B or C might present.
            // However, if an argument group consists of A and B. If A is missing, B must be present.
            long unknownCount = group.arguments().stream()
                    .filter(value -> getOptionality(new VariableValue(value)) == Optionality.UNKNOWN)
                    .count();
            if (unknownCount > 2) {
                return;
            }
        }
        for (Argument sibling : group.arguments()) {
            if (sibling.equals(argument)) {
                continue;
            }
            VariableValue variableValue = new VariableValue(sibling);
            VariableSnapshot snapshot = snapshots.get(variableValue);
            constraints.put(snapshot, constraints.get(snapshot).and(Constraint.any(sibling.type(), optionality)));
        }
    }

    private @NotNull Optional<Argument> getArgument(@NotNull ReferenceValue referenceValue) {
        ReferenceValue rawValue = getRawValue(referenceValue);
        if (!(rawValue instanceof VariableValue variableValue)) {
            return Optional.empty();
        }
        Field field = variableValue.field();
        if (!(field instanceof Argument argument)) {
            return Optional.empty();
        }
        return Optional.of(argument);
    }

    private @NotNull ReferenceValue getRawValue(@NotNull ReferenceValue referenceValue) {
        if (referenceValue instanceof VariableSnapshot snapshot) {
            Optional<ReferenceValue> value = snapshots.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(snapshot))
                    .map(Map.Entry::getKey)
                    .findFirst();
            if (value.isEmpty()) {
                throw new IllegalArgumentException();
            }
            referenceValue = value.orElseThrow();
        }
        if (referenceValue instanceof IndexValue indexValue) {
            return getRawValue(indexValue.variable());
        }
        if (referenceValue instanceof ComponentValue componentValue) {
            return getRawValue(componentValue.variable());
        }
        return referenceValue;
    }


    private @NotNull VariableSnapshot getSnapshot(@NotNull ReferenceValue value) {
        if (value instanceof VariableSnapshot snapshot) {
            return createSnapshot(snapshot);
        }
        if (value instanceof VariableValue) {
            return createSnapshot(value);
        }
        if (value instanceof FieldValue) {
            throw new IllegalArgumentException();
        }
        if (value instanceof IndexValue indexValue) {
            ReferenceValue variable = indexValue.variable();
            Value index = indexValue.index();
            if (index instanceof ReferenceValue referenceValue) {
                if (!(referenceValue instanceof FieldValue)) {
                    index = getSnapshot(referenceValue);
                }
            }
            indexValue = new IndexValue(variable, index);
            return createSnapshot(indexValue);
        }
        if (value instanceof ComponentValue) {
            return createSnapshot(value);
        }
        throw new IllegalArgumentException();
    }

    private @NotNull VariableSnapshot createSnapshot(@NotNull ReferenceValue value) {
        VariableSnapshot snapshot = new VariableSnapshot(value.getType());
        snapshots.put(value, snapshot);
        return snapshot;
    }

    private void prepareCondition(@NotNull Condition condition) {
        condition.iterate(value -> {
            if (!(value instanceof VariableValue variable)) {
                return value;
            }
            if (!(snapshots.containsKey(variable))) {
                throw new IllegalStateException("Condition: " + condition + " references an uninitialized variable: " + variable);
            }
            return snapshots.get(variable);
        });
    }

    public boolean intersects(@NotNull ReferenceValue value, @NotNull Constraint constraint) {
        if (!(snapshots.containsKey(value))) {
            throw new IllegalArgumentException();
        }
        VariableSnapshot snapshot = snapshots.get(value);
        return constraints.get(snapshot).intersects(constraint);
    }

    /**
     * Calculates the constraint of the specified condition.
     *
     * @param condition the condition.
     * @return the constraint.
     */
    public @NotNull Constraint getConstraint(@NotNull Condition condition) {
        Constraint expression = getConstraint(condition.getExpression());
        return calculateConstraint(condition, expression);
    }

    private @NotNull Constraint calculateConstraint(@NotNull Condition condition, @NotNull Constraint expressionConstraint) {
        return switch (condition.getConditionType()) {
            case EQUALITY -> expressionConstraint;
            case INEQUALITY -> {
                if (expressionConstraint instanceof NumericConstraint numericConstraint) {
                    Double point = numericConstraint.getPoint();
                    if (point != null) {
                        yield numericConstraint.negate();
                    }
                    yield NumericConstraint.any();
                }
                if (expressionConstraint instanceof BooleanConstraint booleanConstraint) {
                    yield booleanConstraint.negate();
                }
                if (expressionConstraint instanceof StringConstraint stringConstraint) {
                    if (stringConstraint.sequences().size() == 1) {
                        yield stringConstraint.negate();
                    } else {
                        yield new InverseStringConstraint(Optionality.PRESENT, Set.of());
                    }
                }
                if (expressionConstraint instanceof InverseStringConstraint) {
                    yield new InverseStringConstraint(Optionality.PRESENT, Set.of());
                }
                if (expressionConstraint instanceof OpenConstraint) {
                    yield new OpenConstraint(Optionality.PRESENT);
                }
                throw new AssertionError();
            }
            case LESS_THAN -> {
                if (!(expressionConstraint instanceof NumericConstraint numericConstraint)) {
                    throw new IllegalStateException();
                }
                yield NumericConstraint.lessThan(numericConstraint.getMaximum().value());
            }
            case LESS_THAN_OR_EQUAL -> {
                if (!(expressionConstraint instanceof NumericConstraint numericConstraint)) {
                    throw new IllegalStateException();
                }
                yield NumericConstraint.lessThanOrEqual(numericConstraint.getMaximum().value());
            }
            case GREATER_THAN -> {
                if (!(expressionConstraint instanceof NumericConstraint numericConstraint)) {
                    throw new IllegalStateException();
                }
                yield NumericConstraint.greaterThan(numericConstraint.getMinimum().value());
            }
            case GREATER_THAN_OR_EQUAL -> {
                if (!(expressionConstraint instanceof NumericConstraint numericConstraint)) {
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
        ConstraintVisitor visitor = new ConstraintVisitor(this);
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
            return Constraint.any(value.getType());
        }
        if (value instanceof ConstantValue constant) {
            Object object = constant.value();
            if (value.getType().isAssignable(RapidType.STRING)) {
                return new StringConstraint(Optionality.PRESENT, Set.of(object.toString()));
            }
            if (value.getType().isAssignable(RapidType.NUMBER)) {
                return NumericConstraint.equalTo(((Number) object).doubleValue());
            }
            if (value.getType().isAssignable(RapidType.BOOLEAN)) {
                BooleanConstraint.BooleanValue booleanValue = BooleanConstraint.BooleanValue.withValue((boolean) object);
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
        Set<VariableSnapshot> variables;
        if (variable instanceof VariableSnapshot snapshot) {
            variables = Set.of(snapshots.getOrDefault(variable, snapshot));
        } else if (variable instanceof VariableValue || variable instanceof ComponentValue) {
            variables = Set.of(snapshots.get(variable));
        } else if (variable instanceof FieldValue) {
            return Constraint.any(variable.getType());
        } else if (variable instanceof IndexValue indexValue) {
            Constraint constraint = getConstraint(indexValue.index());
            variables = snapshots.keySet().stream()
                    .filter(value -> value instanceof IndexValue)
                    .map(value -> (IndexValue) value)
                    .filter(value -> {
                        Constraint indexConstraint = getConstraint(value.index());
                        return indexConstraint.intersects(constraint);
                    }).map(snapshots::get)
                    .collect(Collectors.toSet());
        } else {
            throw new IllegalArgumentException();
        }
        Set<Constraint> constraints = this.conditions.stream()
                .filter(condition -> {
                    ReferenceValue referenceValue = condition.getVariable();
                    return referenceValue instanceof VariableSnapshot && variables.contains(referenceValue);
                }).map(this::getConstraint)
                .collect(Collectors.toSet());
        Set<Constraint> precomputed = variables.stream()
                .filter(this.constraints::containsKey)
                .map(this.constraints::get)
                .collect(Collectors.toSet());
        if (constraints.isEmpty()) {
            return Constraint.and(precomputed);
        }
        if (precomputed.isEmpty()) {
            return Constraint.and(constraints);
        }
        return Constraint.and(precomputed).and(Constraint.and(constraints));
    }

}

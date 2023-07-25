package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.constraint.*;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.symbol.RapidType;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private static final Logger logger = Logger.getInstance(DataFlowState.class);

    private final @NotNull Block.FunctionBlock functionBlock;

    /**
     * The conditions which represent the relationship's between variables.
     * For example, a variable might always be equal {@code x + 5}.
     * If {@code x} is checked to be equal to {@code 5}, the variable must be equal to {@code 10}.
     * <p>
     * The variables in all conditions must be replaced with the latest snapshot for the variable.
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

    /**
     * Creates a new {@code DataFlowState} for the specified block. All variables and arguments are also initialized.
     *
     * @param block the block.
     */
    public DataFlowState(@NotNull Block.FunctionBlock block) {
        this.functionBlock = block;
        this.conditions = new HashSet<>();
        this.snapshots = new HashMap<>();
        this.constraints = new HashMap<>();
        initialize(block);
    }

    /**
     * Creates a new copy of the specified state.
     *
     * @param state the state.
     */
    public DataFlowState(@NotNull DataFlowState state) {
        this.functionBlock = state.functionBlock;
        this.conditions = new HashSet<>(state.conditions);
        this.snapshots = new HashMap<>(state.snapshots);
        this.constraints = new HashMap<>(state.constraints);
    }

    private void initialize(@NotNull Block.FunctionBlock block) {
        for (Variable variable : block.getVariables()) {
            initializeVariable(new VariableValue(variable));
        }
        for (ArgumentGroup argumentGroup : block.getArgumentGroups()) {
            Optionality optionality = argumentGroup.isOptional() ? Optionality.UNKNOWN : Optionality.PRESENT;
            for (Argument argument : argumentGroup.arguments()) {
                VariableValue value = new VariableValue(argument);
                if (argument.type().getDimensions() > 0) {
                    initializeArray(value, Constraint.any(argument.type(), optionality));
                } else {
                    assign(value, Constraint.any(argument.type(), optionality));
                }
            }
        }
    }

    private void initializeArray(@NotNull ReferenceValue value, @NotNull Constraint constraint) {
        RapidType arrayType = value.getType();
        VariableSnapshot snapshot = new VariableSnapshot(RapidType.NUMBER);
        add(snapshot, Constraint.any(RapidType.NUMBER));
        for (int i = 0; i < arrayType.getDimensions(); i++) {
            value = new IndexValue(value, snapshot);
        }
        assign(value, constraint);
    }

    private void initializeVariable(@NotNull ReferenceValue value) {
        RapidType type = value.getType();
        if (type.isAssignable(RapidType.NUMBER) || type.isAssignable(RapidType.DOUBLE)) {
            assign(value, Expression.numericConstant(0));
        } else if (type.isAssignable(RapidType.BOOLEAN)) {
            assign(value, Expression.booleanConstant(false));
        } else if (type.isAssignable(RapidType.STRING)) {
            assign(value, Expression.stringConstant(""));
        } else if (type.getDimensions() > 0) {
            initializeArray(value, NumericConstraint.equalTo(0));
        } else if (type.getTargetStructure() instanceof RapidRecord record) {
            for (RapidComponent component : record.getComponents()) {
                RapidType componentType = component.getType();
                Objects.requireNonNull(componentType);
                String componentName = component.getName();
                Objects.requireNonNull(componentName);
                initializeVariable(new ComponentValue(componentType, value, componentName));
            }
        } else {
            assign(value, Constraint.any(value.getType()));
        }
    }

    public void assign(@NotNull ReferenceValue variable, @NotNull Expression expression) {
        if (variable instanceof FieldValue) {
            return;
        }
        logger.debug("Assigning: " + expression + " to: " + variable);
        if (expression instanceof AggregateExpression aggregateExpression) {
            List<Value> values = aggregateExpression.values();
            for (int i = 0; i < values.size(); i++) {
                Value value = values.get(i);
                assign(new IndexValue(variable, new ConstantValue(RapidType.NUMBER, i)), new VariableExpression(value));
            }
            return;
        }
        // If you assign a value to an argument, it must be present if the program is to continue successfully.
        VariableSnapshot snapshot = newSnapshot(variable);
        checkOptionality(snapshot, Optionality.PRESENT);
        Condition condition = new Condition(snapshot, ConditionType.EQUALITY, expression);
        prepareCondition(condition);
        List<Condition> variants = condition.getVariants();
        logger.debug("Adding conditions: " + variants);
        conditions.addAll(variants);
    }

    public void assign(@NotNull ReferenceValue variable, @NotNull Constraint constraint) {
        if (variable instanceof FieldValue) {
            return;
        }
        logger.debug("Assigning: " + constraint + " to: " + variable);
        VariableSnapshot snapshot = newSnapshot(variable);
        checkOptionality(snapshot, constraint.getOptionality());
        constraints.put(snapshot, constraint);
    }

    public void add(@NotNull ReferenceValue variable, @NotNull Constraint constraint) {
        checkOptionality(variable, constraint.getOptionality());
        VariableSnapshot snapshot;
        if (variable instanceof VariableSnapshot) {
            snapshot = (VariableSnapshot) variable;
        } else {
            if (!(snapshots.containsKey(variable))) {
                throw new IllegalStateException();
            }
            snapshot = snapshots.get(variable);
        }
        if (constraints.containsKey(snapshot)) {
            constraint = constraints.get(snapshot).and(constraint);
        }
        constraints.put(snapshot, constraint);
    }

    public void add(@NotNull Condition condition) {
        ReferenceValue variable = condition.getVariable();
        // If the variable is modified, it must be present for execution to continue.
        checkOptionality(variable, Optionality.PRESENT);
        if (variable instanceof VariableValue reference) {
            VariableSnapshot snapshot;
            if (!(snapshots.containsKey(reference))) {
                snapshot = new VariableSnapshot(reference);
                Objects.requireNonNull(snapshot);
                snapshots.put(reference, snapshot);
            } else {
                snapshot = snapshots.get(reference);
            }
            condition = new Condition(snapshot, condition.getConditionType(), condition.getExpression());
        }
        prepareCondition(condition);
        logger.debug("Adding condition: " + condition);
        conditions.add(condition);
        inferSideEffect(condition);
    }

    /**
     * If applicable, updates the optionality for mutually exlusive arguments of the specified variable.
     * For example, if an argument group consists of two arguments,
     * and one of the arguments is now known to be present, the other is known to be missing.
     *
     * @param referenceValue the variable which is changed.
     * @param optionality the new optionality of the specified variable.
     */
    private void checkOptionality(@NotNull ReferenceValue referenceValue, @NotNull Optionality optionality) {
        if (getOptionality(referenceValue) == Optionality.UNKNOWN) {
            if (optionality == Optionality.PRESENT) {
                handleMutuallyExlusiveArguments(referenceValue);
            }
        }
    }

    /**
     * Simplifies this {@code DataFlowState} by removing all conditions and constraints which are the specified
     * variables are not dependent upon.
     * Snapshots are also remapped according to the specified map.
     * For example, if an argument {@code x} has a snapshot {@code x1}, and the specified map contains {@code x} to
     * {@code y}, the result will specify that the field {@code y} has a snapshot {@code x1}.
     * <p>
     * References to variables specified in the {@code references} are remapped, while its latest snapshot is unchanged.
     *
     * @param snapshots the snapshots to update.
     * @param variables the variables to keep.
     * @param references the variables to update.
     */
    public void simplify(@NotNull Map<ReferenceValue, ReferenceValue> snapshots, @NotNull Set<ReferenceValue> variables, @NotNull Map<ReferenceValue, ReferenceValue> references) {
        Set<ReferenceValue> referenceValues = variables.stream()
                .flatMap(variable -> getSnapshots(variable).stream())
                .collect(Collectors.toSet());
        conditions.removeIf(condition -> !(referenceValues.contains(condition.getVariable())) && condition.getVariables().stream().noneMatch(referenceValues::contains));
        constraints.keySet().removeIf(snapshot -> !(referenceValues.contains(snapshot)));
        Map<ReferenceValue, VariableSnapshot> updated = new HashMap<>();
        for (ReferenceValue referenceValue : snapshots.keySet()) {
            VariableSnapshot snapshot = this.snapshots.get(referenceValue);
            updated.put(snapshots.get(referenceValue), snapshot);
        }
        this.snapshots.clear();
        this.snapshots.putAll(updated);
        for (Condition condition : conditions) {
            condition.iterate(variable -> {
                if (references.containsKey(variable)) {
                    return this.snapshots.get(references.get(variable));
                }
                return variable;
            });
        }
    }

    public void merge(@NotNull DataFlowState state) {
        state = combine(state);
        for (ReferenceValue referenceValue : state.snapshots.keySet()) {
            Optionality optionality = state.getConstraint(referenceValue).getOptionality();
            checkOptionality(referenceValue, optionality);
        }
        for (Condition condition : state.conditions) {
            add(condition);
        }
        for (VariableSnapshot value : state.constraints.keySet()) {
            Constraint constraint = state.constraints.get(value);
            Optional<ReferenceValue> variable = state.snapshots.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(value))
                    .map(Map.Entry::getKey)
                    .findFirst();
            if (variable.isPresent()) {
                RapidType type = snapshots.get(variable.orElseThrow()).getType();
                if (constraint instanceof OpenConstraint) {
                    constraint = Constraint.any(type, constraint.getOptionality());
                } else if (constraint instanceof ClosedConstraint) {
                    constraint = Constraint.any(type).negate();
                }
            }
            add(value, constraint);
        }
        snapshots.putAll(state.snapshots);
    }

    /**
     * Modifies the type of snapshots which were passed to a function with a different type.
     * <p>
     * If a function has an {@code anytype} argument, the value passed to the function will retain that type, although
     * it originally had a different type.
     * This method corrects this, by finding the original type of that value in this state, and modifying the mapping to
     * every condition, constraint and snapshot where it occurs.
     *
     * @param state the original state, which might be either correct or incorrect.
     * @return the correct state.
     */
    private @NotNull DataFlowState combine(@NotNull DataFlowState state) {
        Map<ReferenceValue, ReferenceValue> underlying = new HashMap<>();
        Map<VariableSnapshot, VariableSnapshot> remapped = new HashMap<>();
        for (ReferenceValue referenceValue : state.snapshots.keySet()) {
            if (!(snapshots.containsKey(referenceValue))) {
                continue;
            }
            VariableSnapshot current = snapshots.get(referenceValue);
            VariableSnapshot snapshot = state.snapshots.get(referenceValue);
            if (current.getReferenceValue().isEmpty()) {
                continue;
            }
            ReferenceValue reference = current.getReferenceValue().orElseThrow();
            if (snapshot.getReferenceValue().equals(Optional.of(reference))) {
                continue;
            }
            underlying.put(snapshot.getReferenceValue().orElse(null), reference);
            remapped.put(snapshot, new VariableSnapshot(reference));
        }
        DataFlowState copy = new DataFlowState(state);
        for (ReferenceValue referenceValue : state.snapshots.keySet()) {
            VariableSnapshot snapshot = getCorrectSnapshot(remapped, underlying, state.snapshots.get(referenceValue));
            if (snapshot != null) {
                copy.snapshots.put(referenceValue, snapshot);
            }
        }
        for (VariableSnapshot referenceValue : state.constraints.keySet()) {
            VariableSnapshot snapshot = getCorrectSnapshot(remapped, underlying, referenceValue);
            if (snapshot != null) {
                copy.constraints.put(snapshot, copy.constraints.get(referenceValue));
                copy.constraints.remove(referenceValue);
            }
        }
        for (Condition condition : state.conditions) {
            VariableSnapshot correctVariable = getCorrectSnapshot(remapped, underlying, condition.getVariable());
            if (correctVariable != null) {
                copy.conditions.remove(condition);
                condition = new Condition(correctVariable, condition.getConditionType(), condition.getExpression());
                copy.conditions.add(condition);
            }
            condition.iterate(previous -> {
                VariableSnapshot snapshot = getCorrectSnapshot(remapped, underlying, previous);
                return snapshot != null ? snapshot : previous;
            });
        }
        return copy;
    }

    private @Nullable VariableSnapshot getCorrectSnapshot(@NotNull Map<VariableSnapshot, VariableSnapshot> remapped, Map<ReferenceValue, ReferenceValue> underlying, @NotNull ReferenceValue variable) {
        if (!(variable instanceof VariableSnapshot snapshot)) {
            return null;
        }
        if (remapped.containsKey(snapshot)) {
            return remapped.get(snapshot);
        }
        ReferenceValue correctVariable = underlying.get(snapshot.getReferenceValue().orElse(null));
        remapped.put(snapshot, new VariableSnapshot(correctVariable));
        return remapped.get(snapshot);
    }

    private @NotNull Optionality getOptionality(@NotNull ReferenceValue referenceValue) {
        return getConstraint(referenceValue).getOptionality();
    }

    private void handleMutuallyExlusiveArguments(@NotNull ReferenceValue referenceValue) {
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
        for (Argument sibling : group.arguments()) {
            if (sibling.equals(argument)) {
                continue;
            }
            VariableValue variableValue = new VariableValue(sibling);
            VariableSnapshot snapshot = snapshots.get(variableValue);
            constraints.put(snapshot, constraints.get(snapshot).setOptionality(Optionality.MISSING));
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

    private @NotNull VariableSnapshot newSnapshot(@NotNull ReferenceValue value) {
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
                    index = newSnapshot(referenceValue);
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
        VariableSnapshot snapshot = new VariableSnapshot(value);
        logger.debug("Creating snapshot: " + snapshot + " for: " + value);
        Objects.requireNonNull(snapshot);
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
        return getConstraint(value).intersects(constraint);
    }

    /**
     * Retrieves all constraints for a variable with multiple dimensions.
     *
     * @param referenceValue the variable.
     * @return a map, where the key represents the constraint of the index, and the value represents the constraint of
     * the variable.
     */
    public @NotNull Map<Constraint, Constraint> getIndexConstraint(@NotNull ReferenceValue referenceValue) {
        RapidType type = referenceValue.getType();
        int dimensions = type.getDimensions();
        if (dimensions == 0) {
            throw new IllegalArgumentException();
        }
        Map<Constraint, Constraint> constraints = new HashMap<>();
        constraints.put(Constraint.any(RapidType.NUMBER), Constraint.any(type.createArrayType(0)));
        Set<VariableSnapshot> values = getSnapshots(referenceValue);
        snapshots.keySet().stream()
                .filter(variable -> variable instanceof IndexValue)
                .map(variable -> (IndexValue) variable)
                .filter(variable -> values.contains(variable.variable()))
                .forEach(variable -> constraints.put(getConstraint(variable.index()), getConstraint(variable)));
        return constraints;
    }

    private @NotNull Constraint getConstraint(@NotNull Condition condition, @NotNull Set<ReferenceValue> visited) {
        visited.add(condition.getVariable());
        Constraint expression = getConstraint(condition.getExpression(), visited);
        return getConstraint(condition, expression);
    }

    private @NotNull Constraint getConstraint(@NotNull Expression expression, @NotNull Set<ReferenceValue> visited) {
        ConstraintVisitor visitor = new ConstraintVisitor(this, visited);
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
        return getConstraint(value, new HashSet<>());
    }

    @NotNull Constraint getConstraint(@NotNull Value value, @NotNull Set<ReferenceValue> visited) {
        if (value instanceof ReferenceValue variable) {
            return getConstraint(variable, visited);
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
        return getConstraint(variable, new HashSet<>());
    }

    private @NotNull Constraint getConstraint(@NotNull ReferenceValue variable, @NotNull Set<ReferenceValue> visited) {
        Set<VariableSnapshot> variables = getSnapshots(variable);
        Set<Constraint> constraints = conditions.stream()
                .filter(condition -> variables.contains(condition.getVariable()))
                .filter(condition -> condition.getVariables().stream().noneMatch(variables::contains))
                .filter(condition -> condition.getVariables().stream().noneMatch(visited::contains))
                .map(condition -> {
                    Set<ReferenceValue> copy = new HashSet<>(visited);
                    copy.add(condition.getVariable());
                    return getConstraint(condition, copy);
                })
                .collect(Collectors.toSet());
        Set<Constraint> precomputed = variables.stream()
                .filter(this.constraints::containsKey)
                .map(this.constraints::get)
                .collect(Collectors.toSet());
        Constraint conditionConstraint = constraints.isEmpty() ? Constraint.any(variable.getType()) : Constraint.and(constraints);
        Constraint precomputedConstraint = precomputed.isEmpty() ? Constraint.any(variable.getType()) : Constraint.and(precomputed);
        if (conditionConstraint.getOptionality() != precomputedConstraint.getOptionality()) {
            conditionConstraint = conditionConstraint.setOptionality(Optionality.UNKNOWN);
        }
        return conditionConstraint.and(precomputedConstraint);
    }

    private @NotNull Set<VariableSnapshot> getSnapshots(@NotNull ReferenceValue variable) {
        Set<VariableSnapshot> variables = new HashSet<>(1);
        if (variable instanceof VariableSnapshot snapshot) {
            variables.add(snapshots.getOrDefault(variable, snapshot));
        } else if (variable instanceof VariableValue || variable instanceof ComponentValue) {
            if (snapshots.containsKey(variable)) {
                variables.add(snapshots.get(variable));
            }
        } else if (variable instanceof IndexValue indexValue) {
            Constraint constraint = getConstraint(indexValue.index());
            snapshots.keySet().stream()
                    .filter(value -> value instanceof IndexValue)
                    .map(value -> (IndexValue) value)
                    .filter(value -> {
                        Constraint indexConstraint = getConstraint(value.index());
                        return indexConstraint.intersects(constraint);
                    }).map(snapshots::get)
                    .forEach(variables::add);
        } else {
            throw new IllegalArgumentException();
        }
        return variables;
    }

    private @NotNull Constraint getConstraint(@NotNull Condition condition, @NotNull Constraint constraint) {
        return switch (condition.getConditionType()) {
            case EQUALITY -> constraint;
            case INEQUALITY -> {
                if (constraint instanceof NumericConstraint numericConstraint) {
                    Optional<Double> point = numericConstraint.getPoint();
                    if (point.isPresent()) {
                        yield numericConstraint.negate();
                    }
                    yield NumericConstraint.any();
                }
                if (constraint instanceof BooleanConstraint booleanConstraint) {
                    yield booleanConstraint.negate();
                }
                if (constraint instanceof StringConstraint stringConstraint) {
                    if (stringConstraint.sequences().size() == 1) {
                        yield stringConstraint.negate();
                    } else {
                        yield new InverseStringConstraint(Optionality.PRESENT, Set.of());
                    }
                }
                if (constraint instanceof InverseStringConstraint) {
                    yield new InverseStringConstraint(Optionality.PRESENT, Set.of());
                }
                if (constraint instanceof OpenConstraint) {
                    yield new OpenConstraint(Optionality.PRESENT);
                }
                throw new AssertionError();
            }
            case LESS_THAN -> {
                if (!(constraint instanceof NumericConstraint numericConstraint)) {
                    throw new IllegalStateException();
                }
                yield numericConstraint.getMaximum()
                        .map(maximum -> NumericConstraint.lessThan(maximum.value()))
                        .orElse(NumericConstraint.any());
            }
            case LESS_THAN_OR_EQUAL -> {
                if (!(constraint instanceof NumericConstraint numericConstraint)) {
                    throw new IllegalStateException();
                }
                yield numericConstraint.getMaximum()
                        .map(maximum -> NumericConstraint.lessThanOrEqual(maximum.value()))
                        .orElse(NumericConstraint.any());
            }
            case GREATER_THAN -> {
                if (!(constraint instanceof NumericConstraint numericConstraint)) {
                    throw new IllegalStateException();
                }
                yield numericConstraint.getMinimum()
                        .map(minimum -> NumericConstraint.greaterThan(minimum.value()))
                        .orElse(NumericConstraint.any());
            }
            case GREATER_THAN_OR_EQUAL -> {
                if (!(constraint instanceof NumericConstraint numericConstraint)) {
                    throw new IllegalStateException();
                }
                yield numericConstraint.getMinimum()
                        .map(minimum -> NumericConstraint.greaterThanOrEqual(minimum.value()))
                        .orElse(NumericConstraint.any());
            }
        };
    }

    public @NotNull Set<Condition> getConditions(@NotNull ReferenceValue referenceValue) {
        Set<VariableSnapshot> values = getSnapshots(referenceValue);
        return conditions.stream()
                .filter(condition -> values.contains(condition.getVariable()))
                .collect(Collectors.toSet());
    }

    /**
     * Simplifies the side effect of the specified condition.
     *
     * @param expression the expression.
     * @param result the value which the specified expression should evaluate to.
     */
    private void inferSideEffect(@NotNull Expression expression, @NotNull Set<ReferenceValue> visited, boolean result) {
        expression.accept(new ControlFlowVisitor() {
            @Override
            public void visitVariableExpression(@NotNull VariableExpression expression) {
                Value value = expression.value();
                if (!(value.getType().isAssignable(RapidType.BOOLEAN))) {
                    /*
                     * The purpose of this method is to simplify side effects of a conditional jump, where the condition
                     * must be a boolean value.
                     */
                    return;
                }
                if (!(value instanceof ReferenceValue referenceValue)) {
                    /*
                     * The variable is not a variable, which means that the value is an error, in which case no side
                     * effects can be inferred; or the value is a boolean constant.
                     */
                    return;
                }
                if (!(visited.add(referenceValue))) {
                    return;
                }
                Condition condition = new Condition(referenceValue, ConditionType.EQUALITY, new VariableExpression(new ConstantValue(RapidType.BOOLEAN, result)));
                conditions.add(condition);
                Set<Condition> variants = getConditions(referenceValue);
                for (Condition variant : variants) {
                    if (variant.equals(condition)) {
                        continue;
                    }
                    ConditionType conditionType = variant.getConditionType();
                    if (conditionType != ConditionType.EQUALITY && conditionType != ConditionType.INEQUALITY) {
                        throw new IllegalStateException();
                    }
                    inferSideEffect(variant.getExpression(), visited, (conditionType == ConditionType.INEQUALITY) != result);
                }
            }

            @Override
            public void visitBinaryExpression(@NotNull BinaryExpression expression) {
                Value left = expression.left();
                if (!(left instanceof ReferenceValue referenceValue)) {
                    return;
                }
                switch (expression.operator()) {
                    case LESS_THAN ->
                            addCondition(new Condition(referenceValue, ConditionType.LESS_THAN, new VariableExpression(expression.right())), result);
                    case LESS_THAN_OR_EQUAL ->
                            addCondition(new Condition(referenceValue, ConditionType.LESS_THAN_OR_EQUAL, new VariableExpression(expression.right())), result);
                    case EQUAL_TO ->
                            addCondition(new Condition(referenceValue, ConditionType.EQUALITY, new VariableExpression(expression.right())), result);
                    case NOT_EQUAL_TO ->
                            addCondition(new Condition(referenceValue, ConditionType.INEQUALITY, new VariableExpression(expression.right())), result);
                    case GREATER_THAN ->
                            addCondition(new Condition(referenceValue, ConditionType.GREATER_THAN, new VariableExpression(expression.right())), result);
                    case GREATER_THAN_OR_EQUAL ->
                            addCondition(new Condition(referenceValue, ConditionType.GREATER_THAN_OR_EQUAL, new VariableExpression(expression.right())), result);
                    case AND -> {
                        if (result) {
                            conditions.add(new Condition(referenceValue, ConditionType.EQUALITY, new VariableExpression(new ConstantValue(RapidType.BOOLEAN, true))));
                            if (expression.right() instanceof ReferenceValue right) {
                                conditions.add(new Condition(right, ConditionType.EQUALITY, new VariableExpression(new ConstantValue(RapidType.BOOLEAN, true))));
                            }
                        }
                    }
                }
            }

            @Override
            public void visitUnaryExpression(@NotNull UnaryExpression expression) {
                if (expression.operator() == UnaryOperator.NOT) {
                    VariableExpression temporary = new VariableExpression(new ConstantValue(RapidType.BOOLEAN, !result));
                    inferSideEffect(temporary, visited, !(result));
                }
            }
        });
    }

    /**
     * Simplify the side effect of a condition.
     * <p>
     * For example, if the condition {@code x := true} is added, and {@code x := y > 0}, the condition {@code y > 0} can
     * be inferred.
     *
     * @param condition the condition which was added.
     */
    private void inferSideEffect(@NotNull Condition condition) {
        Expression expression = condition.getExpression();
        if (!(expression instanceof VariableExpression variableExpression)) {
            return;
        }
        if (!(variableExpression.value() instanceof ConstantValue constantValue)) {
            return;
        }
        if (!(constantValue.getType().isAssignable(RapidType.BOOLEAN))) {
            return;
        }
        boolean value = (boolean) constantValue.value();
        Expression temporary = new VariableExpression(condition.getVariable());
        inferSideEffect(temporary, new HashSet<>(), value);
    }

    private void addCondition(@NotNull Condition condition, boolean normal) {
        Condition value = normal ? condition : condition.negate();
        conditions.add(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataFlowState state = (DataFlowState) o;
        return Objects.equals(functionBlock, state.functionBlock) && Objects.equals(conditions, state.conditions) && Objects.equals(snapshots, state.snapshots) && Objects.equals(constraints, state.constraints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionBlock, conditions, snapshots, constraints);
    }

    @Override
    public String toString() {
        return "DataFlowState{" +
                "name=" + functionBlock.getModuleName() + ":" + functionBlock.getName() +
                ", conditions=" + conditions.size() +
                ", constraints=" + constraints.size() +
                '}';
    }
}

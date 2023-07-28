package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.constraint.*;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.symbol.*;
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
     * The first snapshots for each argument. If this state is merged with another state, these snapshots are replaced
     * by the value with which the function was invoked.
     */
    private final @NotNull Map<Argument, VariableSnapshot> roots;

    /**
     * Creates a new {@code DataFlowState} for the specified block. All variables and arguments are also initialized.
     *
     * @param block the block.
     */
    private DataFlowState(@NotNull Block.FunctionBlock block) {
        this.functionBlock = block;
        this.conditions = new HashSet<>();
        this.snapshots = new HashMap<>();
        this.constraints = new HashMap<>();
        this.roots = new HashMap<>();
    }

    /**
     * Creates a new copy of the specified state.
     *
     * @param state the state.
     */
    private DataFlowState(@NotNull DataFlowState state) {
        this.functionBlock = state.functionBlock;
        this.conditions = new HashSet<>(state.conditions);
        this.snapshots = new HashMap<>(state.snapshots);
        this.constraints = new HashMap<>(state.constraints);
        this.roots = new HashMap<>(state.roots);
    }

    public static @NotNull DataFlowState createCopy(@NotNull DataFlowState state) {
        return new DataFlowState(state);
    }

    public static @NotNull DataFlowState createFull(@NotNull Block.FunctionBlock block) {
        DataFlowState state = new DataFlowState(block);
        state.initialize(false);
        return state;
    }

    public static @NotNull DataFlowState createLight(@NotNull Block.FunctionBlock block) {
        DataFlowState state = new DataFlowState(block);
        state.initialize(true);
        return state;
    }

    private void initialize(boolean initializeVariables) {
        Optionality variableOptionality = initializeVariables ? Optionality.PRESENT : null;
        for (Variable variable : functionBlock.getVariables()) {
            VariableSnapshot snapshot = new VariableSnapshot(new VariableValue(variable));
            snapshots.put(new VariableValue(variable), snapshot);
            initialize(snapshot, variableOptionality);
        }
        for (ArgumentGroup argumentGroup : functionBlock.getArgumentGroups()) {
            Optionality optionality = argumentGroup.isOptional() ? Optionality.UNKNOWN : Optionality.PRESENT;
            for (Argument argument : argumentGroup.arguments()) {
                VariableSnapshot snapshot = new VariableSnapshot(new VariableValue(argument));
                snapshots.put(new VariableValue(argument), snapshot);
                initialize(snapshot, optionality);
                roots.put(argument, snapshot);
            }
        }
    }

    private void initialize(@NotNull ReferenceValue variable, @Nullable Optionality optionality) {
        while (variable.getType().getDimensions() > 0) {
            VariableSnapshot snapshot = new VariableSnapshot(RapidType.NUMBER);
            add(snapshot, Constraint.any(RapidType.NUMBER));
            variable = new IndexValue(variable, snapshot);
        }
        RapidType type = variable.getType();
        RapidStructure structure = type.getTargetStructure();
        if (structure instanceof RapidRecord record) {
            for (RapidComponent component : record.getComponents()) {
                RapidType componentType = Objects.requireNonNull(component.getType());
                String name = Objects.requireNonNull(component.getName());
                initialize(new ComponentValue(componentType, variable, name), optionality);
            }
        } else if (optionality != null) {
            assign(variable, Constraint.any(type, optionality));
        } else if (type.isAssignable(RapidType.NUMBER) || type.isAssignable(RapidType.DOUBLE)) {
            assign(variable, Expression.numericConstant(0));
        } else if (type.isAssignable(RapidType.STRING)) {
            assign(variable, Expression.stringConstant(""));
        } else if (type.isAssignable(RapidType.BOOLEAN)) {
            assign(variable, Expression.booleanConstant(false));
        } else {
            throw new IllegalArgumentException("Could not initialize: " + variable);
        }
    }

    private @NotNull VariableSnapshot getSnapshot(@NotNull ReferenceValue variable) {
        if (variable instanceof VariableSnapshot snapshot) {
            return snapshot;
        }
        if (!(snapshots.containsKey(variable))) {
            throw new IllegalArgumentException("Could not find snapshot for variable: " + variable);
        }
        VariableSnapshot snapshot = snapshots.get(variable);
        Objects.requireNonNull(snapshot, "Snapshots are corrupted: " + snapshots);
        return snapshot;
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
            snapshot = getSnapshot(variable);
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
                snapshot = getSnapshot(reference);
            }
            condition = new Condition(snapshot, condition.getConditionType(), condition.getExpression());
        }
        prepareCondition(condition);
        logger.debug("Adding condition: " + condition);
        conditions.addAll(condition.getVariants());
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

    public void merge(@NotNull DataFlowState state, @NotNull Map<Argument, ReferenceValue> arguments, @Nullable ReferenceValue returnValue, @Nullable ReferenceValue returnTarget) {
        Set<ReferenceValue> variables = new HashSet<>();
        /*
         * If this map contains the entry x -> y, snapshots referring to x will be replaced by a snapshot referring to y.
         * Alternatively, if y is already a snapshot, the variable will simply be replaced by y.
         */
        Map<ReferenceValue, ReferenceValue> modifications = new HashMap<>();
        /*
         * If a snapshot is replaced, all other occurrences of the snapshot also need to be replaced by the same snapshot.
         */
        Map<VariableSnapshot, VariableSnapshot> remapped = new HashMap<>();
        for (Argument argument : arguments.keySet()) {
            VariableSnapshot snapshot = state.roots.get(argument);
            ReferenceValue value = arguments.get(argument);
            if (argument.parameterType() != ParameterType.INPUT) {
                variables.add(new VariableValue(argument));
            }
            modifications.put(new VariableValue(argument), arguments.get(argument));
            remapped.put(snapshot, getSnapshot(value));
        }
        if (returnValue != null) {
            variables.add(returnValue);
        }
        Set<ReferenceValue> dependentVariables = getDependentVariables(state, variables);
        state.conditions.removeIf(condition -> !(dependentVariables.contains(condition.getVariable())) && condition.getVariables().stream().noneMatch(dependentVariables::contains));
        state.constraints.keySet().removeIf(variable -> !(dependentVariables.contains(variable)));
        for (var entry : arguments.entrySet()) {
            Optional<Argument> argument = getArgument(entry.getValue());
            if (argument.isPresent()) {
                Constraint constraint = state.getConstraint(new VariableValue(entry.getKey()));
                checkOptionality(entry.getValue(), constraint.getOptionality());
            }
        }
        for (Condition condition : state.conditions) {
            condition = new Condition(getModifiedSnapshot(condition.getVariable(), modifications, remapped), condition.getConditionType(), condition.getExpression());
            condition.iterate(variable -> getModifiedSnapshot(variable, modifications, remapped));
            conditions.add(condition);
        }
        for (VariableSnapshot snapshot : state.constraints.keySet()) {
            Constraint constraint = state.constraints.get(snapshot);
            VariableSnapshot modifiedSnapshot = getModifiedSnapshot(snapshot, modifications, remapped);
            if (snapshot.getType().equals(RapidType.ANYTYPE)) {
                if (constraint instanceof OpenConstraint) {
                    constraint = Constraint.any(modifiedSnapshot.getType(), constraint.getOptionality());
                } else if (constraint instanceof ClosedConstraint) {
                    constraint = Constraint.any(modifiedSnapshot.getType(), constraint.getOptionality()).negate();
                }
            }
            constraints.put(modifiedSnapshot, constraint);
        }
        for (Argument argument : arguments.keySet()) {
            if (argument.parameterType() != ParameterType.INPUT) {
                VariableValue value = new VariableValue(argument);
                VariableSnapshot snapshot = remapped.get(state.getSnapshot(value));
                Objects.requireNonNull(snapshot);
                snapshots.put(arguments.get(argument), snapshot);
            }
        }
        if (returnValue != null) {
            if (returnTarget == null) {
                throw new IllegalArgumentException();
            }
            VariableSnapshot snapshot = remapped.get(state.getSnapshot(returnValue));
            Objects.requireNonNull(snapshot);
            snapshots.put(returnTarget, snapshot);
        }
    }

    private @NotNull VariableSnapshot getModifiedSnapshot(@NotNull ReferenceValue previous, @NotNull Map<ReferenceValue, ReferenceValue> modifications, @NotNull Map<VariableSnapshot, VariableSnapshot> remapped) {
        if (!(previous instanceof VariableSnapshot previousSnapshot)) {
            throw new IllegalArgumentException("Unexpected value: " + previous);
        }
        if (remapped.containsKey(previousSnapshot)) {
            return remapped.get(previousSnapshot);
        }
        Optional<ReferenceValue> referenceValue = previousSnapshot.getReferenceValue();
        if (referenceValue.isEmpty()) {
            remapped.put(previousSnapshot, previousSnapshot);
            return previousSnapshot;
        }
        ReferenceValue underlyingValue = referenceValue.orElseThrow();
        if (modifications.containsKey(underlyingValue)) {
            ReferenceValue value = modifications.get(underlyingValue);
            if (value instanceof VariableSnapshot snapshot) {
                remapped.put(previousSnapshot, snapshot);
                return snapshot;
            }
            VariableSnapshot snapshot = new VariableSnapshot(value);
            remapped.put(previousSnapshot, snapshot);
            return snapshot;
        }
        VariableSnapshot snapshot = new VariableSnapshot(previousSnapshot.getType());
        remapped.put(previousSnapshot, snapshot);
        return snapshot;
    }

    private @NotNull Set<ReferenceValue> getDependentVariables(@NotNull DataFlowState state, @NotNull Set<ReferenceValue> variables) {
        Deque<ReferenceValue> workList = new ArrayDeque<>();
        for (ReferenceValue value : variables) {
            Set<VariableSnapshot> snapshots = state.getSnapshots(value);
            workList.addAll(snapshots);
        }
        Set<ReferenceValue> referenceValues = new HashSet<>();
        while (!(workList.isEmpty())) {
            ReferenceValue referenceValue = workList.removeFirst();
            referenceValues.add(referenceValue);
            Set<Condition> conditions = state.getConditions(referenceValue);
            for (Condition condition : conditions) {
                for (ReferenceValue variable : condition.getVariables()) {
                    if (referenceValues.contains(variable)) {
                        continue;
                    }
                    workList.add(variable);
                }
            }
        }
        return referenceValues;
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
            VariableSnapshot snapshot = getSnapshot(variableValue);
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
        if (value instanceof VariableSnapshot snapshot) {
            return snapshot;
        }
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
            return getSnapshot(variable);
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

    public Block.@NotNull FunctionBlock getFunctionBlock() {
        return functionBlock;
    }

    public @NotNull Set<Condition> getConditions() {
        return conditions;
    }

    public @NotNull Map<ReferenceValue, VariableSnapshot> getSnapshots() {
        return snapshots;
    }

    public @NotNull Map<VariableSnapshot, Constraint> getConstraints() {
        return constraints;
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
                variables.add(getSnapshot(variable));
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

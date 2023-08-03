package com.bossymr.rapid.language.flow.data.block;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.constraint.*;
import com.bossymr.rapid.language.flow.data.snapshots.ArrayEntry;
import com.bossymr.rapid.language.flow.data.snapshots.ArraySnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.RecordSnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.symbol.RapidType;
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

    private final @NotNull DataFlowBlock block;

    private final @NotNull Block.FunctionBlock functionBlock;

    /**
     * The conditions for all variables.
     */
    private final @NotNull Set<Condition> conditions;

    /**
     * The latest snapshot of each variable. The latest snapshot for a given variable also represents the variable.
     */
    private final @NotNull Map<VariableValue, ReferenceSnapshot> snapshots;

    /**
     * The constraints for all variables.
     */
    private final @NotNull Map<VariableSnapshot, Constraint> constraints;

    /**
     * The first snapshots for each argument. If this state is merged with another state, these snapshots are replaced
     * by the value with which the function was invoked.
     */
    private final @NotNull Map<Argument, ReferenceSnapshot> roots;

    private DataFlowState(@NotNull DataFlowBlock block) {
        this.block = block;
        Block blockEntity = block.getBasicBlock().getBlock();
        if (!(blockEntity instanceof Block.FunctionBlock functionEntity)) {
            throw new IllegalArgumentException();
        }
        this.functionBlock = functionEntity;
        this.conditions = new HashSet<>();
        this.snapshots = new HashMap<>();
        this.constraints = new HashMap<>();
        this.roots = new HashMap<>();
    }

    private DataFlowState(@NotNull DataFlowBlock block, @NotNull DataFlowState state) {
        this.block = block;
        this.functionBlock = state.functionBlock;
        this.snapshots = new HashMap<>();
        Map<ReferenceSnapshot, ReferenceSnapshot> remapping = new HashMap<>();
        for (var entry : state.snapshots.entrySet()) {
            snapshots.put(entry.getKey(), (ReferenceSnapshot) mapSnapshot(entry.getValue(), remapping));
        }
        this.conditions = new HashSet<>();
        for (Condition condition : state.conditions) {
            conditions.add(Condition.create(condition, value -> mapSnapshot(value, remapping)));
        }
        this.constraints = new HashMap<>();
        for (var entry : state.constraints.entrySet()) {
            constraints.put((VariableSnapshot) mapSnapshot(entry.getKey(), remapping), entry.getValue());
        }
        this.roots = new HashMap<>();
        for (var entry : state.roots.entrySet()) {
            roots.put(entry.getKey(), (ReferenceSnapshot) mapSnapshot(entry.getValue(), remapping));
        }
    }

    /**
     * Create a new copy of the specified state.
     *
     * @param state the state.
     * @return the copy.
     */
    public static @NotNull DataFlowState copy(@NotNull DataFlowBlock block, @NotNull DataFlowState state) {
        return new DataFlowState(block, state);
    }

    /**
     * Create a new state for the specified block. All variables are initialized to their default value, and all
     * arguments are assigned to any value.
     *
     * @param block the block.
     * @return the state.
     */
    public static @NotNull DataFlowState createState(@NotNull DataFlowBlock block) {
        DataFlowState state = new DataFlowState(block);
        state.initialize(AssignmentType.INITIALIZE);
        return state;
    }

    /**
     * Create a new state for the specified block. All variables and arguments are assigned to any value. This method is
     * used for if a block has no predecessor, but is not the entry point of a method, all variables should not be equal
     * to zero.
     *
     * @param block the block.
     * @return the state.
     */
    public static @NotNull DataFlowState createUnknownState(@NotNull DataFlowBlock block) {
        DataFlowState state = new DataFlowState(block);
        state.initialize(AssignmentType.UNKNOWN);
        return state;
    }

    private @NotNull Value mapSnapshot(@NotNull Value previous, @NotNull Map<ReferenceSnapshot, ReferenceSnapshot> remapping) {
        if (!(previous instanceof ReferenceValue referenceValue)) {
            return previous;
        }
        return mapSnapshot(referenceValue, remapping);
    }

    private @NotNull ReferenceValue mapSnapshot(@NotNull ReferenceValue previous, @NotNull Map<ReferenceSnapshot, ReferenceSnapshot> remapping) {
        if (previous instanceof FieldValue || previous instanceof VariableValue) {
            return previous;
        }
        if (previous instanceof IndexValue indexValue) {
            return new IndexValue(mapSnapshot(indexValue.variable(), remapping), mapSnapshot(previous, remapping));
        }
        if (previous instanceof ComponentValue componentValue) {
            return new ComponentValue(componentValue.getType(), mapSnapshot(componentValue.variable(), remapping), componentValue.name());
        }
        if (!(previous instanceof ReferenceSnapshot snapshot)) {
            throw new AssertionError();
        }
        if (remapping.containsKey(snapshot)) {
            return remapping.get(snapshot);
        }
        if (previous instanceof RecordSnapshot recordSnapshot) {
            RecordSnapshot copy = new RecordSnapshot(mapSnapshot(recordSnapshot.getVariable(), remapping));
            for (var entry : recordSnapshot.getSnapshots().entrySet()) {
                copy.getSnapshots().put((ComponentValue) mapSnapshot(entry.getKey(), remapping), (VariableSnapshot) mapSnapshot(entry.getValue(), remapping));
            }
            remapping.put(snapshot, copy);
            return copy;
        }
        if (previous instanceof ArraySnapshot arraySnapshot) {
            ArraySnapshot copy = new ArraySnapshot(mapSnapshot(arraySnapshot.getDefaultValue(), remapping), mapSnapshot(arraySnapshot.getVariable(), remapping));
            for (ArrayEntry.Assignment assignment : arraySnapshot.getAssignments()) {
                copy.getAssignments().add(new ArrayEntry.Assignment(mapSnapshot(assignment.index(), remapping), mapSnapshot(assignment.value(), remapping)));
            }
            remapping.put(snapshot, copy);
            return copy;
        }
        if (previous instanceof VariableSnapshot variableSnapshot) {
            remapping.put(variableSnapshot, variableSnapshot);
            return variableSnapshot;
        }
        throw new AssertionError();
    }

    private void initialize(@NotNull AssignmentType assignmentType) {
        for (Variable variable : functionBlock.getVariables()) {
            ReferenceSnapshot snapshot = initialize(new VariableValue(variable), Optionality.PRESENT, assignmentType);
            snapshots.put(new VariableValue(variable), snapshot);
        }
        for (ArgumentGroup argumentGroup : functionBlock.getArgumentGroups()) {
            Optionality optionality = argumentGroup.isOptional() ? Optionality.UNKNOWN : Optionality.PRESENT;
            for (Argument argument : argumentGroup.arguments()) {
                ReferenceSnapshot snapshot = initialize(new VariableValue(argument), optionality, AssignmentType.UNKNOWN);
                snapshots.put(new VariableValue(argument), snapshot);
                roots.put(argument, snapshot);
            }
        }
    }

    private @NotNull ReferenceSnapshot initialize(@NotNull ReferenceValue variable, @NotNull Optionality optionality, @NotNull AssignmentType assignmentType) {
        RapidType type = variable.getType();
        if (type.getDimensions() > 0) {
            RapidType elementType = type.createArrayType(0);
            VariableSnapshot defaultValue = new VariableSnapshot(elementType);
            add(defaultValue, assignmentType.getConstraint(optionality, elementType));
            return new ArraySnapshot(defaultValue, variable);
        } else if (type.getTargetStructure() instanceof RapidRecord record) {
            RecordSnapshot snapshot = new RecordSnapshot(variable);
            for (RapidComponent component : record.getComponents()) {
                /*
                 * Unlike an array type, the number of components is always known, and is often much smaller - you also
                 * always know which component is being modified. As such, each component always has its own snapshot.
                 */
                RapidType componentType = component.getType();
                String componentName = component.getName();
                if (componentType == null || componentName == null) {
                    continue;
                }
                ComponentValue componentValue = new ComponentValue(componentType, variable, componentName);
                initialize(snapshot.createSnapshot(componentValue), optionality, assignmentType);
            }
            return snapshot;
        } else {
            VariableSnapshot snapshot = new VariableSnapshot(variable);
            add(snapshot, assignmentType.getConstraint(optionality, type));
            return snapshot;
        }
    }

    public void assign(@NotNull Condition condition) {
        if (condition.getVariable() instanceof FieldValue) {
            return;
        }
        if (condition.getVariable() instanceof IndexValue indexValue) {
            if (condition.getExpression() instanceof ValueExpression valueExpression) {
                if (condition.getConditionType() == ConditionType.EQUALITY) {
                    ArraySnapshot snapshot = (ArraySnapshot) getSnapshot(indexValue.variable());
                    snapshot.assign(getSnapshotValue(indexValue.index()), getSnapshotValue(valueExpression.value()));
                    if (valueExpression.value() instanceof ReferenceValue referenceValue) {
                        conditions.add(new Condition(referenceValue, ConditionType.EQUALITY, new ValueExpression(getSnapshot(indexValue))));
                    }
                    return;
                }
            }
        }
        condition = Condition.create(condition, this::createSnapshot, this::getSnapshot);
        insert(condition);
    }

    public void add(@NotNull Condition condition) {
        if (condition.getVariable() instanceof IndexValue indexValue) {
            ArraySnapshot snapshot = (ArraySnapshot) getSnapshot(indexValue.variable());
            List<ArrayEntry> assignments = snapshot.getAssignments(this, indexValue.index());
            if (assignments.size() != 1) {
                throw new IllegalStateException();
            }
            ArrayEntry arrayEntry = assignments.get(0);
            if (arrayEntry instanceof ArrayEntry.Assignment assignment) {
                Value value = assignment.value();
                if (value instanceof ReferenceValue referenceValue) {
                    condition = Condition.create(condition, unused -> referenceValue, this::getSnapshot);
                    insert(condition);
                    inferSideEffect(condition);
                    return;
                }
            }
        }
        condition = Condition.create(condition, this::getSnapshot);
        insert(condition);
        inferSideEffect(condition);
    }

    private @NotNull Value getSnapshotValue(@NotNull Value value) {
        if (value instanceof ReferenceValue referenceValue) {
            return getSnapshot(referenceValue);
        } else {
            return value;
        }
    }

    public void assign(@NotNull ReferenceValue variable, @NotNull Constraint constraint) {
        if (variable instanceof FieldValue) {
            return;
        }
        ReferenceSnapshot snapshot = createSnapshot(variable);
        if (!(snapshot instanceof VariableSnapshot variableSnapshot)) {
            throw new IllegalArgumentException("Could not assign constraint: " + constraint + " to variable: " + variable);
        }
        insert(variableSnapshot, constraint);
    }

    public void add(@NotNull ReferenceValue variable, @NotNull Constraint constraint) {
        VariableSnapshot snapshot = (VariableSnapshot) getSnapshot(variable);
        if (constraints.containsKey(snapshot)) {
            constraint = constraints.get(snapshot).and(constraint);
        }
        insert(snapshot, constraint);
    }

    private void insert(@NotNull VariableSnapshot variable, @NotNull Constraint constraint) {
        checkOptionality(variable, constraint.getOptionality());
        if (constraints.containsKey(variable)) {
            constraint = constraints.get(variable).and(constraint);
        }
        constraints.put(variable, constraint);
    }

    private void insert(@NotNull Condition condition) {
        ReferenceValue variable = condition.getVariable();
        checkOptionality(variable, Optionality.PRESENT);
        RapidType type = variable.getType();
        if (condition.getExpression() instanceof AggregateExpression aggregateExpression) {
            List<Value> values = aggregateExpression.values();
            if (type.getDimensions() > 0) {
                for (int i = 0; i < values.size(); i++) {
                    Value value = values.get(i);
                    assign(new Condition(new IndexValue(variable, ConstantValue.of(i + 1)), ConditionType.EQUALITY, new ValueExpression(value)));
                }
                return;
            } else if (type.getTargetStructure() instanceof RapidRecord record) {
                List<RapidComponent> components = record.getComponents();
                for (int i = 0; i < components.size(); i++) {
                    RapidComponent component = components.get(i);
                    String name = Objects.requireNonNull(component.getName());
                    RapidType componentType = Objects.requireNonNull(component.getType());
                    assign(new Condition(new ComponentValue(componentType, variable, name), ConditionType.EQUALITY, new ValueExpression(values.get(i))));
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
        conditions.addAll(condition.getVariants());
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

    public void merge(@NotNull DataFlowState state, @NotNull Map<Argument, Value> arguments, @Nullable ReferenceValue returnValue, @Nullable ReferenceValue returnTarget) {
        Set<ReferenceValue> variables = new HashSet<>();
        /*
         * If this map contains the entry x -> y, snapshots referring to x will be replaced by a snapshot referring to y.
         * Alternatively, if y is already a snapshot, the variable will simply be replaced by y.
         */
        Map<ReferenceValue, ReferenceValue> modifications = new HashMap<>();
        /*
         * If a snapshot is replaced, all other occurrences of the snapshot also need to be replaced by the same snapshot.
         */
        Map<ReferenceSnapshot, ReferenceSnapshot> remapped = new HashMap<>();
        for (Argument argument : arguments.keySet()) {
            ReferenceSnapshot snapshot = state.roots.get(argument);
            Value value = arguments.get(argument);
            if (!(value instanceof ReferenceValue referenceValue)) {
                continue;
            }
            if (argument.parameterType() != ParameterType.INPUT) {
                variables.add(new VariableValue(argument));
            }
            modifications.put(new VariableValue(argument), referenceValue);
            remapped.put(snapshot, getSnapshot(referenceValue));
        }
        if (returnValue != null) {
            variables.add(returnValue);
        }
        Set<ReferenceValue> dependentVariables = getDependentVariables(state, variables);
        state.conditions.removeIf(condition -> !(dependentVariables.contains(condition.getVariable())) && condition.getVariables().stream().noneMatch(dependentVariables::contains));
        state.constraints.keySet().removeIf(variable -> !(dependentVariables.contains(variable)));
        for (var entry : arguments.entrySet()) {
            if (!(entry.getValue() instanceof ReferenceValue referenceValue)) {
                continue;
            }
            Optional<Argument> argument = getArgument(referenceValue);
            if (argument.isPresent()) {
                Constraint constraint = state.getConstraint(new VariableValue(entry.getKey()));
                checkOptionality(referenceValue, constraint.getOptionality());
            }
        }
        for (Condition condition : state.conditions) {
            condition = new Condition(getModifiedSnapshot(condition.getVariable(), modifications, remapped), condition.getConditionType(), condition.getExpression());
            condition = condition.modify(variable -> getModifiedSnapshot(variable, modifications, remapped));
            conditions.add(condition);
        }
        for (VariableSnapshot snapshot : state.constraints.keySet()) {
            Constraint constraint = state.constraints.get(snapshot);
            VariableSnapshot modifiedSnapshot = (VariableSnapshot) getModifiedSnapshot(snapshot, modifications, remapped);
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
                ReferenceValue snapshot = remapped.get(state.getSnapshot(value));
                Objects.requireNonNull(snapshot);
                if (arguments.get(argument) instanceof ReferenceValue referenceValue) {
                    /*
                     * The argument which was passed to the function now reflects any potential modifications made by the function.
                     */
                    assign(new Condition(referenceValue, ConditionType.EQUALITY, new ValueExpression(snapshot)));
                }
            }
        }
        if (returnValue != null) {
            if (returnTarget == null) {
                throw new IllegalArgumentException();
            }
            ReferenceSnapshot snapshot = remapped.get(state.getSnapshot(returnValue));
            Objects.requireNonNull(snapshot);
            assign(new Condition(returnTarget, ConditionType.EQUALITY, new ValueExpression(snapshot)));
        }
    }

    private @NotNull ReferenceSnapshot getModifiedSnapshot(@NotNull ReferenceValue previous, @NotNull Map<ReferenceValue, ReferenceValue> modifications, @NotNull Map<ReferenceSnapshot, ReferenceSnapshot> remapped) {
        if (!(previous instanceof ReferenceSnapshot previousSnapshot)) {
            throw new IllegalArgumentException("Unexpected value: " + previous);
        }
        if (remapped.containsKey(previousSnapshot)) {
            return remapped.get(previousSnapshot);
        }
        ReferenceValue referenceValue = previousSnapshot.getVariable();
        if (referenceValue == null) {
            remapped.put(previousSnapshot, previousSnapshot);
            return previousSnapshot;
        }
        if (modifications.containsKey(referenceValue)) {
            ReferenceValue value = modifications.get(referenceValue);
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
            ReferenceSnapshot snapshots = state.getSnapshot(value);
            workList.add(snapshots);
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
            VariableSnapshot snapshot = (VariableSnapshot) getSnapshot(variableValue);
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
            Optional<VariableValue> value = snapshots.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(snapshot))
                    .map(Map.Entry::getKey)
                    .findFirst();
            if (value.isEmpty()) {
                throw new IllegalArgumentException();
            }
            referenceValue = value.orElseThrow();
        }
        if (referenceValue instanceof ComponentValue componentValue) {
            return getRawValue(componentValue.variable());
        }
        return referenceValue;
    }

    public boolean intersects(@NotNull Condition condition) {
        Optional<Constraint> optional = getConstraint(condition, new HashSet<>());
        if (optional.isEmpty()) {
            return true;
        }
        return getConstraint(condition.getVariable()).intersects(optional.orElseThrow());
    }

    public @NotNull Block.FunctionBlock getFunctionBlock() {
        return functionBlock;
    }

    public @NotNull Set<Condition> getConditions() {
        return conditions;
    }

    public @NotNull Map<VariableValue, ReferenceSnapshot> getSnapshots() {
        return snapshots;
    }

    public @NotNull Map<VariableSnapshot, Constraint> getConstraints() {
        return constraints;
    }

    public @NotNull Constraint getConstraint(@NotNull Value value) {
        return getConstraint(value, new HashSet<>())
                .orElseGet(() -> Constraint.any(value.getType()));
    }

    public @NotNull Constraint getConstraint(@NotNull Expression expression) {
        Optional<Constraint> optional = getConstraint(expression, new HashSet<>());
        if (optional.isPresent()) {
            return optional.orElseThrow();
        }
        if (expression instanceof ValueExpression valueExpression) {
            return Constraint.any(valueExpression.value().getType());
        }
        if (expression instanceof UnaryExpression unaryExpression) {
            return Constraint.any(unaryExpression.value().getType());
        }
        if (expression instanceof BinaryExpression binaryExpression) {
            return switch (binaryExpression.operator()) {
                case ADD -> Constraint.any(binaryExpression.left().getType());
                case SUBTRACT, MULTIPLY, DIVIDE, INTEGER_DIVIDE, MODULO -> Constraint.any(RapidType.NUMBER);
                case AND, XOR, OR, EQUAL_TO, NOT_EQUAL_TO, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL ->
                        Constraint.any(RapidType.BOOLEAN);
            };
        }
        if (expression instanceof AggregateExpression) {
            throw new IllegalArgumentException();
        }
        throw new AssertionError();
    }

    /**
     * Attempts to calculate the constraint of the specified value.
     *
     * @param value the value.
     * @param visited the variables which have already been visited.
     * @return the constraint of the value, or an empty optional if the constrant could not be calculated.
     */
    @NotNull Optional<Constraint> getConstraint(@NotNull Value value, @NotNull Set<ReferenceValue> visited) {
        if (value instanceof ReferenceValue variable) {
            return getConstraint(variable, visited);
        }
        if (value instanceof ErrorValue) {
            return Optional.of(Constraint.any(value.getType()));
        }
        if (value instanceof ConstantValue constant) {
            Object object = constant.value();
            if (value.getType().isAssignable(RapidType.STRING)) {
                return Optional.of(new StringConstraint(Optionality.PRESENT, Set.of(object.toString())));
            }
            if (value.getType().isAssignable(RapidType.NUMBER)) {
                return Optional.of(NumericConstraint.equalTo(((Number) object).doubleValue()));
            }
            if (value.getType().isAssignable(RapidType.BOOLEAN)) {
                BooleanConstraint.BooleanValue booleanValue = BooleanConstraint.BooleanValue.withValue((boolean) object);
                return Optional.of(new BooleanConstraint(Optionality.PRESENT, booleanValue));
            }
        }
        throw new AssertionError();
    }

    /**
     * Attempts to calculate the constraint of the specified variable.
     *
     * @param variable the variable.
     * @param visited the variables which have already been visited.
     * @return the constraint of the variable, or an empty optional if the constraint could not be calculated.
     */
    private @NotNull Optional<Constraint> getConstraint(@NotNull ReferenceValue variable, @NotNull Set<ReferenceValue> visited) {
        ReferenceSnapshot snapshot = getSnapshot(variable);
        List<Constraint> constraints = conditions.stream()
                .filter(condition -> condition.getVariable().equals(snapshot))
                .filter(condition -> condition.getVariables().stream().noneMatch(snapshot::equals))
                .filter(condition -> condition.getVariables().stream().noneMatch(visited::contains))
                .map(condition -> {
                    Set<ReferenceValue> copy = new HashSet<>(visited);
                    copy.add(condition.getVariable());
                    return getConstraint(condition, copy);
                })
                .filter(Optional::isPresent)
                .map(Optional::orElseThrow)
                .toList();
        if (constraints.isEmpty()) {
            return getPrecomputedConstraint(variable);
        }
        Constraint constraint = Constraint.and(constraints);
        if (constraint.getOptionality() == Optionality.ANY_VALUE) {
            constraint = constraint.setOptionality(Optionality.PRESENT);
        }
        Optional<Constraint> precomputed = getPrecomputedConstraint(variable);
        if (precomputed.isPresent()) {
            if (precomputed.orElseThrow().getOptionality() != constraint.getOptionality()) {
                constraint = constraint.setOptionality(Optionality.UNKNOWN);
            }
            return Optional.of(constraint.and(precomputed.orElseThrow()));
        } else {
            return Optional.of(constraint);
        }
    }

    private @NotNull Optional<Constraint> getPrecomputedConstraint(@NotNull ReferenceValue variable) {
        ReferenceSnapshot snapshot = getSnapshot(variable);
        if (!(snapshot instanceof VariableSnapshot variableSnapshot)) {
            return Optional.empty();
        }
        if (!(constraints.containsKey(variableSnapshot))) {
            return Optional.empty();
        }
        return Optional.of(constraints.get(variableSnapshot));
    }

    private @NotNull Optional<Constraint> getConstraint(@NotNull Condition condition, @NotNull Set<ReferenceValue> visited) {
        visited.add(condition.getVariable());
        Optional<Constraint> expression = getConstraint(condition.getExpression(), visited);
        return expression.map(constraint -> getConstraint(condition, constraint));
    }

    private @NotNull Optional<Constraint> getConstraint(@NotNull Expression expression, @NotNull Set<ReferenceValue> visited) {
        ConstraintVisitor visitor = new ConstraintVisitor(this, visited);
        expression.accept(visitor);
        return visitor.getResult();
    }


    private @NotNull ReferenceSnapshot createSnapshot(@NotNull ReferenceValue referenceValue) {
        if (referenceValue instanceof ReferenceSnapshot snapshot) {
            return snapshot;
        }
        if (referenceValue instanceof FieldValue fieldValue) {
            throw new IllegalArgumentException("Cannot create snapshot for field: " + fieldValue);
        }
        if (referenceValue instanceof IndexValue indexValue) {
            ReferenceSnapshot snapshot = getSnapshot(indexValue.variable());
            if (!(snapshot instanceof ArraySnapshot arraySnapshot)) {
                throw new IllegalArgumentException();
            }
            Value index;
            if (indexValue.index() instanceof ReferenceValue referenceIndex) {
                index = getSnapshot(referenceIndex);
            } else {
                index = indexValue.index();
            }
            return arraySnapshot.createSnapshot(index);
        }
        if (referenceValue instanceof VariableValue variableValue) {
            RapidType type = variableValue.getType();
            ReferenceSnapshot snapshot;
            if (type.getDimensions() > 0) {
                RapidType elementType = type.createArrayType(0);
                VariableSnapshot defaultValue = new VariableSnapshot(elementType);
                add(defaultValue, Constraint.any(elementType));
                snapshot = new ArraySnapshot(defaultValue, variableValue);
            } else if (type.getTargetStructure() instanceof RapidRecord) {
                snapshot = new RecordSnapshot(variableValue);
            } else {
                snapshot = new VariableSnapshot(variableValue);
            }
            snapshots.put(variableValue, snapshot);
            return snapshot;
        }
        if (referenceValue instanceof ComponentValue componentValue) {
            ReferenceSnapshot snapshot = getSnapshot(componentValue.variable());
            if (!(snapshot instanceof RecordSnapshot recordSnapshot)) {
                throw new IllegalArgumentException();
            }
            return recordSnapshot.createSnapshot(componentValue);
        }
        throw new IllegalArgumentException();
    }

    public @NotNull ReferenceSnapshot getSnapshot(@NotNull ReferenceValue variable) {
        if (variable instanceof FieldValue) {
            throw new IllegalArgumentException("Cannot retrieve snapshot of field: " + variable);
        }
        if (variable instanceof ReferenceSnapshot snapshot) {
            /*
             * The variable is already a snapshot, and snapshots should not be nested.
             */
            return snapshot;
        }
        if (variable instanceof ComponentValue componentValue) {
            ReferenceSnapshot snapshot = getSnapshot(componentValue.variable());
            if (!(snapshot instanceof RecordSnapshot recordSnapshot)) {
                throw new IllegalArgumentException("Cannot retrieve snapshot of: " + variable);
            }
            return recordSnapshot.getSnapshot(componentValue);
        }
        if (variable instanceof IndexValue indexValue) {
            ReferenceSnapshot snapshot = getSnapshot(indexValue.variable());
            if (!(snapshot instanceof ArraySnapshot arraySnapshot)) {
                throw new IllegalArgumentException("Cannot retrieve snapshot of: " + variable);
            }
            Value index;
            if (indexValue.index() instanceof ReferenceValue referenceValue) {
                index = getSnapshot(referenceValue);
            } else {
                index = indexValue.index();
            }
            List<ArrayEntry> assignments = arraySnapshot.getAssignments(this, index);
            if (assignments.size() > 1) {
                /*
                 * The DataFlowBlock should split the states so that each state only handles one assignment.
                 */
                throw new IllegalStateException();
            }
            if (assignments.isEmpty()) {
                /*
                 * The variable will always be equal to the default value of the array if no other assignment matches.
                 */
                throw new IllegalStateException();
            }
            ArrayEntry entry = assignments.iterator().next();
            if (entry instanceof ArrayEntry.DefaultValue defaultValue) {
                if (indexValue.getType().getDimensions() > 0) {
                    return new ArraySnapshot(defaultValue.defaultValue(), indexValue);
                } else {
                    VariableSnapshot defaultSnapshot = new VariableSnapshot(indexValue);
                    add(new Condition(defaultSnapshot, ConditionType.EQUALITY, new ValueExpression(defaultValue.defaultValue())));
                    return defaultSnapshot;
                }
            } else if (entry instanceof ArrayEntry.Assignment assignment) {
                if (assignment.value() instanceof ReferenceValue referenceValue) {
                    return getSnapshot(referenceValue);
                }
                VariableSnapshot variableSnapshot = new VariableSnapshot(indexValue);
                add(new Condition(variableSnapshot, ConditionType.EQUALITY, new ValueExpression(assignment.value())));
                return variableSnapshot;
            }
        }
        if (!(variable instanceof VariableValue variableValue)) {
            throw new AssertionError();
        }
        if (!(snapshots.containsKey(variableValue))) {
            return createSnapshot(variableValue);
        }
        ReferenceSnapshot snapshot = snapshots.get(variableValue);
        return Objects.requireNonNull(snapshot);
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
        ReferenceSnapshot values = getSnapshot(referenceValue);
        return conditions.stream()
                .filter(condition -> values.equals(condition.getVariable()))
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
            public void visitValueExpression(@NotNull ValueExpression expression) {
                Value value = expression.value();
                if (!(value.getType().isAssignable(RapidType.BOOLEAN))) {
                    /*
                     * The purpose of this method is to simplify the side effects of a conditional jump, where the condition
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
                Condition condition = new Condition(referenceValue, ConditionType.EQUALITY, new ValueExpression(new ConstantValue(RapidType.BOOLEAN, result)));
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
                            addCondition(new Condition(referenceValue, ConditionType.LESS_THAN, new ValueExpression(expression.right())), result);
                    case LESS_THAN_OR_EQUAL ->
                            addCondition(new Condition(referenceValue, ConditionType.LESS_THAN_OR_EQUAL, new ValueExpression(expression.right())), result);
                    case EQUAL_TO ->
                            addCondition(new Condition(referenceValue, ConditionType.EQUALITY, new ValueExpression(expression.right())), result);
                    case NOT_EQUAL_TO ->
                            addCondition(new Condition(referenceValue, ConditionType.INEQUALITY, new ValueExpression(expression.right())), result);
                    case GREATER_THAN ->
                            addCondition(new Condition(referenceValue, ConditionType.GREATER_THAN, new ValueExpression(expression.right())), result);
                    case GREATER_THAN_OR_EQUAL ->
                            addCondition(new Condition(referenceValue, ConditionType.GREATER_THAN_OR_EQUAL, new ValueExpression(expression.right())), result);
                    case AND -> {
                        if (result) {
                            conditions.add(new Condition(referenceValue, ConditionType.EQUALITY, new ValueExpression(new ConstantValue(RapidType.BOOLEAN, true))));
                            if (expression.right() instanceof ReferenceValue right) {
                                conditions.add(new Condition(right, ConditionType.EQUALITY, new ValueExpression(new ConstantValue(RapidType.BOOLEAN, true))));
                            }
                        }
                    }
                }
            }

            @Override
            public void visitUnaryExpression(@NotNull UnaryExpression expression) {
                if (expression.operator() == UnaryOperator.NOT) {
                    ValueExpression temporary = new ValueExpression(new ConstantValue(RapidType.BOOLEAN, !result));
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
        if (!(expression instanceof ValueExpression valueExpression)) {
            return;
        }
        if (!(valueExpression.value() instanceof ConstantValue constantValue)) {
            return;
        }
        if (!(constantValue.getType().isAssignable(RapidType.BOOLEAN))) {
            return;
        }
        boolean value = (boolean) constantValue.value();
        Expression temporary = new ValueExpression(condition.getVariable());
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

    private enum AssignmentType {
        INITIALIZE {
            @Override
            public @NotNull Constraint getConstraint(@NotNull Optionality optionality, @NotNull RapidType type) {
                Constraint constraint;
                if (type.equals(RapidType.ANYTYPE)) {
                    constraint = new OpenConstraint(optionality);
                } else if (type.isAssignable(RapidType.NUMBER) || type.isAssignable(RapidType.DOUBLE)) {
                    constraint = NumericConstraint.equalTo(0);
                } else if (type.isAssignable(RapidType.STRING)) {
                    constraint = StringConstraint.anyOf("");
                } else if (type.isAssignable(RapidType.BOOLEAN)) {
                    constraint = BooleanConstraint.alwaysFalse();
                } else {
                    constraint = new OpenConstraint(optionality);
                }
                if (optionality != constraint.getOptionality()) {
                    constraint = constraint.setOptionality(optionality);
                }
                return constraint;
            }
        },
        UNKNOWN {
            @Override
            public @NotNull Constraint getConstraint(@NotNull Optionality optionality, @NotNull RapidType type) {
                return Constraint.any(type, optionality);
            }
        };

        public abstract @NotNull Constraint getConstraint(@NotNull Optionality optionality, @NotNull RapidType type);
    }
}

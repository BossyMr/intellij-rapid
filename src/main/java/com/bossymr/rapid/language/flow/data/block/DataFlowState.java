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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

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

    private final @Nullable DataFlowState predecessor;
    private final @Nullable DataFlowBlock block;

    private final @NotNull Block functionBlock;

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

    public DataFlowState(@NotNull DataFlowBlock block, @Nullable DataFlowState predecessor) {
        this(block, block.getBasicBlock().getBlock(), predecessor);
    }

    private DataFlowState(@NotNull Block functionBlock, @Nullable DataFlowState predecessor) {
        this(null, functionBlock, predecessor);
    }

    public DataFlowState(@Nullable DataFlowBlock block, @NotNull Block functionBlock, @Nullable DataFlowState predecessor) {
        this.predecessor = predecessor;
        this.block = block;
        this.functionBlock = functionBlock;
        this.conditions = new HashSet<>();
        this.snapshots = new HashMap<>();
        this.constraints = new HashMap<>();
        this.roots = new HashMap<>();
    }

    private DataFlowState(@NotNull DataFlowState state) {
        this.block = state.block;
        this.predecessor = state.predecessor;
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
    public static @NotNull DataFlowState copy(@NotNull DataFlowState state) {
        return new DataFlowState(state);
    }

    /**
     * Create a new state for the specified block. All variables are initialized to their default value, and all
     * arguments are assigned to any value.
     *
     * @param block the block.
     * @return the state.
     */
    public static @NotNull DataFlowState createState(@NotNull Block.FunctionBlock block) {
        DataFlowState state = new DataFlowState(block, null);
        state.initialize(AssignmentType.INITIALIZE);
        return state;
    }

    public static @NotNull DataFlowState createState(@NotNull DataFlowBlock block) {
        DataFlowState state = new DataFlowState(block, null);
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
    public static @NotNull DataFlowState createUnknownState(@NotNull Block.FunctionBlock block) {
        DataFlowState state = new DataFlowState(block, null);
        state.initialize(AssignmentType.UNKNOWN);
        return state;
    }

    public static @NotNull DataFlowState createUnknownState(@NotNull DataFlowBlock block) {
        DataFlowState state = new DataFlowState(block, null);
        state.initialize(AssignmentType.UNKNOWN);
        return state;
    }

    public static @NotNull DataFlowState createSuccessorState(@NotNull DataFlowBlock block, @NotNull DataFlowState predecessor) {
        return new DataFlowState(block, predecessor);
    }

    public @NotNull Optional<DataFlowState> getPredecessor() {
        return Optional.ofNullable(predecessor);
    }

    private @NotNull Value mapSnapshot(@NotNull Value previous, @NotNull Map<ReferenceSnapshot, ReferenceSnapshot> remapping) {
        if (!(previous instanceof ReferenceValue referenceValue)) {
            return previous;
        }
        return mapSnapshot(referenceValue, remapping);
    }

    public @NotNull Optional<DataFlowBlock> getBlock() {
        return Optional.ofNullable(block);
    }

    private @NotNull ReferenceValue mapSnapshot(@NotNull ReferenceValue previous, @NotNull Map<ReferenceSnapshot, ReferenceSnapshot> remapping) {
        if (previous instanceof FieldValue || previous instanceof VariableValue) {
            return previous;
        }
        if (previous instanceof IndexValue indexValue) {
            if (indexValue.variable() instanceof FieldValue) {
                return indexValue;
            }
            return new IndexValue(mapSnapshot(indexValue.variable(), remapping), mapSnapshot(indexValue.index(), remapping));
        }
        if (previous instanceof ComponentValue componentValue) {
            if (componentValue.variable() instanceof FieldValue) {
                return componentValue;
            }
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
            remapping.put(snapshot, copy);
            for (var entry : recordSnapshot.getSnapshots().entrySet()) {
                copy.assign(entry.getKey(), mapSnapshot(entry.getValue(), remapping));
            }
            return copy;
        }
        if (previous instanceof ArraySnapshot arraySnapshot) {
            ArraySnapshot copy = new ArraySnapshot(mapSnapshot(arraySnapshot.getDefaultValue(), remapping), mapSnapshot(arraySnapshot.getVariable(), remapping));
            remapping.put(snapshot, copy);
            for (ArrayEntry.Assignment assignment : arraySnapshot.getAssignments()) {
                copy.assign(mapSnapshot(assignment.index(), remapping), mapSnapshot(assignment.value(), remapping));
            }
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

    public @NotNull ReferenceSnapshot initialize(@NotNull ReferenceValue variable, @NotNull Optionality optionality, @NotNull AssignmentType assignmentType) {
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
                initialize(snapshot.createSnapshot(componentName), optionality, assignmentType);
            }
            return snapshot;
        } else {
            VariableSnapshot snapshot = new VariableSnapshot(variable);
            add(snapshot, assignmentType.getConstraint(optionality, type));
            return snapshot;
        }
    }

    public void assign(@NotNull Condition condition, boolean addVariants) {
        if (condition.getVariable() instanceof VariableValue variableValue) {
            if (condition.getExpression() instanceof ValueExpression valueExpression) {
                if (valueExpression.value() instanceof ReferenceValue referenceValue) {
                    Optional<ReferenceSnapshot> variable = getSnapshot(referenceValue);
                    if (variable.isPresent()) {
                        snapshots.put(variableValue, variable.orElseThrow());
                        return;
                    }
                }
            }
        }
        if(condition.getVariable() instanceof IndexValue indexValue && condition.getConditionType() == ConditionType.EQUALITY) {
            Optional<ReferenceSnapshot> optional = getSnapshot(indexValue.variable());
            if(optional.isPresent() && optional.orElseThrow() instanceof ArraySnapshot snapshot) {
                Value value;
                Expression expression = condition.getExpression();
                if(expression instanceof ValueExpression valueExpression) {
                    value = getSnapshot(valueExpression.value());
                } else {
                    VariableSnapshot variableSnapshot = new VariableSnapshot(indexValue.getType());
                    add(new Condition(variableSnapshot, condition.getConditionType(), expression), addVariants);
                    value = variableSnapshot;
                }
                snapshot.assign(getSnapshot(indexValue.index()), value);
                Optional<ReferenceSnapshot> indexOptional = getSnapshot(indexValue);
                if(indexOptional.isPresent() && value instanceof ReferenceValue referenceValue) {
                    conditions.add(new Condition(referenceValue, ConditionType.EQUALITY, new ValueExpression(indexOptional.orElseThrow())));
                }
                return;
            }
        }
        Optional<Condition> optional = getCondition(this::createSnapshot, condition);
        if(optional.isEmpty()) {
            return;
        }
        insert(optional.orElseThrow(), addVariants);
    }

    private @NotNull Optional<Condition> getCondition(@NotNull Function<ReferenceValue, Optional<? extends ReferenceValue>> variable, @NotNull Condition condition) {
        Optional<? extends ReferenceValue> optional = variable.apply(condition.getVariable());
        if(optional.isEmpty()) {
            return Optional.empty();
        }
        AtomicBoolean skip = new AtomicBoolean();
        Condition result = Condition.create(condition, referenceValue -> optional.orElseThrow(), referenceValue -> {
            Optional<ReferenceSnapshot> snapshot = getSnapshot(referenceValue);
            if (snapshot.isEmpty()) {
                skip.set(true);
                return referenceValue;
            } else {
                return snapshot.orElseThrow();
            }
        });
        if(skip.get()) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    public void add(@NotNull Condition condition, boolean addVariants) {
        if (condition.getVariable() instanceof IndexValue indexValue) {
            Optional<ReferenceSnapshot> optional = getSnapshot(indexValue.variable());
            if(optional.isPresent() && optional.orElseThrow() instanceof ArraySnapshot snapshot) {
                List<ArrayEntry> assignments = snapshot.getAssignments(this, indexValue.index());
                if (assignments.size() != 1) {
                    throw new IllegalStateException();
                }
                ArrayEntry arrayEntry = assignments.get(0);
                if (arrayEntry instanceof ArrayEntry.Assignment assignment) {
                    Value value = assignment.value();
                    if (value instanceof ReferenceValue referenceValue) {
                        Optional<Condition> result = getCondition(unused -> Optional.of(referenceValue), condition);
                        if(result.isPresent()) {
                            condition = result.orElseThrow();
                            insert(condition, addVariants);
                            inferSideEffect(condition);
                            return;
                        }
                    }
                }
            }
        }
        Optional<Condition> optional = getCondition(this::getSnapshot, condition);
        if(optional.isEmpty()) {
            return;
        }
        condition = optional.orElseThrow();
        insert(condition, addVariants);
        inferSideEffect(condition);
    }

    public void assign(@NotNull ReferenceValue variable, @NotNull Constraint constraint) {
        if (variable instanceof FieldValue) {
            return;
        }
        Optional<ReferenceSnapshot> snapshot = createSnapshot(variable);
        if (snapshot.isEmpty() || !(snapshot.orElseThrow() instanceof VariableSnapshot variableSnapshot)) {
            return;
        }
        checkOptionality(variable, constraint.getOptionality());
        constraints.put(variableSnapshot, constraint);
    }

    public void add(@NotNull ReferenceValue variable, @NotNull Constraint constraint) {
        Optional<ReferenceSnapshot> optional = getSnapshot(variable);
        if(optional.isEmpty() || !(optional.orElseThrow() instanceof VariableSnapshot snapshot)) {
            return;
        }
        checkOptionality(variable, constraint.getOptionality());
        Optional<Constraint> previous = getPrecomputedConstraint(snapshot);
        if (previous.isPresent()) {
            constraint = previous.orElseThrow().and(constraint);
        }
        constraints.put(snapshot, constraint);
    }

    private void insert(@NotNull Condition condition, boolean addVariants) {
        ReferenceValue variable = condition.getVariable();
        checkOptionality(variable, Optionality.PRESENT);
        RapidType type = variable.getType();
        if (condition.getExpression() instanceof AggregateExpression aggregateExpression) {
            List<Value> values = aggregateExpression.values();
            if (type.getDimensions() > 0) {
                for (int i = 0; i < values.size(); i++) {
                    Value value = values.get(i);
                    assign(new Condition(new IndexValue(variable, ConstantValue.of(i + 1)), ConditionType.EQUALITY, new ValueExpression(value)), addVariants);
                }
                return;
            } else if (type.getTargetStructure() instanceof RapidRecord record) {
                List<RapidComponent> components = record.getComponents();
                for (int i = 0; i < components.size(); i++) {
                    RapidComponent component = components.get(i);
                    String name = Objects.requireNonNull(component.getName());
                    RapidType componentType = Objects.requireNonNull(component.getType());
                    assign(new Condition(new ComponentValue(componentType, variable, name), ConditionType.EQUALITY, new ValueExpression(values.get(i))), addVariants);
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (addVariants) {
            conditions.addAll(condition.getVariants());
        } else {
            conditions.add(condition);
        }
    }

    /**
     * If applicable, updates the optionality for mutually exclusive arguments of the specified variable. For example, if
     * an argument group consists of two arguments, and one of the arguments is now known to be present, the other is
     * known to be missing.
     *
     * @param referenceValue the variable which is changed.
     * @param optionality the new optionality of the specified variable.
     */
    private void checkOptionality(@NotNull ReferenceValue referenceValue, @NotNull Optionality optionality) {
        if (getOptionality(referenceValue) == Optionality.UNKNOWN) {
            if (optionality == Optionality.PRESENT) {
                handleMutuallyExclusiveArguments(referenceValue);
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
            variables.add(new VariableValue(argument));
            ReferenceSnapshot snapshot = state.roots.get(argument);
            Value value = arguments.get(argument);
            if (!(value instanceof ReferenceValue referenceValue)) {
                continue;
            }
            Optional<ReferenceSnapshot> optional = getSnapshot(referenceValue);
            if(optional.isEmpty()) {
                continue;
            }
            modifications.put(new VariableValue(argument), referenceValue);
            remapped.put(snapshot, optional.orElseThrow());
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
            VariableValue value = new VariableValue(argument);
            Optional<ReferenceSnapshot> optional = state.getSnapshot(value);
            if (optional.isEmpty()) {
                continue;
            }
            ReferenceSnapshot snapshot = optional.orElseThrow();
            if (remapped.containsKey(snapshot)) {
                snapshot = remapped.get(snapshot);
            }
            if (!(arguments.get(argument) instanceof ReferenceValue)) {
                add(new Condition(snapshot, ConditionType.EQUALITY, new ValueExpression(arguments.get(argument))), true);
            }
            if (argument.parameterType() != ParameterType.INPUT) {
                if (arguments.get(argument) instanceof ReferenceValue variable) {
                    /*
                     * The argument which was passed to the function now reflects any potential modifications made by the function.
                     */
                    assign(new Condition(variable, ConditionType.EQUALITY, new ValueExpression(snapshot)), false);
                }
            }
        }
        if (returnValue != null) {
            if (returnTarget == null) {
                throw new IllegalArgumentException();
            }
            Optional<ReferenceSnapshot> optional = state.getSnapshot(returnValue);
            if (optional.isPresent()) {
                ReferenceSnapshot snapshot = remapped.get(optional.orElseThrow());
                Objects.requireNonNull(snapshot);
                assign(new Condition(returnTarget, ConditionType.EQUALITY, new ValueExpression(snapshot)), false);
            }
        }
    }

    private @NotNull ReferenceValue getModifiedSnapshot(@NotNull ReferenceValue previous, @NotNull Map<ReferenceValue, ReferenceValue> modifications, @NotNull Map<ReferenceSnapshot, ReferenceSnapshot> remapped) {
        if (!(previous instanceof ReferenceSnapshot previousSnapshot)) {
            return previous;
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
            Optional<ReferenceSnapshot> snapshot = state.getSnapshot(value);
            snapshot.ifPresent(workList::add);
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

    private void handleMutuallyExclusiveArguments(@NotNull ReferenceValue referenceValue) {
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
            Optional<ReferenceSnapshot> optional = getSnapshot(variableValue);
            if(optional.isEmpty() || !(optional.orElseThrow() instanceof VariableSnapshot variableSnapshot)) {
                continue;
            }
            constraints.put(variableSnapshot, getConstraint(variableSnapshot).setOptionality(Optionality.MISSING));
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

    public @NotNull Block getFunctionBlock() {
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
     * @return the constraint of the value, or an empty optional if the constraint could not be calculated.
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
        Optional<ReferenceSnapshot> optional = getSnapshot(variable);
        if (optional.isEmpty()) {
            return Optional.of(Constraint.any(variable.getType()));
        }
        ReferenceSnapshot snapshot = optional.orElseThrow();
        List<Constraint> constraints = getConditions(snapshot).stream()
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
        Optional<ReferenceSnapshot> snapshot = getSnapshot(variable);
        if (snapshot.isEmpty() || !(snapshot.orElseThrow() instanceof VariableSnapshot variableSnapshot)) {
            return Optional.empty();
        }
        if (constraints.containsKey(variableSnapshot)) {
            return Optional.of(constraints.get(variableSnapshot));
        }
        if (predecessor != null) {
            return predecessor.getPrecomputedConstraint(variableSnapshot);
        }
        return Optional.empty();
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


    public @NotNull Optional<ReferenceSnapshot> createSnapshot(@NotNull ReferenceValue referenceValue) {
        if (referenceValue instanceof ReferenceSnapshot snapshot) {
            return Optional.of(snapshot);
        }
        if (referenceValue instanceof FieldValue) {
            return Optional.empty();
        }
        if (referenceValue instanceof IndexValue indexValue) {
            Optional<ReferenceSnapshot> snapshot = getSnapshot(indexValue.variable());
            if (snapshot.isEmpty() || !(snapshot.orElseThrow() instanceof ArraySnapshot arraySnapshot)) {
                return Optional.empty();
            }
            Value index = getSnapshot(indexValue.index());
            return Optional.of(arraySnapshot.createSnapshot(index));
        }
        if (referenceValue instanceof VariableValue variableValue) {
            return Optional.of(initializeSnapshot(variableValue));
        }
        if (referenceValue instanceof ComponentValue componentValue) {
            Optional<ReferenceSnapshot> snapshot = getSnapshot(componentValue.variable());
            if (snapshot.isEmpty() || !(snapshot.orElseThrow() instanceof RecordSnapshot recordSnapshot)) {
                return Optional.empty();
            }
            VariableSnapshot componentSnapshot = new VariableSnapshot(componentValue.getType());
            recordSnapshot.assign(componentValue.name(), componentSnapshot);
            return Optional.of(componentSnapshot);
        }
        throw new IllegalArgumentException();
    }

    private @NotNull ReferenceSnapshot initializeSnapshot(@NotNull ReferenceValue referenceValue) {
        RapidType type = referenceValue.getType();
        ReferenceSnapshot snapshot;
        if (type.getDimensions() > 0) {
            VariableSnapshot anySnapshot = new VariableSnapshot(RapidType.NUMBER);
            add(anySnapshot, NumericConstraint.any());
            IndexValue indexValue = new IndexValue(referenceValue, anySnapshot);
            for (int i = 1; i < type.getDimensions(); i++) {
                indexValue = new IndexValue(indexValue, anySnapshot);
            }
            ReferenceSnapshot defaultValue = initializeSnapshot(indexValue);
            snapshot = new ArraySnapshot(defaultValue, referenceValue);
        } else if (type.getTargetStructure() instanceof RapidRecord record) {
            RecordSnapshot recordSnapshot = new RecordSnapshot(referenceValue);
            snapshot = recordSnapshot;
            for (RapidComponent component : record.getComponents()) {
                String componentName = component.getName();
                RapidType componentType = component.getType();
                if (componentName == null || componentType == null) {
                    continue;
                }
                ReferenceSnapshot componentSnapshot = initializeSnapshot(new ComponentValue(componentType, referenceValue, componentName));
                recordSnapshot.assign(componentName, componentSnapshot);
            }
        } else {
            snapshot = new VariableSnapshot(referenceValue);
            add(snapshot, Constraint.any(snapshot.getType()));
        }
        if (referenceValue instanceof VariableValue variableValue) {
            snapshots.put(variableValue, snapshot);
        }
        return snapshot;
    }

    public @NotNull Value getSnapshot(@NotNull Value value) {
        if (value instanceof ReferenceValue referenceValue) {
            Optional<ReferenceSnapshot> snapshot = getSnapshot(referenceValue);
            if (snapshot.isPresent()) {
                return snapshot.orElseThrow();
            }
        }
        return value;
    }

    /**
     * Attempts to retrieve the latest snapshot for the specified variable. If this state does not modify the variable,
     * the predecessors of this state are recursively queried until the latest snapshot is found. If this state or its
     * predecessors has no reference to this variable, or if the variable is a field, an empty optional is returned.
     *
     * @param referenceValue the variable.
     * @return the latest snapshot for the variable, or an empty optional if no reference to this variable was found.
     */
    public @NotNull Optional<ReferenceSnapshot> getSnapshot(@NotNull ReferenceValue referenceValue) {
        if (referenceValue instanceof FieldValue) {
            return Optional.empty();
        }
        if (referenceValue instanceof ReferenceSnapshot snapshot) {
            return Optional.of(snapshot);
        }
        if (referenceValue instanceof VariableValue variable) {
            if (snapshots.containsKey(variable)) {
                return Optional.of(snapshots.get(variable));
            } else if (predecessor != null) {
                return predecessor.getSnapshot(referenceValue);
            } else {
                return Optional.empty();
            }
        }
        if (referenceValue instanceof IndexValue indexValue) {
            Optional<ReferenceSnapshot> snapshot = getSnapshot(indexValue.variable());
            if (snapshot.isEmpty() || !(snapshot.orElseThrow() instanceof ArraySnapshot arraySnapshot)) {
                return Optional.empty();
            }
            Value indexSnapshot = getSnapshot(indexValue.index());
            List<ArrayEntry> assignments = arraySnapshot.getAssignments(this, indexSnapshot);
            if (assignments.size() > 1) {
                throw new IllegalStateException("Array: " + arraySnapshot + " provided multiple assignments for index: " + indexSnapshot);
            } else if (assignments.isEmpty()) {
                throw new IllegalStateException("Array: " + arraySnapshot + " provided no assignments for index " + indexSnapshot);
            }
            ArrayEntry arrayEntry = assignments.get(0);
            if (arrayEntry instanceof ArrayEntry.DefaultValue defaultAssignment) {
                Value value = defaultAssignment.defaultValue();
                if (!(value instanceof ReferenceValue defaultValue)) {
                    VariableSnapshot variableSnapshot = new VariableSnapshot(value.getType());
                    add(new Condition(variableSnapshot, ConditionType.EQUALITY, new ValueExpression(value)), false);
                    return Optional.of(variableSnapshot);
                }
                Optional<ReferenceSnapshot> referenceSnapshot = copy(defaultValue).or(() -> createSnapshot(defaultValue));
                referenceSnapshot.ifPresent(variable -> arraySnapshot.assign(indexSnapshot, variable));
                return referenceSnapshot;
            }
            if (arrayEntry instanceof ArrayEntry.Assignment assignment) {
                Value value = assignment.value();
                if (!(value instanceof ReferenceValue assignmentValue)) {
                    VariableSnapshot variableSnapshot = new VariableSnapshot(value.getType());
                    add(new Condition(variableSnapshot, ConditionType.EQUALITY, new ValueExpression(value)), false);
                    return Optional.of(variableSnapshot);
                }
                return getSnapshot(assignmentValue);
            }
        }
        if (referenceValue instanceof ComponentValue componentValue) {
            Optional<ReferenceSnapshot> snapshot = getSnapshot(componentValue.variable());
            if (snapshot.isEmpty() || !(snapshot.orElseThrow() instanceof RecordSnapshot recordSnapshot)) {
                return Optional.empty();
            }
            Value value = recordSnapshot.getValue(componentValue.name());
            if (value instanceof ReferenceValue variable) {
                return getSnapshot(variable);
            } else {
                VariableSnapshot variableSnapshot = new VariableSnapshot(value.getType());
                add(new Condition(variableSnapshot, ConditionType.EQUALITY, new ValueExpression(value)), false);
                return Optional.of(variableSnapshot);
            }
        }
        throw new IllegalArgumentException();
    }

    private @NotNull Optional<ReferenceSnapshot> copy(@NotNull ReferenceValue referenceValue) {
        if (!(referenceValue instanceof ReferenceSnapshot snapshot)) {
            /*
             * If the variable was modified since the array was initialized, the default value of the array is lost.
             */
            return Optional.empty();
        }
        if (snapshot instanceof VariableSnapshot variableSnapshot) {
            ReferenceValue variable = variableSnapshot.getVariable();
            VariableSnapshot copy = variable != null ? new VariableSnapshot(variable) : new VariableSnapshot(variableSnapshot.getType());
            add(new Condition(copy, ConditionType.EQUALITY, new ValueExpression(variableSnapshot)), false);
            return Optional.of(copy);
        }
        if (snapshot instanceof RecordSnapshot recordSnapshot) {
            RecordSnapshot copy = new RecordSnapshot(recordSnapshot.getVariable());
            copy.getSnapshots().putAll(recordSnapshot.getSnapshots());
            return Optional.of(copy);
        }
        if (snapshot instanceof ArraySnapshot arraySnapshot) {
            ArraySnapshot copy = new ArraySnapshot(arraySnapshot.getDefaultValue(), arraySnapshot.getVariable());
            copy.getAssignments().addAll(arraySnapshot.getAssignments());
            return Optional.of(arraySnapshot);
        }
        throw new IllegalArgumentException();
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
        Optional<ReferenceSnapshot> value = getSnapshot(referenceValue);
        if (value.isEmpty()) {
            return Set.of();
        }
        ReferenceSnapshot snapshot = value.orElseThrow();
        Set<Condition> result = new HashSet<>();
        for (Condition condition : conditions) {
            if (condition.getVariable().equals(snapshot)) {
                result.add(condition);
            }
        }
        if (snapshots.containsValue(snapshot)) {
            /*
             * The specified snapshot is declared in this state, as a result, it won't be declared in any previous state.
             */
            return result;
        }
        if (predecessor == null) {
            return result;
        }
        result.addAll(predecessor.getConditions(snapshot));
        return result;
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
    public String toString() {
        return "DataFlowState{" +
                "name=" + functionBlock.getModuleName() + ":" + functionBlock.getName() +
                ", conditions=" + conditions.size() +
                ", constraints=" + constraints.size() +
                '}';
    }

    public enum AssignmentType {
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

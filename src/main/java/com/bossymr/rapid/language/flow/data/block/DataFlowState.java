package com.bossymr.rapid.language.flow.data.block;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.data.PathCounter;
import com.bossymr.rapid.language.flow.data.snapshots.ArrayEntry;
import com.bossymr.rapid.language.flow.data.snapshots.ArraySnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.RecordSnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.microsoft.z3.Status;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Ref;
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
    private final @NotNull Set<Expression> conditions;

    /**
     * The latest snapshot of each variable. The latest snapshot for a given variable also represents the variable.
     */
    private final @NotNull Map<Field, SnapshotExpression> snapshots;

    /**
     * The constraints for all variables.
     */
    private final @NotNull Map<SnapshotExpression, Optionality> optionality;

    private DataFlowState(@NotNull DataFlowBlock block, @Nullable DataFlowState predecessor) {
        this(block, block.getBasicBlock().getBlock(), predecessor);
    }

    private DataFlowState(@NotNull Block functionBlock, @Nullable DataFlowState predecessor) {
        this(null, functionBlock, predecessor);
    }

    private DataFlowState(@Nullable DataFlowBlock block, @NotNull Block functionBlock, @Nullable DataFlowState predecessor) {
        this.predecessor = predecessor;
        this.block = block;
        this.functionBlock = functionBlock;
        this.conditions = new HashSet<>();
        this.snapshots = new HashMap<>();
        this.optionality = new HashMap<>();
    }

    private DataFlowState(@NotNull DataFlowState state) {
        this.block = state.block;
        this.predecessor = state.predecessor;
        this.functionBlock = state.functionBlock;
        this.snapshots = new HashMap<>();
        Map<SnapshotExpression, SnapshotExpression> remapping = new HashMap<>();
        for (var entry : state.snapshots.entrySet()) {
            snapshots.put(entry.getKey(), (SnapshotExpression) mapSnapshot(entry.getValue(), remapping));
        }
        this.conditions = new HashSet<>();
        for (Condition condition : state.conditions) {
            conditions.add(Condition.create(condition, value -> mapSnapshot(value, remapping)));
        }
        this.optionality = new HashMap<>();
        for (var entry : state.optionality.entrySet()) {
            optionality.put((VariableSnapshot) mapSnapshot(entry.getKey(), remapping), entry.getValue());
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

    public static @NotNull DataFlowState createSuccessorState(@NotNull Block block, @NotNull DataFlowState predecessor) {
        return new DataFlowState(block, predecessor);
    }

    public @NotNull Optional<DataFlowState> getPredecessor() {
        return Optional.ofNullable(predecessor);
    }

    private @NotNull Value mapSnapshot(@NotNull Value previous, @NotNull Map<SnapshotExpression, SnapshotExpression> remapping) {
        if (!(previous instanceof ReferenceValue referenceValue)) {
            return previous;
        }
        return mapSnapshot(referenceValue, remapping);
    }

    public @NotNull Optional<DataFlowBlock> getBlock() {
        return Optional.ofNullable(block);
    }

    private @NotNull ReferenceValue mapSnapshot(@NotNull ReferenceValue previous, @NotNull Map<SnapshotExpression, SnapshotExpression> remapping) {
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
        if (!(previous instanceof SnapshotExpression snapshot)) {
            throw new AssertionError();
        }
        if (remapping.containsKey(snapshot)) {
            return remapping.get(snapshot);
        }
        if (previous instanceof RecordSnapshot recordSnapshot) {
            RecordSnapshot copy = new RecordSnapshot(mapSnapshot(recordSnapshot.getUnderlyingVariable(), remapping));
            remapping.put(snapshot, copy);
            for (var entry : recordSnapshot.getSnapshots().entrySet()) {
                copy.assign(entry.getKey(), mapSnapshot(entry.getValue(), remapping));
            }
            return copy;
        }
        if (previous instanceof ArraySnapshot arraySnapshot) {
            ArraySnapshot copy = new ArraySnapshot(mapSnapshot(arraySnapshot.getDefaultValue(), remapping), mapSnapshot(arraySnapshot.getUnderlyingVariable(), remapping));
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
            snapshots.put(new VariableValue(variable), initialize(new VariableValue(variable), Optionality.PRESENT, assignmentType));
        }
        for (ArgumentGroup argumentGroup : functionBlock.getArgumentGroups()) {
            Optionality optionality = argumentGroup.isOptional() ? Optionality.UNKNOWN : Optionality.PRESENT;
            for (Argument argument : argumentGroup.arguments()) {
                snapshots.put(new VariableValue(argument), initialize(new VariableValue(argument), optionality, AssignmentType.UNKNOWN));
            }
        }
    }

    public @NotNull SnapshotExpression initialize(@NotNull ReferenceValue variable, @NotNull Optionality optionality, @NotNull AssignmentType assignmentType) {
        RapidType type = variable.getType();
        if (type.getDimensions() > 0) {
            RapidType elementType = type.createArrayType(0);
            VariableSnapshot defaultValue = new VariableSnapshot(elementType);
            add(defaultValue, assignmentType.getConstraint(optionality, elementType));
            return new ArraySnapshot(defaultValue, variable);
        } else if (type.getActualStructure() instanceof RapidRecord record) {
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

    public @NotNull Set<PathCounter> getPathCounters() {
        Set<PathCounter> counters = new HashSet<>();
        getPathCounters(counters);
        return counters;
    }

    private void getPathCounters(@NotNull Set<PathCounter> counters) {
        for (Expression condition : conditions) {
            Collection<Expression> components = condition.getAllComponents();
            for (Expression component : components) {
                if(component instanceof PathCounter pathCounter) {
                    counters.add(pathCounter);
                }
            }
        }
        if (predecessor != null) {
            predecessor.getPathCounters(counters);
        }
    }

    public void assign(@NotNull ReferenceExpression variable, @NotNull Expression expression) {
        if (variable instanceof VariableExpression leftValue && expression instanceof VariableExpression rightValue) {
            Optional<SnapshotExpression> snapshot = getSnapshot(rightValue);
            if (snapshot.isPresent()) {
                snapshots.put(leftValue.getField(), snapshot.orElseThrow());
                return;
            }
        }
        Optional<SnapshotExpression> snapshot = createSnapshot(variable);
        if(snapshot.isEmpty()) {
            return;
        }
        expression = expression.replace(this::getSnapshot);
        insert(new BinaryExpression(BinaryOperator.EQUAL_TO, RapidPrimitiveType.BOOLEAN, snapshot.orElseThrow(), expression));
    }

    public void add(@NotNull Expression expression) {
        expression = expression.replace(this::getSnapshot);
        insert(expression);
    }

    private void insert(@NotNull Expression expression) {
        if (!(expression.getType().isAssignable(RapidPrimitiveType.BOOLEAN))) {
            throw new IllegalArgumentException("Cannot add expression: " + expression);
        }
        for (Expression component : expression.getAllComponents()) {
            // Find all components in the expression (including for example the index and variable in for example an
            // index expression) and check if they are of type optional unknown or missing.
            // If they are, create a new snapshot of the expression and make it present or no value (if it was already
            // missing) - and add previousSnapshot := newSnapshot.
            // TODO: Rework getAllComponents() to optionally return all expressions, even those inside IndexExpression (but not inside ReferenceSnapshot)
        }
        // TODO: Rework below:
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
            } else if (type.getActualStructure() instanceof RapidRecord record) {
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

    public @NotNull Optionality getOptionality(@NotNull ReferenceExpression expression) {
        Optional<SnapshotExpression> snapshot = getSnapshot(expression);
        if(snapshot.isPresent()) {
            return optionality.getOrDefault(snapshot.orElseThrow(), Optionality.PRESENT);
        }
        return Optionality.PRESENT;
    }

    public @NotNull Set<Expression> getExpressions() {
        return conditions;
    }

    public @NotNull Map<Field, SnapshotExpression> getSnapshots() {
        return snapshots;
    }

    public @NotNull Map<VariableSnapshot, Optionality> getOptionality() {
        return optionality;
    }

    public @NotNull BooleanValue getConstraint(@NotNull Expression expression) {
        if (!(expression.getType().isAssignable(RapidPrimitiveType.BOOLEAN))) {
            throw new IllegalArgumentException("Cannot calculate constraint for expression: " + expression);
        }
        DataFlowState successor;
        if (block != null) {
            successor = createSuccessorState(block, this);
        } else {
            successor = createSuccessorState(functionBlock, this);
        }
        successor.add(expression);
        return null;
    }

    public @NotNull Status isSatisfiable() {
        return null;
    }

    public @NotNull Optional<SnapshotExpression> createSnapshot(@NotNull ReferenceExpression expression) {
        if(expression instanceof SnapshotExpression snapshot) {
            expression = snapshot.getUnderlyingVariable();
            if(expression == null) {
                return Optional.of(snapshot);
            }
        }
        if(expression instanceof FieldExpression) {
            return Optional.empty();
        }
        if(expression instanceof IndexExpression indexExpression) {
            Optional<SnapshotExpression> optional = getSnapshot(indexExpression.getVariable());
            if(optional.isEmpty() || !(optional.orElseThrow() instanceof ArraySnapshot arraySnapshot)) {
                return Optional.empty();
            }
            Expression indexSnapshot = getSnapshot(indexExpression.getIndex());
            IndexExpression underlyingExpression = new IndexExpression(arraySnapshot, indexSnapshot);
            SnapshotExpression variableSnapshot = createSnapshot(indexExpression.getType(), underlyingExpression);
            arraySnapshot.assign(indexSnapshot, variableSnapshot);
            return Optional.of(variableSnapshot);
        }
        if(expression instanceof ComponentExpression componentExpression) {
            Optional<SnapshotExpression> snapshot = getSnapshot(componentExpression.getVariable());
            if (snapshot.isEmpty() || !(snapshot.orElseThrow() instanceof RecordSnapshot recordSnapshot)) {
                return Optional.empty();
            }
            ComponentExpression underlyingExpression = new ComponentExpression(componentExpression.getType(), recordSnapshot, componentExpression.getComponent());
            SnapshotExpression variableSnapshot = createSnapshot(componentExpression.getType(), underlyingExpression);
            recordSnapshot.assign(componentExpression.getComponent(), variableSnapshot);
            return Optional.of(variableSnapshot);
        }
        return Optional.of(createSnapshot(expression.getType(), expression));
    }

    private @NotNull SnapshotExpression createSnapshot(@NotNull RapidType snapshotType, @NotNull ReferenceExpression underlyingExpression) {
        SnapshotExpression snapshot;
        if (snapshotType.getDimensions() > 0) {
            snapshot = new ArraySnapshot((arraySnapshot) -> {
                VariableSnapshot indexSnapshot = new VariableSnapshot(RapidPrimitiveType.NUMBER);
                IndexExpression indexExpression = new IndexExpression(arraySnapshot, indexSnapshot);
                return createSnapshot(indexExpression).orElseThrow();
            }, underlyingExpression);
        } else if (snapshotType.getActualStructure() instanceof RapidRecord record) {
            RecordSnapshot recordSnapshot = new RecordSnapshot(underlyingExpression);
            snapshot = recordSnapshot;
            for (RapidComponent component : record.getComponents()) {
                String componentName = component.getName();
                RapidType componentType = component.getType();
                if (componentName == null || componentType == null) {
                    continue;
                }
                ComponentExpression componentExpression = new ComponentExpression(componentType, underlyingExpression, componentName);
                SnapshotExpression componentSnapshot = createSnapshot(componentExpression).orElseThrow();
                recordSnapshot.assign(componentName, componentSnapshot);
            }
        } else {
            snapshot = new VariableSnapshot(underlyingExpression);
        }
        if (underlyingExpression instanceof VariableExpression variableValue) {
            snapshots.put(variableValue.getField(), snapshot);
        }
        return snapshot;
    }

    public @NotNull Expression getSnapshot(@NotNull Expression value) {
        if (value instanceof ReferenceExpression referenceValue) {
            Optional<SnapshotExpression> snapshot = getSnapshot(referenceValue);
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
     * @param expression the variable.
     * @return the latest snapshot for the variable, or an empty optional if no reference to this variable was found.
     */
    public @NotNull Optional<SnapshotExpression> getSnapshot(@NotNull ReferenceExpression expression) {
        if (expression instanceof FieldExpression) {
            return Optional.empty();
        }
        if (expression instanceof SnapshotExpression snapshot) {
            return Optional.of(snapshot);
        }
        if (expression instanceof VariableExpression variable) {
            Field field = variable.getField();
            if (snapshots.containsKey(field)) {
                return Optional.of(snapshots.get(field));
            } else if (predecessor != null) {
                return predecessor.getSnapshot(expression);
            } else {
                return Optional.empty();
            }
        }
        if (expression instanceof IndexExpression indexValue) {
            Optional<SnapshotExpression> snapshot = getSnapshot(indexValue.getVariable());
            if (snapshot.isEmpty() || !(snapshot.orElseThrow() instanceof ArraySnapshot arraySnapshot)) {
                return Optional.empty();
            }
            Expression indexSnapshot = getSnapshot(indexValue.getIndex());
            List<ArrayEntry> assignments = arraySnapshot.getAssignments(this, indexSnapshot);
            if (assignments.size() > 1) {
                throw new IllegalStateException("Array: " + arraySnapshot + " provided " + assignments.size() + " assignments for index " + indexSnapshot);
            } else if (assignments.isEmpty()) {
                throw new IllegalStateException("Array: " + arraySnapshot + " provided no assignments for index " + indexSnapshot);
            }
            ArrayEntry arrayEntry = assignments.get(0);
            if (arrayEntry instanceof ArrayEntry.DefaultValue defaultAssignment) {
                Expression value = defaultAssignment.defaultValue();
                if (!(value instanceof ReferenceExpression defaultValue)) {
                    VariableSnapshot variableSnapshot = new VariableSnapshot(value.getType());
                    add(new BinaryExpression(BinaryOperator.EQUAL_TO, RapidPrimitiveType.BOOLEAN, variableSnapshot, value));
                    return Optional.of(variableSnapshot);
                }
                Optional<SnapshotExpression> defaultSnapshot = getSnapshot(defaultValue);
                if(defaultSnapshot.isPresent()) {
                    arraySnapshot.assign(indexSnapshot, defaultSnapshot.orElseThrow());
                }
                return defaultSnapshot;
            }
            if (arrayEntry instanceof ArrayEntry.Assignment assignment) {
                Expression value = assignment.value();
                if (!(value instanceof ReferenceExpression assignmentValue)) {
                    VariableSnapshot variableSnapshot = new VariableSnapshot(value.getType());
                    add(new BinaryExpression(BinaryOperator.EQUAL_TO, RapidPrimitiveType.BOOLEAN, variableSnapshot, value));
                    return Optional.of(variableSnapshot);
                }
                return getSnapshot(assignmentValue);
            }
        }
        if (expression instanceof ComponentExpression componentValue) {
            Optional<SnapshotExpression> snapshot = getSnapshot(componentValue.getVariable());
            if (snapshot.isEmpty() || !(snapshot.orElseThrow() instanceof RecordSnapshot recordSnapshot)) {
                return Optional.empty();
            }
            Expression value = recordSnapshot.getValue(componentValue.getComponent());
            if (value instanceof ReferenceExpression variable) {
                return getSnapshot(variable);
            } else {
                VariableSnapshot variableSnapshot = new VariableSnapshot(value.getType());
                add(new BinaryExpression(BinaryOperator.EQUAL_TO, RapidPrimitiveType.BOOLEAN, variableSnapshot, value));
                return Optional.of(variableSnapshot);
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        return "DataFlowState{" +
                "name=" + functionBlock.getModuleName() + ":" + functionBlock.getName() +
                ", conditions=" + conditions.size() +
                ", constraints=" + optionality.size() +
                '}';
    }
}

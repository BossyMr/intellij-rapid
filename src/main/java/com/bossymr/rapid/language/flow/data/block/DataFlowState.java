package com.bossymr.rapid.language.flow.data.block;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.ConditionAnalyzer;
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
    private final @NotNull List<Expression> conditions;

    /**
     * The latest snapshot of each variable. The latest snapshot for a given variable also represents the variable.
     */
    private final @NotNull Map<Field, SnapshotExpression> snapshots;

    /**
     * The constraints for all variables.
     */
    private final @NotNull Map<SnapshotExpression, Optionality> optionality;

    private DataFlowState(@Nullable DataFlowBlock block, @NotNull Block functionBlock, @Nullable DataFlowState predecessor) {
        this.predecessor = predecessor;
        this.block = block;
        this.functionBlock = functionBlock;
        this.conditions = new ArrayList<>();
        this.snapshots = new HashMap<>();
        this.optionality = new HashMap<>();
    }

    private DataFlowState(@NotNull DataFlowState state, @Nullable DataFlowState predecessor) {
        this.block = state.block;
        this.predecessor = predecessor;
        this.functionBlock = state.functionBlock;
        this.snapshots = new HashMap<>();
        Map<SnapshotExpression, SnapshotExpression> map = new HashMap<>();
        state.snapshots.forEach((field, snapshot) -> snapshots.put(field, mapSnapshot(snapshot, new HashMap<>(), map, state)));
        this.conditions = new ArrayList<>();
        for (Expression expression : state.conditions) {
            conditions.add(expression.replace(component -> mapExpression(component, new HashMap<>(), map, state)));
        }
        this.optionality = new HashMap<>();
        state.optionality.forEach((snapshot, optionality) -> {
            this.optionality.put(mapSnapshot(snapshot, new HashMap<>(), map, state), optionality);
        });
    }

    /**
     * Create a new copy of the specified state.
     *
     * @param state the state.
     * @return the copy.
     */
    public static @NotNull DataFlowState copy(@NotNull DataFlowState state) {
        return new DataFlowState(state, state.getPredecessor().orElse(null));
    }

    public static @NotNull DataFlowState copy(@NotNull DataFlowState state, @Nullable DataFlowState predecessor) {
        return new DataFlowState(state, predecessor);
    }

    /**
     * Create a new state for the specified block. All variables are initialized to their default value, and all state
     * are assigned to any value.
     *
     * @param block the block.
     * @return the state.
     */
    public static @NotNull DataFlowState createState(@NotNull Block.FunctionBlock block) {
        DataFlowState state = new DataFlowState(null, block, null);
        state.initializeDefault();
        return state;
    }

    public static @NotNull DataFlowState createState(@NotNull DataFlowBlock block) {
        DataFlowState state = new DataFlowState(block, block.getBasicBlock().getBlock(), null);
        state.initializeDefault();
        return state;
    }

    /**
     * Create a new state for the specified block. All variables and state are assigned to any value. This method is
     * used for if a block has no predecessor, but is not the entry point of a method, all variables should not be equal
     * to zero.
     *
     * @param block the block.
     * @return the state.
     */
    public static @NotNull DataFlowState createUnknownState(@NotNull Block.FunctionBlock block) {
        DataFlowState state = new DataFlowState(null, block, null);
        state.initializeUnknown();
        return state;
    }

    public static @NotNull DataFlowState createUnknownState(@NotNull DataFlowBlock block) {
        DataFlowState state = new DataFlowState(block, block.getBasicBlock().getBlock(), null);
        state.initializeUnknown();
        return state;
    }

    public static @NotNull DataFlowState createSuccessorState(@NotNull DataFlowBlock block, @NotNull DataFlowState predecessor) {
        return new DataFlowState(block, block.getBasicBlock().getBlock(), predecessor);
    }

    public static @NotNull DataFlowState createSuccessorState(@NotNull Block block, @NotNull DataFlowState predecessor) {
        return new DataFlowState(null, block, predecessor);
    }

    public @NotNull DataFlowState createSuccessorState() {
        if (block != null) {
            return createSuccessorState(block, this);
        } else {
            return createSuccessorState(functionBlock, this);
        }
    }

    public @NotNull DataFlowState createCompactSuccessor() {
        DataFlowState successor = new DataFlowState(block, functionBlock, null);
        DataFlowState state = this;
        List<DataFlowState> history = new ArrayList<>();
        while (state != null) {
            history.add(state);
            Optional<DataFlowState> predecessor = state.getPredecessor();
            state = predecessor.orElse(null);
        }
        for (int i = history.size() - 1; i >= 0; i--) {
            DataFlowState predecessor = history.get(i);
            successor.getSnapshots().putAll(predecessor.getSnapshots());
            successor.getExpressions().addAll(predecessor.getExpressions());
            successor.getOptionality().putAll(predecessor.getOptionality());
        }
        return successor;
    }

    public @NotNull Optional<DataFlowState> getPredecessor() {
        return Optional.ofNullable(predecessor);
    }

    private void initializeUnknown() {
        for (Variable variable : functionBlock.getVariables()) {
            createSnapshot(new VariableExpression(variable));
        }
        initializeArguments();
    }

    private void initializeDefault() {
        for (Variable variable : functionBlock.getVariables()) {
            snapshots.put(variable, createDefaultSnapshot(new VariableExpression(variable)));
        }
        initializeArguments();
    }

    private void initializeArguments() {
        for (ArgumentGroup argumentGroup : functionBlock.getArgumentGroups()) {
            for (Argument argument : argumentGroup.arguments()) {
                Optional<SnapshotExpression> optional = createSnapshot(new VariableExpression(argument));
                optional.ifPresent(snapshot -> {
                    if (argumentGroup.isOptional()) {
                        optionality.put(snapshot, Optionality.UNKNOWN);
                    }
                });
            }
        }
    }

    public @NotNull SnapshotExpression createDefaultSnapshot(@NotNull ReferenceExpression variable) {
        RapidType type = variable.getType();
        if (type.getDimensions() > 0) {
            return new ArraySnapshot((parent) -> createDefaultSnapshot(new IndexExpression(parent, new VariableSnapshot(RapidPrimitiveType.NUMBER))), variable.getType(), variable);
        } else if (type.getActualStructure() instanceof RapidRecord record) {
            RecordSnapshot snapshot = new RecordSnapshot(variable.getType(), variable);
            for (RapidComponent component : record.getComponents()) {
                RapidType componentType = component.getType();
                String componentName = component.getName();
                if (componentType == null || componentName == null) {
                    continue;
                }
                SnapshotExpression defaultSnapshot = createDefaultSnapshot(new ComponentExpression(componentType, snapshot, componentName));
                snapshot.assign(componentName, defaultSnapshot);
            }
            return snapshot;
        } else {
            VariableSnapshot snapshot = new VariableSnapshot(variable);
            Object object = null;
            if (type.isAssignable(RapidPrimitiveType.BOOLEAN)) {
                object = false;
            } else if (type.isAssignable(RapidPrimitiveType.STRING)) {
                object = "";
            } else if (type.isAssignable(RapidPrimitiveType.NUMBER) || type.isAssignable(RapidPrimitiveType.DOUBLE)) {
                object = 0;
            }
            if (object != null) {
                add(new BinaryExpression(BinaryOperator.EQUAL_TO, snapshot, new ConstantExpression(type, object)));
            }
            return snapshot;
        }
    }

    public @NotNull Optional<DataFlowBlock> getBlock() {
        return Optional.ofNullable(block);
    }

    private @NotNull Expression mapExpression(@NotNull Expression previous, @NotNull Map<ReferenceExpression, ReferenceExpression> variableMap, @NotNull Map<SnapshotExpression, SnapshotExpression> snapshotMap, @NotNull DataFlowState state) {
        if (!(previous instanceof ReferenceExpression expression)) {
            return previous;
        }
        return mapSnapshot(expression, variableMap, snapshotMap, state);
    }

    @Contract("null, _, _, _ -> null; !null, _, _, _ -> !null")
    @SuppressWarnings("unchecked")
    private <T extends ReferenceExpression> @Nullable T mapSnapshot(@Nullable T previous, @NotNull Map<ReferenceExpression, ReferenceExpression> variableMap, @NotNull Map<SnapshotExpression, SnapshotExpression> snapshotMap, @NotNull DataFlowState state) {
        if (previous == null) {
            return null;
        }
        if (variableMap.containsKey(previous)) {
            return (T) variableMap.get(previous);
        }
        if (previous instanceof FieldExpression || previous instanceof VariableExpression) {
            return previous;
        }
        if (previous instanceof IndexExpression indexExpression) {
            return (T) new IndexExpression(mapSnapshot(indexExpression.getVariable(), variableMap, snapshotMap, state), mapExpression(indexExpression.getIndex(), variableMap, snapshotMap, state));
        }
        if (previous instanceof ComponentExpression componentExpression) {
            return (T) new ComponentExpression(componentExpression.getType(), mapSnapshot(componentExpression.getVariable(), variableMap, snapshotMap, state), componentExpression.getComponent());
        }
        if (!(previous instanceof SnapshotExpression snapshot)) {
            throw new IllegalArgumentException("Could not map: " + previous);
        }
        if (snapshotMap.containsKey(snapshot)) {
            return (T) snapshotMap.get(snapshot);
        }
        if (!(state.snapshots.containsValue(snapshot))) {
            // Do not replace snapshots declared in previous states, since expressions in this state would no longer reference the same variables.
            snapshotMap.put(snapshot, snapshot);
            return previous;
        }
        if (previous instanceof RecordSnapshot recordSnapshot) {
            RecordSnapshot copy = new RecordSnapshot(snapshot.getType(), mapSnapshot(recordSnapshot.getUnderlyingVariable(), variableMap, snapshotMap, state));
            snapshotMap.put(snapshot, copy);
            for (var entry : recordSnapshot.getSnapshots().entrySet()) {
                copy.assign(entry.getKey(), mapExpression(entry.getValue(), variableMap, snapshotMap, state));
            }
            return (T) copy;
        }
        if (previous instanceof ArraySnapshot arraySnapshot) {
            ArraySnapshot copy = new ArraySnapshot(parent -> mapExpression(arraySnapshot.getDefaultValue().get(), variableMap, snapshotMap, state), arraySnapshot.getType(), mapSnapshot(arraySnapshot.getUnderlyingVariable(), variableMap, snapshotMap, state));
            snapshotMap.put(snapshot, copy);
            for (ArrayEntry.Assignment assignment : arraySnapshot.getAssignments()) {
                copy.assign(mapExpression(assignment.index(), variableMap, snapshotMap, state), mapExpression(assignment.value(), variableMap, snapshotMap, state));
            }
            return (T) copy;
        }
        if (previous instanceof VariableSnapshot variableSnapshot) {
            VariableSnapshot copy = new VariableSnapshot(variableSnapshot.getType(), mapSnapshot(variableSnapshot.getUnderlyingVariable(), variableMap, snapshotMap, state));
            snapshotMap.put(variableSnapshot, copy);
            return (T) copy;
        }
        throw new AssertionError();
    }

    public @NotNull DataFlowState modify(@NotNull Map<ReferenceExpression, ReferenceExpression> modifications) {
        DataFlowState state = new DataFlowState(block, functionBlock, predecessor);
        Map<SnapshotExpression, SnapshotExpression> snapshotMap = new HashMap<>();
        snapshots.forEach((field, snapshot) -> state.snapshots.put(field, mapSnapshot(snapshot, modifications, snapshotMap, state)));
        for (Expression expression : conditions) {
            state.conditions.add(expression.replace(component -> mapExpression(component, modifications, snapshotMap, this)));
        }
        optionality.forEach((snapshot, optionality) -> {
            state.optionality.put(mapSnapshot(snapshot, modifications, snapshotMap, state), optionality);
        });
        return state;
    }

    public @NotNull Set<PathCounter> getPathCounters() {
        Set<PathCounter> counters = new HashSet<>();
        getPathCounters(counters);
        return counters;
    }

    private void getPathCounters(@NotNull Set<PathCounter> counters) {
        for (Expression condition : conditions) {
            Collection<Expression> components = condition.getComponents();
            for (Expression component : components) {
                if (component instanceof PathCounter pathCounter) {
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
        if (snapshot.isEmpty()) {
            return;
        }
        expression = expression.replace(this::getSnapshot);
        insert(new BinaryExpression(BinaryOperator.EQUAL_TO, snapshot.orElseThrow(), expression));
    }

    public void add(@NotNull Expression expression) {
        expression = expression.replace(this::getSnapshot);
        insert(expression);
    }

    private void insert(@NotNull Expression expression) {
        if (!(expression.getType().isAssignable(RapidPrimitiveType.BOOLEAN))) {
            throw new IllegalArgumentException("Cannot add expression: " + expression);
        }
        for (Expression component : expression.getComponents()) {
            if (component instanceof SnapshotExpression snapshot) {
                Optionality optionality = this.optionality.get(snapshot);
                if (optionality == Optionality.PRESENT) {
                    setOptionality(snapshot, Optionality.PRESENT);
                }
            }
        }
        if (expression instanceof BinaryExpression binaryExpression) {
            if (binaryExpression.getLeft() instanceof AggregateExpression || binaryExpression.getRight() instanceof AggregateExpression) {
                if (binaryExpression.getLeft() instanceof AggregateExpression) {
                    if (binaryExpression.getRight() instanceof AggregateExpression) {
                        throw new IllegalArgumentException("Cannot add expression: " + expression);
                    }
                    binaryExpression = new BinaryExpression(binaryExpression.getOperator(), binaryExpression.getRight(), binaryExpression.getLeft());
                }
                if (binaryExpression.getLeft() instanceof ReferenceExpression referenceExpression) {
                    AggregateExpression aggregateExpression = (AggregateExpression) binaryExpression.getRight();
                    List<Expression> components = aggregateExpression.getComponents();
                    if (binaryExpression.getType().getDimensions() > 0) {
                        for (int i = 0; i < components.size(); i++) {
                            Expression component = components.get(i);
                            IndexExpression indexExpression = new IndexExpression(referenceExpression, new ConstantExpression(RapidPrimitiveType.NUMBER, i));
                            assign(indexExpression, component);
                        }
                        return;
                    } else if (binaryExpression.getType().getActualStructure() instanceof RapidRecord record) {
                        List<RapidComponent> fields = record.getComponents();
                        if (fields.size() != components.size()) {
                            throw new IllegalArgumentException("Cannot add expression: " + expression);
                        }
                        for (int i = 0; i < fields.size(); i++) {
                            RapidComponent field = fields.get(i);
                            Expression component = components.get(i);
                            RapidType componentType = Objects.requireNonNullElse(field.getType(), RapidPrimitiveType.ANYTYPE);
                            String componentName = Objects.requireNonNullElse(field.getName(), "<ID>");
                            ComponentExpression componentExpression = new ComponentExpression(componentType, referenceExpression, componentName);
                            assign(componentExpression, component);
                        }
                        return;
                    } else {
                        throw new IllegalArgumentException("Cannot add expression: " + expression);
                    }
                }
            }
        }
        conditions.add(expression);
    }

    public @NotNull Optionality getOptionality(@NotNull ReferenceExpression expression) {
        Optional<SnapshotExpression> optional = getSnapshot(expression);
        if (optional.isPresent()) {
            SnapshotExpression snapshot = optional.orElseThrow();
            if(optionality.containsKey(snapshot)) {
                return optionality.get(snapshot);
            }
            if(predecessor != null) {
                return predecessor.getOptionality(snapshot);
            }
        }
        return Optionality.PRESENT;
    }

    public void forceOptionality(@NotNull ReferenceExpression expression, @NotNull Optionality optionality) {
        Optionality current = getOptionality(expression);
        if (current.and(optionality) == current) {
            return;
        }
        Optional<SnapshotExpression> optional = createSnapshot(expression);
        if(optional.isPresent()) {
            SnapshotExpression snapshot = optional.orElseThrow();
            this.optionality.put(snapshot, optionality);
            add(new BinaryExpression(BinaryOperator.EQUAL_TO, snapshot, expression));
        }
    }

    public void setOptionality(@NotNull ReferenceExpression expression, @NotNull Optionality optionality) {
        Field field = getField(expression);
        if (!(field instanceof Argument argument)) {
            return;
        }
        ArgumentGroup argumentGroup = getArgumentGroup(argument);
        if (argumentGroup == null) {
            return;
        }
        Optionality current = getOptionality(expression);
        if (current.and(optionality) == current) {
            return;
        }
        Optional<SnapshotExpression> optional = createSnapshot(expression);
        if (optional.isEmpty()) {
            return;
        }
        SnapshotExpression snapshot = optional.orElseThrow();
        this.optionality.put(snapshot, current.and(optionality));
        add(new BinaryExpression(BinaryOperator.EQUAL_TO, snapshot, expression));
        if(optionality == Optionality.PRESENT) {
            for (Argument otherArgument : argumentGroup.arguments()) {
                if(otherArgument == argument) {
                    continue;
                }
                setOptionality(new VariableExpression(otherArgument), Optionality.MISSING);
            }
        }
    }

    private @Nullable ArgumentGroup getArgumentGroup(@NotNull Argument argument) {
        for (ArgumentGroup argumentGroup : functionBlock.getArgumentGroups()) {
            if (argumentGroup.arguments().contains(argument)) {
                return argumentGroup;
            }
        }
        return null;
    }

    private @Nullable Field getField(@NotNull ReferenceExpression expression) {
        return expression.accept(new ControlFlowVisitor<>() {
            @Override
            public Field visitIndexExpression(@NotNull IndexExpression expression) {
                return getField(expression.getVariable());
            }

            @Override
            public Field visitComponentExpression(@NotNull ComponentExpression expression) {
                return getField(expression.getVariable());
            }

            @Override
            public Field visitFieldExpression(@NotNull FieldExpression expression) {
                return null;
            }

            @Override
            public Field visitVariableExpression(@NotNull VariableExpression expression) {
                return expression.getField();
            }

            @Override
            public Field visitSnapshotExpression(@NotNull SnapshotExpression snapshot) {
                if (snapshot.getUnderlyingVariable() != null) {
                    return getField(snapshot.getUnderlyingVariable());
                }
                return null;
            }
        });
    }

    public @NotNull List<Expression> getExpressions() {
        return conditions;
    }

    public @NotNull Map<Field, SnapshotExpression> getSnapshots() {
        return snapshots;
    }

    public @NotNull Map<SnapshotExpression, Optionality> getOptionality() {
        return optionality;
    }

    public @NotNull BooleanValue getConstraint(@NotNull Expression expression) {
        if (!(expression.getType().isAssignable(RapidPrimitiveType.BOOLEAN))) {
            throw new IllegalArgumentException("Cannot calculate constraint for expression: " + expression);
        }
        boolean isTrue = checkSatisfiability(expression, true);
        boolean isFalse = checkSatisfiability(expression, false);
        if (isTrue && isFalse) {
            return BooleanValue.ANY_VALUE;
        }
        if (isTrue) {
            return BooleanValue.ALWAYS_TRUE;
        }
        if (isFalse) {
            return BooleanValue.ALWAYS_FALSE;
        }
        return BooleanValue.NO_VALUE;
    }

    private boolean checkSatisfiability(@NotNull Expression expression, boolean value) {
        DataFlowState state = createSuccessorState();
        state.add(expression);
        state.add(new BinaryExpression(BinaryOperator.EQUAL_TO, expression, new ConstantExpression(RapidPrimitiveType.BOOLEAN, value)));
        return state.isSatisfiable();
    }

    public boolean isSatisfiable() {
        return ConditionAnalyzer.isSatisfiable(this);
    }

    public @Nullable List<ConstantExpression> getSolutions(@NotNull Expression variable, int timeout) {
        return ConditionAnalyzer.getSolutions(this, variable, timeout);
    }

    public @NotNull Optional<SnapshotExpression> createSnapshot(@NotNull ReferenceExpression expression) {
        if (expression instanceof SnapshotExpression snapshot) {
            expression = snapshot.getUnderlyingVariable();
            if (expression == null) {
                return Optional.of(snapshot);
            }
        }
        if (expression instanceof FieldExpression) {
            return Optional.empty();
        }
        if (expression instanceof IndexExpression indexExpression) {
            Optional<SnapshotExpression> optional = getSnapshot(indexExpression.getVariable());
            if (optional.isEmpty() || !(optional.orElseThrow() instanceof ArraySnapshot arraySnapshot)) {
                return Optional.empty();
            }
            Expression indexSnapshot = getSnapshot(indexExpression.getIndex());
            IndexExpression underlyingExpression = new IndexExpression(arraySnapshot, indexSnapshot);
            SnapshotExpression variableSnapshot = createSnapshot(indexExpression.getType(), underlyingExpression);
            arraySnapshot.assign(indexSnapshot, variableSnapshot);
            return Optional.of(variableSnapshot);
        }
        if (expression instanceof ComponentExpression componentExpression) {
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

    public @NotNull SnapshotExpression createSnapshot(@NotNull RapidType snapshotType, @Nullable ReferenceExpression underlyingExpression) {
        SnapshotExpression snapshot;
        if (snapshotType.getDimensions() > 0) {
            snapshot = new ArraySnapshot((arraySnapshot) -> {
                VariableSnapshot indexSnapshot = new VariableSnapshot(RapidPrimitiveType.NUMBER);
                IndexExpression indexExpression = new IndexExpression(arraySnapshot, indexSnapshot);
                return createSnapshot(indexExpression).orElseThrow();
            }, snapshotType, underlyingExpression);
        } else if (snapshotType.getActualStructure() instanceof RapidRecord record) {
            RecordSnapshot recordSnapshot = new RecordSnapshot(snapshotType, underlyingExpression);
            snapshot = recordSnapshot;
            for (RapidComponent component : record.getComponents()) {
                String componentName = component.getName();
                RapidType componentType = component.getType();
                if (componentName == null || componentType == null) {
                    continue;
                }
                ComponentExpression componentExpression = new ComponentExpression(componentType, recordSnapshot, componentName);
                SnapshotExpression componentSnapshot = createSnapshot(componentExpression).orElseThrow();
                recordSnapshot.assign(componentName, componentSnapshot);
            }
        } else {
            snapshot = new VariableSnapshot(snapshotType, underlyingExpression);
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
                    add(new BinaryExpression(BinaryOperator.EQUAL_TO, variableSnapshot, value));
                    return Optional.of(variableSnapshot);
                }
                Optional<SnapshotExpression> defaultSnapshot = getSnapshot(defaultValue);
                if (defaultSnapshot.isPresent()) {
                    arraySnapshot.assign(indexSnapshot, defaultSnapshot.orElseThrow());
                }
                return defaultSnapshot;
            }
            if (arrayEntry instanceof ArrayEntry.Assignment assignment) {
                Expression value = assignment.value();
                if (!(value instanceof ReferenceExpression assignmentValue)) {
                    VariableSnapshot variableSnapshot = new VariableSnapshot(value.getType());
                    add(new BinaryExpression(BinaryOperator.EQUAL_TO, variableSnapshot, value));
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
                add(new BinaryExpression(BinaryOperator.EQUAL_TO, variableSnapshot, value));
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

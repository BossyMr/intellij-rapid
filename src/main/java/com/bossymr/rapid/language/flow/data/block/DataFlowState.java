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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
    private final @NotNull List<Expression> conditions;

    /**
     * The latest snapshot of each variable. The latest snapshot for a given variable also represents the variable.
     */
    private final @NotNull Map<Field, SnapshotExpression> snapshots;

    /**
     * The first snapshot of each variable.
     */
    private final @NotNull Map<Field, SnapshotExpression> roots;

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
        this.roots = new HashMap<>();
    }

    private DataFlowState(@NotNull DataFlowState state, @Nullable DataFlowState predecessor) {
        this.block = state.block;
        this.predecessor = predecessor;
        this.functionBlock = state.functionBlock;
        this.snapshots = new HashMap<>();
        this.conditions = new ArrayList<>();
        this.optionality = new HashMap<>();
        this.roots = state.roots.isEmpty() ? Map.of() : new HashMap<>();
        Map<ReferenceExpression, ReferenceExpression> modifications = new HashMap<>();
        state.roots.forEach((field, snapshot) -> roots.put(field, (SnapshotExpression) modifyExpression(snapshot, modifications)));
        state.snapshots.forEach((field, snapshot) -> snapshots.put(field, (SnapshotExpression) modifyExpression(snapshot, modifications)));
        for (Expression expression : state.conditions) {
            conditions.add(expression.replace(component -> modifyExpression(component, modifications)));
        }
        state.optionality.forEach((snapshot, optionality) -> {
            SnapshotExpression expression = (SnapshotExpression) modifyExpression(snapshot, modifications);
            this.optionality.put(expression, optionality);
        });
    }

    /**
     * Create a new copy of the specified state.
     *
     * @param state the state.
     * @return the copy.
     */
    public static @NotNull DataFlowState copy(@NotNull DataFlowState state) {
        return new DataFlowState(state, state.getPredecessor());
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
        DataFlowState state = new DataFlowState(block, block.getInstruction().getBlock(), null);
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
        DataFlowState state = new DataFlowState(block, block.getInstruction().getBlock(), null);
        state.initializeUnknown();
        return state;
    }

    public static @NotNull DataFlowState createSuccessorState(@NotNull DataFlowBlock block, @NotNull DataFlowState predecessor) {
        return new DataFlowState(block, block.getInstruction().getBlock(), predecessor);
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

    public @NotNull DataFlowState createCompactState() {
        DataFlowState successor = new DataFlowState(block, functionBlock, null);
        DataFlowState state = this;
        List<DataFlowState> history = new ArrayList<>();
        while (state != null) {
            history.add(state);
            state = state.getPredecessor();
        }
        for (int i = history.size() - 1; i >= 0; i--) {
            DataFlowState predecessor = history.get(i);
            successor.getSnapshots().putAll(predecessor.getSnapshots());
            successor.getExpressions().addAll(predecessor.getExpressions());
            successor.getOptionality().putAll(predecessor.getOptionality());
        }
        return successor;
    }

    // TODO: Implement #createCompactState(@NotNull Instruction)

    public @Nullable DataFlowState getPredecessor() {
        return predecessor;
    }

    private void initializeUnknown() {
        for (Variable variable : functionBlock.getVariables()) {
            assign(new VariableExpression(variable), null);
        }
        initializeArguments();
        for (Field field : snapshots.keySet()) {
            roots.put(field, snapshots.get(field));
        }
    }

    private void initializeDefault() {
        for (Variable variable : functionBlock.getVariables()) {
            snapshots.put(variable, createDefaultSnapshot(new VariableExpression(variable)));
        }
        initializeArguments();
        for (Field field : snapshots.keySet()) {
            roots.put(field, snapshots.get(field));
        }
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
                add(new BinaryExpression(BinaryOperator.EQUAL_TO, snapshot, new ConstantExpression(object)));
            }
            return snapshot;
        }
    }

    public @Nullable DataFlowBlock getBlock() {
        return block;
    }

    private @NotNull Expression modifyExpression(@NotNull Expression previous, @NotNull Map<ReferenceExpression, ReferenceExpression> modifications) {
        if (previous instanceof ReferenceExpression referenceExpression) {
            return modifyExpression(referenceExpression, modifications);
        } else {
            return previous;
        }
    }

    private @NotNull ReferenceExpression modifyExpression(@NotNull ReferenceExpression previous, @NotNull Map<ReferenceExpression, ReferenceExpression> modifications) {
        if (modifications.containsKey(previous)) {
            return modifications.get(previous);
        }
        if (previous instanceof FieldExpression || previous instanceof VariableExpression) {
            return previous;
        }
        if (previous instanceof IndexExpression indexExpression) {
            ReferenceExpression underlyingVariable = modifyExpression(indexExpression.getVariable(), modifications);
            Expression indexVariable = modifyExpression(indexExpression.getIndex(), modifications);
            IndexExpression copy = new IndexExpression(indexExpression.getElement(), underlyingVariable, indexVariable);
            if (copy.equals(indexExpression)) {
                return indexExpression;
            }
            return copy;
        }
        if (previous instanceof ComponentExpression componentExpression) {
            ReferenceExpression underlyingVariable = modifyExpression(componentExpression.getVariable(), modifications);
            ComponentExpression copy = new ComponentExpression(componentExpression.getElement(), componentExpression.getType(), underlyingVariable, componentExpression.getComponent());
            if (copy.equals(componentExpression)) {
                return componentExpression;
            }
            return copy;
        }
        if (!(previous instanceof SnapshotExpression snapshotExpression)) {
            throw new IllegalArgumentException("Unexpected expression: " + previous);
        }
        if (isAssignedInChain(snapshotExpression)) {
            return snapshotExpression;
        }
        if (snapshotExpression instanceof RecordSnapshot recordSnapshot) {
            ReferenceExpression underlyingVariable = recordSnapshot.getUnderlyingVariable() != null ? modifyExpression(recordSnapshot.getUnderlyingVariable(), modifications) : null;
            RecordSnapshot copy = new RecordSnapshot(recordSnapshot.getType(), underlyingVariable);
            modifications.put(recordSnapshot, copy);
            for (Map.Entry<String, Expression> entry : recordSnapshot.getSnapshots().entrySet()) {
                copy.assign(entry.getKey(), modifyExpression(entry.getValue(), modifications));
            }
            return copy;
        }
        if (previous instanceof ArraySnapshot arraySnapshot) {
            ReferenceExpression underlyingVariable = arraySnapshot.getUnderlyingVariable() != null ? modifyExpression(arraySnapshot.getUnderlyingVariable(), modifications) : null;
            Function<ArraySnapshot, Expression> defaultValue = (snapshot) -> modifyExpression(arraySnapshot.getDefaultValue().get(), modifications);
            ArraySnapshot copy = new ArraySnapshot(defaultValue, arraySnapshot.getType(), underlyingVariable);
            modifications.put(arraySnapshot, copy);
            for (ArrayEntry.Assignment assignment : arraySnapshot.getAssignments()) {
                Expression indexExpression = modifyExpression(assignment.index(), modifications);
                Expression valueExpression = modifyExpression(assignment.value(), modifications);
                copy.assign(indexExpression, valueExpression);
            }
            return copy;
        }
        if (previous instanceof VariableSnapshot variableSnapshot) {
            ReferenceExpression underlyingVariable = variableSnapshot.getUnderlyingVariable() != null ? modifyExpression(variableSnapshot.getUnderlyingVariable(), modifications) : null;
            RapidType type = underlyingVariable != null ? underlyingVariable.getType() : variableSnapshot.getType();
            if (Objects.equals(underlyingVariable, variableSnapshot.getUnderlyingVariable()) && type.equals(variableSnapshot.getType())) {
                return variableSnapshot;
            }
            VariableSnapshot copy = new VariableSnapshot(type, underlyingVariable);
            modifications.put(variableSnapshot, copy);
            return copy;
        }
        throw new AssertionError();
    }

    private boolean isAssignedInChain(SnapshotExpression snapshotExpression) {
        DataFlowState predecessor = this;
        while (predecessor != null) {
            if (predecessor.getSnapshots().containsValue(snapshotExpression)) {
                return true;
            }
            predecessor = predecessor.getPredecessor();
        }
        return false;
    }

    public @NotNull DataFlowState merge(@NotNull DataFlowState state, @NotNull Map<ReferenceExpression, ReferenceExpression> modifications) {
        DataFlowState successorState = createSuccessorState();
        for (Expression expression : state.getExpressions()) {
            successorState.add(expression.replace(component -> {
                if (component instanceof VariableExpression) {
                    return modifications.get(component);
                }
                return modifyExpression(component, modifications);
            }));
        }
        state.getOptionality().forEach((snapshot, optionality) -> {
            SnapshotExpression expression = (SnapshotExpression) modifyExpression(snapshot, modifications);
            successorState.forceOptionality(expression, optionality);
        });
        for (Field field : getSnapshots().keySet()) {
            SnapshotExpression snapshot = getSnapshots().get(field);
            if (modifications.containsKey(snapshot)) {
                successorState.add(new BinaryExpression(BinaryOperator.EQUAL_TO, snapshot, modifications.get(snapshot)));
            }
        }
        return successorState;
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

    public void assign(@NotNull ReferenceExpression variable, @Nullable Expression expression) {
        if(expression == null) {
            createSnapshot(variable);
            return;
        }
        if (variable instanceof VariableExpression leftValue && expression instanceof ReferenceExpression rightValue) {
            SnapshotExpression snapshot = getSnapshot(rightValue);
            if (snapshot != null) {
                snapshots.put(leftValue.getField(), snapshot);
                return;
            }
        }
        expression = expression.replace(this::getSnapshot);
        Optional<SnapshotExpression> snapshot = createSnapshot(variable);
        if (snapshot.isEmpty()) {
            return;
        }
        insert(new BinaryExpression(BinaryOperator.EQUAL_TO, snapshot.orElseThrow(), expression));
    }

    public void add(@NotNull Expression expression) {
        expression = expression.replace(this::getSnapshot);
        if (expression instanceof BinaryExpression binaryExpression && binaryExpression.getOperator() == BinaryOperator.EQUAL_TO) {
            if (binaryExpression.getLeft() instanceof ReferenceExpression left && binaryExpression.getRight() instanceof ReferenceExpression right) {
                if (!(getOptionality(left).equals(getOptionality(right)))) {
                    Optionality optionality = getOptionality(left).and(getOptionality(right));
                    setOptionality(left, optionality);
                    setOptionality(right, optionality);
                }
            }
        }
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
                    List<? extends Expression> components = aggregateExpression.getExpressions();
                    if (referenceExpression.getType().getDimensions() > 0) {
                        for (int i = 0; i < components.size(); i++) {
                            Expression component = components.get(i);
                            IndexExpression indexExpression = new IndexExpression(referenceExpression, new ConstantExpression(i));
                            assign(indexExpression, component);
                        }
                        return;
                    } else if (referenceExpression.getType().getActualStructure() instanceof RapidRecord record) {
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
        Optionality result = getActualOptionality(expression);
        return Objects.requireNonNullElse(result, Optionality.PRESENT);
    }

    private @Nullable Optionality getActualOptionality(@NotNull ReferenceExpression expression) {
        SnapshotExpression snapshot = getSnapshot(expression);
        if (snapshot != null) {
            if (optionality.containsKey(snapshot)) {
                return optionality.get(snapshot);
            }
            if (predecessor != null) {
                return predecessor.getActualOptionality(snapshot);
            }
        }
        return null;
    }

    public void forceOptionality(@NotNull ReferenceExpression expression, @NotNull Optionality optionality) {
        setOptionality(expression, optionality, true);
    }

    public void setOptionality(@NotNull ReferenceExpression expression, @NotNull Optionality optionality) {
        setOptionality(expression, optionality, false);
    }

    private void setOptionality(@NotNull ReferenceExpression expression, @NotNull Optionality optionality, boolean forceOptionality) {
        Field field = getField(expression);
        if (!(field instanceof Argument argument)) {
            return;
        }
        ArgumentGroup argumentGroup = argument.getArgumentGroup(functionBlock);
        if (argumentGroup == null) {
            return;
        }
        SnapshotExpression snapshot = getSnapshot(expression);
        if (snapshot == null) {
            return;
        }
        Optionality value;
        Optionality actual;
        if (forceOptionality) {
            value = getActualOptionality(snapshot);
            if (value != null && value.and(optionality) == value) {
                return;
            }
            actual = optionality;
        } else {
            value = getOptionality(snapshot);
            if (value.and(optionality) == value) {
                return;
            }
            actual = value.and(optionality);
        }
        SnapshotExpression result = createSnapshot(expression).orElse(null);
        if (result == null) {
            return;
        }
        this.optionality.put(result, actual);
        conditions.add(new BinaryExpression(BinaryOperator.EQUAL_TO, result, snapshot));
        if (optionality == Optionality.PRESENT) {
            for (Argument otherArgument : argumentGroup.arguments()) {
                if (otherArgument == argument) {
                    continue;
                }
                setOptionality(new VariableExpression(otherArgument), Optionality.MISSING, forceOptionality);
            }
        }
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
        state.add(new BinaryExpression(BinaryOperator.EQUAL_TO, expression, new ConstantExpression(value)));
        return state.isSatisfiable();
    }

    public boolean isSatisfiable() {
        return ConditionAnalyzer.isSatisfiable(this);
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
            SnapshotExpression snapshot = getSnapshot(indexExpression.getVariable());
            if (!(snapshot instanceof ArraySnapshot arraySnapshot)) {
                return Optional.empty();
            }
            Expression indexSnapshot = getSnapshot(indexExpression.getIndex());
            IndexExpression underlyingExpression = new IndexExpression(arraySnapshot, indexSnapshot);
            SnapshotExpression variableSnapshot = createSnapshot(indexExpression.getType(), underlyingExpression);
            arraySnapshot.assign(indexSnapshot, variableSnapshot);
            return Optional.of(variableSnapshot);
        }
        if (expression instanceof ComponentExpression componentExpression) {
            SnapshotExpression snapshot = getSnapshot(componentExpression.getVariable());
            if (!(snapshot instanceof RecordSnapshot recordSnapshot)) {
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

    public @Nullable SnapshotExpression getRoot(@NotNull ReferenceExpression expression) {
        if (predecessor != null) {
            return predecessor.getSnapshot(expression);
        }
        return expression.accept(new ControlFlowVisitor<>() {
            @Override
            public SnapshotExpression visitFieldExpression(@NotNull FieldExpression expression) {
                return null;
            }

            @Override
            public SnapshotExpression visitSnapshotExpression(@NotNull SnapshotExpression snapshot) {
                return snapshot;
            }

            @Override
            public SnapshotExpression visitVariableExpression(@NotNull VariableExpression expression) {
                Field field = expression.getField();
                return roots.getOrDefault(field, null);
            }

            @Override
            public SnapshotExpression visitIndexExpression(@NotNull IndexExpression expression) {
                SnapshotExpression snapshot = getRoot(expression.getVariable());
                if (!(snapshot instanceof ArraySnapshot arraySnapshot)) {
                    return null;
                }
                Expression defaultValue = arraySnapshot.getDefaultValue().get();
                if (defaultValue instanceof SnapshotExpression snapshotExpression) {
                    return snapshotExpression;
                } else {
                    VariableSnapshot variableSnapshot = new VariableSnapshot(defaultValue.getType());
                    add(new BinaryExpression(BinaryOperator.EQUAL_TO, variableSnapshot, defaultValue));
                    return variableSnapshot;
                }
            }

            @Override
            public SnapshotExpression visitComponentExpression(@NotNull ComponentExpression expression) {
                SnapshotExpression snapshot = getSnapshot(expression.getVariable());
                if (!(snapshot instanceof RecordSnapshot recordSnapshot)) {
                    return null;
                }
                Expression defaultValue = recordSnapshot.getRoot(expression.getComponent());
                if (defaultValue instanceof SnapshotExpression snapshotExpression) {
                    return snapshotExpression;
                } else {
                    VariableSnapshot variableSnapshot = new VariableSnapshot(defaultValue.getType());
                    add(new BinaryExpression(BinaryOperator.EQUAL_TO, variableSnapshot, defaultValue));
                    return variableSnapshot;
                }
            }
        });
    }

    private @NotNull Expression getSnapshot(@NotNull Expression value) {
        if (value instanceof ReferenceExpression referenceValue) {
            SnapshotExpression snapshot = getSnapshot(referenceValue);
            if (snapshot !=null) {
                return snapshot;
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
    public @Nullable SnapshotExpression getSnapshot(@NotNull ReferenceExpression expression) {
        return expression.accept(new ControlFlowVisitor<>() {
            @Override
            public SnapshotExpression visitSnapshotExpression(@NotNull SnapshotExpression snapshot) {
                return snapshot;
            }

            @Override
            public SnapshotExpression visitVariableExpression(@NotNull VariableExpression expression) {
                Field field = expression.getField();
                if (snapshots.containsKey(field)) {
                    return snapshots.get(field);
                } else if (predecessor != null) {
                    return predecessor.getSnapshot(expression);
                } else {
                    return null;
                }
            }

            @Override
            public SnapshotExpression visitIndexExpression(@NotNull IndexExpression expression) {
                SnapshotExpression snapshot = getSnapshot(expression.getVariable());
                if (!(snapshot instanceof ArraySnapshot arraySnapshot)) {
                    return null;
                }
                Expression indexSnapshot = getSnapshot(expression.getIndex());
                List<ArrayEntry> assignments = arraySnapshot.getAssignments(DataFlowState.this, indexSnapshot);
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
                        return variableSnapshot;
                    }
                    SnapshotExpression defaultSnapshot = getSnapshot(defaultValue);
                    if (defaultSnapshot != null) {
                        arraySnapshot.assign(indexSnapshot, defaultSnapshot);
                    }
                    return defaultSnapshot;
                }
                if (arrayEntry instanceof ArrayEntry.Assignment assignment) {
                    Expression value = assignment.value();
                    if (!(value instanceof ReferenceExpression assignmentValue)) {
                        VariableSnapshot variableSnapshot = new VariableSnapshot(value.getType());
                        add(new BinaryExpression(BinaryOperator.EQUAL_TO, variableSnapshot, value));
                        return variableSnapshot;
                    }
                    return getSnapshot(assignmentValue);
                }
                throw new IllegalArgumentException();
            }

            @Override
            public SnapshotExpression visitComponentExpression(@NotNull ComponentExpression expression) {
                SnapshotExpression snapshot = getSnapshot(expression.getVariable());
                if (!(snapshot instanceof RecordSnapshot recordSnapshot)) {
                    return null;
                }
                Expression value = recordSnapshot.getValue(expression.getComponent());
                if (value instanceof ReferenceExpression variable) {
                    return getSnapshot(variable);
                } else {
                    VariableSnapshot variableSnapshot = new VariableSnapshot(value.getType());
                    add(new BinaryExpression(BinaryOperator.EQUAL_TO, variableSnapshot, value));
                    return variableSnapshot;
                }
            }
        });
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

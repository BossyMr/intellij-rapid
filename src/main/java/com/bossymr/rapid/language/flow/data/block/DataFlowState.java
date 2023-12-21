package com.bossymr.rapid.language.flow.data.block;

import com.bossymr.network.MultiMap;
import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.ConditionAnalyzer;
import com.bossymr.rapid.language.flow.data.snapshots.*;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
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

    private final @NotNull List<DataFlowState> successors = new ArrayList<>();
    private final @NotNull Block functionBlock;

    /**
     * The conditions for all variables.
     */
    private final @NotNull List<Expression> conditions = new ArrayList<>();

    /**
     * The latest snapshot of each variable. The latest snapshot for a given variable also represents the variable.
     */
    private final @NotNull Map<Field, Snapshot> snapshots = new HashMap<>();

    /**
     * The first snapshot of each variable.
     */
    private final @NotNull Map<Field, Snapshot> roots;

    private final @NotNull DataFlowBlock block;
    private @Nullable DataFlowState predecessor;

    public DataFlowState(@NotNull DataFlowBlock block, @NotNull Block functionBlock, @Nullable DataFlowState predecessor) {
        this.predecessor = predecessor;
        this.block = block;
        this.functionBlock = functionBlock;
        this.roots = predecessor == null ? new HashMap<>() : Map.of();
        block.getStates().add(this);
        if (predecessor != null) {
            predecessor.getSuccessors().add(this);
        }
    }

    public static @NotNull DataFlowState createState(@NotNull DataFlowBlock block) {
        DataFlowState state = new DataFlowState(block, block.getInstruction().getBlock(), null);
        state.initializeDefault();
        return state;
    }

    public static @NotNull DataFlowState createSuccessorState(@NotNull DataFlowBlock block, @NotNull DataFlowState predecessor) {
        return new DataFlowState(block, block.getInstruction().getBlock(), predecessor);
    }

    public static @NotNull Snapshot createDefaultSnapshot(@NotNull DataFlowState state, @NotNull RapidType type) {
        if (type.getDimensions() > 0) {
            RapidType arrayType = type.createArrayType(type.getDimensions() - 1);
            return new ArraySnapshot(type, Optionality.PRESENT, nextState -> createDefaultSnapshot(nextState, arrayType));
        }
        if (type.getRootStructure() instanceof RapidRecord) {
            return new RecordSnapshot(type, Optionality.PRESENT, DataFlowState::createDefaultSnapshot);
        }
        VariableSnapshot snapshot = new VariableSnapshot(type, Optionality.PRESENT);
        Object object = getDefaultValue(type);
        if (object != null) {
            state.add(new BinaryExpression(BinaryOperator.EQUAL_TO, new SnapshotExpression(snapshot), new LiteralExpression(object)));
        }
        return snapshot;
    }

    private static @Nullable Object getDefaultValue(@NotNull RapidType type) {
        if (type.isAssignable(RapidPrimitiveType.BOOLEAN)) {
            return false;
        } else if (type.isAssignable(RapidPrimitiveType.STRING)) {
            return "";
        } else if (type.isAssignable(RapidPrimitiveType.NUMBER) || type.isAssignable(RapidPrimitiveType.DOUBLE)) {
            return 0;
        }
        return null;
    }

    public @NotNull DataFlowState createCompactState(@NotNull Set<Snapshot> targets) {
        Set<Snapshot> snapshots = new HashSet<>(targets);
        DataFlowState successor = new DataFlowState(block, functionBlock, null) {
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                DataFlowState flowState = (DataFlowState) o;
                return Objects.equals(getFunctionBlock(), flowState.getFunctionBlock()) && Objects.equals(getExpressions(), flowState.getExpressions()) && Objects.equals(getSnapshots(), flowState.getSnapshots()) && Objects.equals(getRoots(), flowState.getRoots());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getFunctionBlock(), getExpressions(), getSnapshots(), getRoots());
            }
        };
        block.getStates().remove(successor);
        MultiMap<DataFlowState, Expression> expressions = new MultiMap<>(HashSet::new);
        List<DataFlowState> states = new ArrayList<>();
        for (DataFlowState predecessor = this; predecessor != null; predecessor = predecessor.getPredecessor()) {
            states.add(predecessor);
            List<Expression> result = getAllExpressions(predecessor, snapshots);
            expressions.putAll(predecessor, result);
            successor.getExpressions().addAll(result);
            DataFlowState state = predecessor;
            if (snapshots.stream().allMatch(snapshot -> state.getSnapshots().containsValue(snapshot))) {
                break;
            }
        }
        for (int i = states.size() - 1; i >= 0; i--) {
            DataFlowState predecessor = states.get(i);
            successor.getSnapshots().putAll(predecessor.getSnapshots());
            List<Expression> result = getAllExpressions(predecessor, snapshots);
            result.removeAll(expressions.getAll(predecessor));
            successor.getExpressions().addAll(result);
        }
        successor.getSnapshots().values().removeIf(snapshot -> !(snapshots.contains(snapshot)));
        successor.getRoots().putAll(states.get(states.size() - 1).getRoots());
        return successor;
    }

    public @NotNull Block getFunctionBlock() {
        return functionBlock;
    }

    private @NotNull List<Expression> getAllExpressions(@NotNull DataFlowState state, @NotNull Set<Snapshot> variables) {
        MultiMap<Snapshot, Expression> result = new MultiMap<>(HashSet::new);
        MultiMap<Expression, Snapshot> dependency = new MultiMap<>(HashSet::new);
        for (Expression expression : state.getExpressions()) {
            expression.iterate(expr -> {
                if (expr instanceof SnapshotExpression variable) {
                    result.put(variable.getSnapshot(), expression);
                    dependency.put(expression, variable.getSnapshot());
                }
                return false;
            });
        }
        List<Expression> expressions = new ArrayList<>();
        Deque<Snapshot> workList = new ArrayDeque<>(variables);
        while (!(workList.isEmpty())) {
            Snapshot key = workList.removeLast();
            Collection<Expression> collection = result.getAll(key);
            for (Expression expression : collection) {
                for (Snapshot dependent : dependency.getAll(expression)) {
                    if (variables.add(dependent)) {
                        workList.add(dependent);
                    }
                }
            }
            for (Expression expression : collection) {
                if (!(expressions.contains(expression))) {
                    expressions.add(expression);
                }
            }
        }
        return expressions;
    }

    public void close() {
        if (predecessor != null) {
            predecessor.getSuccessors().remove(this);
        }
        block.getStates().remove(this);
        predecessor = null;
    }

    public @Nullable DataFlowState getPredecessor() {
        return predecessor;
    }

    public @NotNull List<DataFlowState> getSuccessors() {
        return successors;
    }

    public @Nullable DataFlowState getFirstPredecessor() {
        if (predecessor == null) {
            return null;
        }
        DataFlowState state = predecessor;
        while (true) {
            if (state.predecessor == null) {
                return state;
            }
            state = state.predecessor;
        }
    }

    private void initializeDefault() {
        for (Variable variable : functionBlock.getVariables()) {
            snapshots.put(variable, createDefaultSnapshot(this, variable.getType()));
        }
        initializeArguments();
        for (Field field : snapshots.keySet()) {
            roots.put(field, snapshots.get(field));
        }
    }

    private void initializeArguments() {
        for (ArgumentGroup argumentGroup : functionBlock.getArgumentGroups()) {
            for (Argument argument : argumentGroup.arguments()) {
                if (argumentGroup.isOptional()) {
                    snapshots.put(argument, Snapshot.createSnapshot(argument.getType(), Optionality.UNKNOWN));
                } else {
                    if (argument.getParameterType() == ParameterType.REFERENCE) {
                        snapshots.put(argument, Snapshot.createSnapshot(argument.getType(), Optionality.UNKNOWN));
                    } else {
                        snapshots.put(argument, Snapshot.createSnapshot(argument.getType()));
                    }
                }
            }
        }
    }

    public @NotNull DataFlowBlock getBlock() {
        return block;
    }

    public @NotNull DataFlowState merge(@NotNull DataFlowState state, @NotNull Map<Snapshot, Snapshot> modifications) {
        DataFlowState successorState = DataFlowState.createSuccessorState(getBlock(), this);
        for (Expression expression : state.getExpressions()) {
            successorState.add(expression.replace(component -> {
                if (!(component instanceof SnapshotExpression snapshot)) {
                    return component;
                }
                return new SnapshotExpression(modifications.getOrDefault(snapshot.getSnapshot(), snapshot.getSnapshot()));
            }));
        }
        return successorState;
    }

    public void assign(@NotNull ReferenceExpression variable, @Nullable Expression expression) {
        if (expression == null) {
            createSnapshot(variable);
            return;
        }
        expression = expression.replace(this::getSnapshot);
        Optionality optionality = getQuickOptionality(expression);
        SnapshotExpression snapshot = createSnapshot(variable, optionality);
        insert(new BinaryExpression(BinaryOperator.EQUAL_TO, snapshot, expression));
        if (optionality == Optionality.UNKNOWN) {
            insert(new BinaryExpression(BinaryOperator.EQUAL_TO, new UnaryExpression(UnaryOperator.PRESENT, snapshot), new UnaryExpression(UnaryOperator.PRESENT, expression)));
        }
    }

    private @NotNull Optionality getQuickOptionality(@NotNull Expression expression) {
        if (expression instanceof SnapshotExpression snapshot) {
            return snapshot.getSnapshot().getOptionality();
        }
        return Optionality.PRESENT;
    }

    public void add(@NotNull Expression expression) {
        expression = expression.replace(this::getSnapshot);
        insert(expression);
    }

    private void insert(@NotNull Expression expression) {
        if (!(expression.getType().isAssignable(RapidPrimitiveType.BOOLEAN))) {
            throw new IllegalArgumentException("Cannot add expression: " + expression);
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
                            IndexExpression indexExpression = new IndexExpression(referenceExpression, new LiteralExpression(i + 1));
                            assign(indexExpression, component);
                        }
                        return;
                    } else if (referenceExpression.getType().getRootStructure() instanceof RapidRecord record) {
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
        if (expression.getComponents().stream().anyMatch(expr -> expr instanceof IndexExpression || expr instanceof ComponentExpression)) {
            throw new IllegalArgumentException("Cannot add expression: " + expression);
        }
        conditions.add(expression);
    }

    public @NotNull List<Expression> getExpressions() {
        return conditions;
    }

    public @NotNull Map<Field, Snapshot> getSnapshots() {
        return snapshots;
    }

    public @NotNull Map<Field, Snapshot> getRoots() {
        return roots;
    }

    public @NotNull BooleanValue getConstraint(@NotNull Expression expression) {
        if (!(expression.getType().isAssignable(RapidPrimitiveType.BOOLEAN))) {
            throw new IllegalArgumentException("Cannot calculate constraint for expression: " + expression);
        }
        expression = expression.replace(this::getSnapshot);
        return ConditionAnalyzer.getBooleanValue(this, expression);
    }

    public boolean isSatisfiable(@NotNull Set<Snapshot> targets) {
        return ConditionAnalyzer.isSatisfiable(this, targets);
    }

    public @NotNull Optionality getOptionality(@NotNull ReferenceExpression variable) {
        SnapshotExpression snapshot = getSnapshot(variable);
        Optionality optionality = snapshot.getSnapshot().getOptionality();
        if (optionality != Optionality.UNKNOWN) {
            return optionality;
        }
        return ConditionAnalyzer.getOptionality(this, snapshot);
    }

    public @NotNull SnapshotExpression createSnapshot(@NotNull Expression expression) {
        return createSnapshot(expression, Optionality.PRESENT);
    }

    public @NotNull SnapshotExpression createSnapshot(@NotNull Expression variable, @NotNull Optionality optionality) {
        if (variable instanceof SnapshotExpression expression) {
            return new SnapshotExpression(expression.getSnapshot());
        }
        if (variable instanceof IndexExpression indexExpression) {
            SnapshotExpression variableSnapshot = getSnapshot(indexExpression.getVariable());
            if (!(variableSnapshot.getSnapshot() instanceof ArraySnapshot arraySnapshot)) {
                throw new IllegalArgumentException();
            }
            Expression indexSnapshot = getSnapshot(indexExpression.getIndex());
            Snapshot snapshot = Snapshot.createSnapshot(indexExpression.getType());
            arraySnapshot.assign(indexSnapshot, snapshot);
            return new SnapshotExpression(snapshot, indexExpression);
        }
        if (variable instanceof ComponentExpression componentExpression) {
            SnapshotExpression variableSnapshot = getSnapshot(componentExpression.getVariable());
            if (!(variableSnapshot.getSnapshot() instanceof RecordSnapshot recordSnapshot)) {
                throw new IllegalArgumentException();
            }
            Snapshot snapshot = Snapshot.createSnapshot(componentExpression.getType());
            recordSnapshot.assign(componentExpression.getComponent(), snapshot);
            return new SnapshotExpression(snapshot, componentExpression);
        }
        Snapshot snapshot = Snapshot.createSnapshot(variable.getType(), optionality);
        if (variable instanceof VariableExpression variableExpression) {
            snapshots.put(variableExpression.getField(), snapshot);
        }
        return new SnapshotExpression(snapshot, variable);
    }

    public @Nullable Snapshot getSnapshot(@NotNull RapidExpression expression) {
        for (Expression condition : conditions) {
            for (Expression component : condition.getComponents()) {
                RapidExpression element = component.getElement();
                if (element == null) {
                    continue;
                }
                if (element.isEquivalentTo(expression)) {
                    if (component instanceof SnapshotExpression reference) {
                        return reference.getSnapshot();
                    }
                    Snapshot snapshot = Snapshot.createSnapshot(component.getType());
                    add(new BinaryExpression(BinaryOperator.EQUAL_TO, new SnapshotExpression(snapshot), component));
                    return snapshot;
                }
            }
        }
        if (predecessor != null) {
            if (predecessor.getBlock().equals(getBlock())) {
                return predecessor.getSnapshot(expression);
            }
        }
        return null;
    }

    private @NotNull Expression getSnapshot(@NotNull Expression value) {
        if (value instanceof ReferenceExpression referenceValue) {
            return getSnapshot(referenceValue);
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
    public @NotNull SnapshotExpression getSnapshot(@NotNull ReferenceExpression expression) {
        return expression.accept(new ControlFlowVisitor<>() {
            private static @NotNull Expression getExpressionChain(@NotNull List<Expression> expressions) {
                Expression chain = new LiteralExpression(false);
                for (int i = 0; i < expressions.size(); i++) {
                    Expression instance = new LiteralExpression(true);
                    for (int j = 0; j < expressions.size(); j++) {
                        instance = new BinaryExpression(BinaryOperator.AND, instance, new BinaryExpression(BinaryOperator.EQUAL_TO, expressions.get(j), new LiteralExpression(j == i)));
                    }
                    chain = new BinaryExpression(BinaryOperator.OR, chain, instance);
                }
                return chain;
            }

            @Override
            public @NotNull SnapshotExpression visitSnapshotExpression(@NotNull SnapshotExpression snapshot) {
                return snapshot;
            }

            @Override
            public @NotNull SnapshotExpression visitVariableExpression(@NotNull VariableExpression expression) {
                Field field = expression.getField();
                if (snapshots.containsKey(field)) {
                    return new SnapshotExpression(snapshots.get(field), expression);
                } else if (predecessor != null) {
                    return predecessor.getSnapshot(expression);
                } else {
                    throw new IllegalArgumentException("Cannot find snapshot for: " + expression);
                }
            }

            @Override
            public @NotNull SnapshotExpression visitIndexExpression(@NotNull IndexExpression expression) {
                ReferenceExpression variable = expression.getVariable();
                SnapshotExpression snapshot = getSnapshot(variable);
                if (!(snapshot.getSnapshot() instanceof ArraySnapshot arraySnapshot)) {
                    throw new IllegalArgumentException("Unexpected snapshot: " + snapshot + " for variable of type: " + variable.getType());
                }
                Expression index = getSnapshot(expression.getIndex());
                List<ArrayEntry> assignments = arraySnapshot.getAssignments(DataFlowState.this, index);
                if (assignments.isEmpty()) {
                    throw new IllegalArgumentException();
                }
                if (assignments.size() == 1) {
                    ArrayEntry entry = assignments.get(0);
                    if (entry instanceof ArrayEntry.DefaultValue defaultAssignment) {
                        Snapshot value = defaultAssignment.snapshot();
                        return new SnapshotExpression(value, expression);
                    }
                    if (entry instanceof ArrayEntry.Assignment assignment) {
                        Snapshot value = assignment.snapshot();
                        return new SnapshotExpression(value, expression);
                    }
                }
                SnapshotExpression result = new SnapshotExpression(Snapshot.createSnapshot(expression.getType()), expression);
                List<Expression> expressions = new ArrayList<>();
                for (int i = 0; i < assignments.size(); i++) {
                    ArrayEntry entry = assignments.get(i);
                    Snapshot value = entry.snapshot();
                    Expression expr = new BinaryExpression(BinaryOperator.EQUAL_TO, result, new SnapshotExpression(value));
                    if (entry instanceof ArrayEntry.Assignment assignment) {
                        expr = new BinaryExpression(BinaryOperator.AND, expr, new BinaryExpression(BinaryOperator.EQUAL_TO, index, getSnapshot(assignment.index())));
                    }
                    for (int j = 0; j < i; j++) {
                        if (entry instanceof ArrayEntry.Assignment assignment) {
                            expr = new BinaryExpression(BinaryOperator.AND, expr, new BinaryExpression(BinaryOperator.NOT_EQUAL_TO, index, getSnapshot(assignment.index())));
                        }
                    }
                    expressions.add(expr);
                }
                Expression chain = getExpressionChain(expressions);
                add(chain);
                return result;
            }

            @Override
            public @NotNull SnapshotExpression visitComponentExpression(@NotNull ComponentExpression expression) {
                ReferenceExpression variable = expression.getVariable();
                SnapshotExpression snapshot = getSnapshot(variable);
                if (!(snapshot.getSnapshot() instanceof RecordSnapshot recordSnapshot)) {
                    throw new IllegalArgumentException("Unexpected snapshot: " + snapshot + " for variable of type: " + variable.getType());
                }
                Snapshot value = recordSnapshot.getSnapshot(DataFlowState.this, expression.getComponent());
                return new SnapshotExpression(value, expression);
            }
        });
    }

    @Override
    public String toString() {
        return "DataFlowState{" +
                "name=" + functionBlock.getModuleName() + ":" + functionBlock.getName() +
                ", conditions=" + conditions.size() +
                '}';
    }
}

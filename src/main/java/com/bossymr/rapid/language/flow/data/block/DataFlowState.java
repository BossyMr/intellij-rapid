package com.bossymr.rapid.language.flow.data.block;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.ConditionAnalyzer;
import com.bossymr.rapid.language.flow.data.snapshots.ArrayEntry;
import com.bossymr.rapid.language.flow.data.snapshots.ArraySnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.RecordSnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
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

    private final @NotNull List<DataFlowState> successors = new ArrayList<>();
    private final @NotNull Block functionBlock;
    /**
     * The conditions for all variables.
     */
    private final @NotNull List<Expression> conditions = new ArrayList<>();
    /**
     * The latest snapshot of each variable. The latest snapshot for a given variable also represents the variable.
     */
    private final @NotNull Map<Field, SnapshotExpression> snapshots = new HashMap<>();
    /**
     * The first snapshot of each variable.
     */
    private final @NotNull Map<Field, SnapshotExpression> roots;
    private final @NotNull DataFlowBlock block;
    private @Nullable DataFlowState predecessor;

    private DataFlowState(@NotNull DataFlowBlock block, @NotNull Block functionBlock, @Nullable DataFlowState predecessor) {
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

    public @NotNull DataFlowState createCompactState() {
        DataFlowState successor = new DataFlowState(block, functionBlock, null);
        block.getStates().remove(successor);
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
        }
        return successor;
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
                if (argumentGroup.isOptional()) {
                    createSnapshot(new VariableExpression(argument), Optionality.UNKNOWN);
                } else {
                    if (argument.getParameterType() == ParameterType.REFERENCE) {
                        createSnapshot(new VariableExpression(argument), Optionality.UNKNOWN);
                    } else {
                        createSnapshot(new VariableExpression(argument));
                    }
                }
            }
        }
    }

    public @NotNull SnapshotExpression createDefaultSnapshot(@NotNull ReferenceExpression variable) {
        RapidType type = variable.getType();
        if (type.getDimensions() > 0) {
            ArraySnapshot snapshot = new ArraySnapshot(variable.getType(), Optionality.PRESENT, (parent) -> createDefaultSnapshot(new IndexExpression(parent, new VariableSnapshot(RapidPrimitiveType.NUMBER, Optionality.PRESENT))));
            add(new BinaryExpression(BinaryOperator.EQUAL_TO, new UnaryExpression(UnaryOperator.PRESENT, snapshot), new LiteralExpression(true)));
            return snapshot;
        } else if (type.getRootStructure() instanceof RapidRecord record) {
            RecordSnapshot snapshot = new RecordSnapshot(variable.getType(), Optionality.PRESENT);
            add(new BinaryExpression(BinaryOperator.EQUAL_TO, new UnaryExpression(UnaryOperator.PRESENT, snapshot), new LiteralExpression(true)));
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
            VariableSnapshot snapshot = new VariableSnapshot(Optionality.PRESENT, variable);
            add(new BinaryExpression(BinaryOperator.EQUAL_TO, new UnaryExpression(UnaryOperator.PRESENT, snapshot), new LiteralExpression(true)));
            Object object = null;
            if (type.isAssignable(RapidPrimitiveType.BOOLEAN)) {
                object = false;
            } else if (type.isAssignable(RapidPrimitiveType.STRING)) {
                object = "";
            } else if (type.isAssignable(RapidPrimitiveType.NUMBER) || type.isAssignable(RapidPrimitiveType.DOUBLE)) {
                object = 0;
            }
            if (object != null) {
                add(new BinaryExpression(BinaryOperator.EQUAL_TO, snapshot, new LiteralExpression(object)));
            }
            return snapshot;
        }
    }

    public @NotNull DataFlowBlock getBlock() {
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
            return previous;
        }
        if (isAssignedInChain(snapshotExpression)) {
            return snapshotExpression;
        }
        if (snapshotExpression instanceof RecordSnapshot recordSnapshot) {
            ReferenceExpression underlyingVariable = recordSnapshot.getUnderlyingVariable() != null ? modifyExpression(recordSnapshot.getUnderlyingVariable(), modifications) : null;
            RecordSnapshot copy = new RecordSnapshot(recordSnapshot.getType(), recordSnapshot.getOptionality());
            modifications.put(recordSnapshot, copy);
            for (Map.Entry<String, Expression> entry : recordSnapshot.getSnapshots().entrySet()) {
                copy.assign(entry.getKey(), modifyExpression(entry.getValue(), modifications));
            }
            return copy;
        }
        if (previous instanceof ArraySnapshot arraySnapshot) {
            ReferenceExpression underlyingVariable = arraySnapshot.getUnderlyingVariable() != null ? modifyExpression(arraySnapshot.getUnderlyingVariable(), modifications) : null;
            Function<ArraySnapshot, Expression> defaultValue = (snapshot) -> modifyExpression(arraySnapshot.getDefaultValue().get(), modifications);
            ArraySnapshot copy = new ArraySnapshot(arraySnapshot.getType(), arraySnapshot.getOptionality(), defaultValue);
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
            VariableSnapshot copy = new VariableSnapshot(type, variableSnapshot.getOptionality());
            modifications.put(variableSnapshot, copy);
            return copy;
        }
        return previous;
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
        DataFlowState successorState = DataFlowState.createSuccessorState(getBlock(), this);
        for (Expression expression : state.getExpressions()) {
            successorState.add(expression.replace(component -> {
                if (component instanceof VariableExpression) {
                    return modifications.get(component);
                }
                return modifyExpression(component, modifications);
            }));
        }
        for (Field field : getSnapshots().keySet()) {
            SnapshotExpression snapshot = getSnapshots().get(field);
            if (modifications.containsKey(snapshot)) {
                successorState.add(new BinaryExpression(BinaryOperator.EQUAL_TO, snapshot, modifications.get(snapshot)));
            }
        }
        return successorState;
    }

    public void assign(@NotNull ReferenceExpression variable, @Nullable Expression expression) {
        if (expression == null) {
            createSnapshot(variable);
            return;
        }
        expression = expression.replace(this::getSnapshot);
        if (expression instanceof LiteralExpression) {
            if (variable instanceof IndexExpression index) {
                SnapshotExpression snapshot = getSnapshot(index.getVariable());
                if (snapshot instanceof ArraySnapshot arraySnapshot) {
                    arraySnapshot.assign(getSnapshot(index.getIndex()), expression);
                    return;
                }
            }
            if (variable instanceof ComponentExpression component) {
                SnapshotExpression snapshot = getSnapshot(component.getVariable());
                if (snapshot instanceof RecordSnapshot recordSnapshot) {
                    recordSnapshot.assign(component.getComponent(), expression);
                    return;
                }
            }
        }
        SnapshotExpression snapshot = createSnapshot(variable, Optionality.UNKNOWN);
        insert(new BinaryExpression(BinaryOperator.EQUAL_TO, snapshot, expression));
        insert(new BinaryExpression(BinaryOperator.EQUAL_TO, new UnaryExpression(UnaryOperator.PRESENT, snapshot), new UnaryExpression(UnaryOperator.PRESENT, expression)));
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

    public @NotNull Map<Field, SnapshotExpression> getSnapshots() {
        return snapshots;
    }

    public @NotNull Map<Field, SnapshotExpression> getRoots() {
        return roots;
    }

    public @NotNull BooleanValue getConstraint(@NotNull Expression expression) {
        if (!(expression.getType().isAssignable(RapidPrimitiveType.BOOLEAN))) {
            throw new IllegalArgumentException("Cannot calculate constraint for expression: " + expression);
        }
        expression = expression.replace(this::getSnapshot);
        return ConditionAnalyzer.getBooleanValue(this, expression);
    }

    public boolean isSatisfiable(@NotNull Set<ReferenceExpression> targets) {
        return ConditionAnalyzer.isSatisfiable(this, targets);
    }

    public @NotNull Optionality getOptionality(@NotNull ReferenceExpression variable) {
        SnapshotExpression snapshot = getSnapshot(variable);
        variable = snapshot != null ? snapshot : variable;
        return ConditionAnalyzer.getOptionality(this, variable);
    }

    public void createSnapshot(@NotNull ReferenceExpression expression) {
        createSnapshot(expression, Optionality.PRESENT);
    }

    public @NotNull SnapshotExpression createSnapshot(@NotNull ReferenceExpression variable, @NotNull Optionality optionality) {
        if (variable instanceof SnapshotExpression snapshot) {
            /*
             * If the specified variable is a snapshot, try to create a snapshot for the underlying variable.
             */
            variable = snapshot.getUnderlyingVariable();
            if (variable == null) {
                return createSnapshot(snapshot.getType(), null);
            }
        }
        if (variable instanceof IndexExpression indexExpression) {
            SnapshotExpression snapshot = getSnapshot(indexExpression.getVariable());
            if (!(snapshot instanceof ArraySnapshot arraySnapshot)) {
                throw new IllegalArgumentException();
            }
            Expression indexSnapshot = getSnapshot(indexExpression.getIndex());
            IndexExpression underlyingExpression = new IndexExpression(arraySnapshot, indexSnapshot);
            SnapshotExpression variableSnapshot = createSnapshot(indexExpression.getType(), underlyingExpression);
            arraySnapshot.assign(indexSnapshot, variableSnapshot);
            return variableSnapshot;
        }
        if (variable instanceof ComponentExpression componentExpression) {
            SnapshotExpression snapshot = getSnapshot(componentExpression.getVariable());
            if (!(snapshot instanceof RecordSnapshot recordSnapshot)) {
                throw new IllegalArgumentException();
            }
            ComponentExpression underlyingExpression = new ComponentExpression(componentExpression.getType(), recordSnapshot, componentExpression.getComponent());
            SnapshotExpression variableSnapshot = createSnapshot(componentExpression.getType(), underlyingExpression);
            recordSnapshot.assign(componentExpression.getComponent(), variableSnapshot);
            return variableSnapshot;
        }
        return createSnapshot(variable.getType(), optionality, variable);
    }

    public @NotNull SnapshotExpression createSnapshot(@NotNull RapidType snapshotType, @Nullable ReferenceExpression underlyingExpression) {
        return createSnapshot(snapshotType, Optionality.PRESENT, underlyingExpression);
    }

    public @NotNull SnapshotExpression createSnapshot(@NotNull RapidType snapshotType, @NotNull Optionality optionality, @Nullable ReferenceExpression underlyingExpression) {
        SnapshotExpression snapshot = SnapshotExpression.createSnapshot(snapshotType, optionality);
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
                    SnapshotExpression variableSnapshot = SnapshotExpression.createSnapshot(defaultValue.getType());
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
                    SnapshotExpression variableSnapshot = SnapshotExpression.createSnapshot(defaultValue.getType());
                    add(new BinaryExpression(BinaryOperator.EQUAL_TO, variableSnapshot, defaultValue));
                    return variableSnapshot;
                }
            }
        });
    }

    public @Nullable SnapshotExpression getSnapshot(@NotNull RapidExpression expression) {
        for (SnapshotExpression value : snapshots.values()) {
            RapidExpression element = value.getElement();
            if (element == null) {
                continue;
            }
            if (element.isEquivalentTo(expression)) {
                return value;
            }
        }
        for (Expression condition : conditions) {
            for (Expression component : condition.getComponents()) {
                RapidExpression element = component.getElement();
                if (element == null) {
                    continue;
                }
                if (element.isEquivalentTo(expression)) {
                    if (component instanceof SnapshotExpression reference) {
                        return reference;
                    }
                    SnapshotExpression reference = SnapshotExpression.createSnapshot(component.getType());
                    add(new BinaryExpression(BinaryOperator.EQUAL_TO, reference, component));
                    return reference;
                }
            }
        }
        return null;
    }

    private @NotNull Expression getSnapshot(@NotNull Expression value) {
        if (value instanceof ReferenceExpression referenceValue) {
            SnapshotExpression snapshot = getSnapshot(referenceValue);
            if (snapshot != null) {
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
                    throw new IllegalArgumentException("Cannot find snapshot for: " + expression);
                }
            }

            @Override
            public SnapshotExpression visitIndexExpression(@NotNull IndexExpression expression) {
                ReferenceExpression variable = expression.getVariable();
                SnapshotExpression snapshot = getSnapshot(variable);
                if (!(snapshot instanceof ArraySnapshot arraySnapshot)) {
                    throw new IllegalArgumentException("Unexpected snapshot: " + snapshot + " for variable of type: " + variable.getType());
                }
                Expression indexSnapshot = getSnapshot(expression.getIndex());
                List<ArrayEntry> assignments = arraySnapshot.getAssignments(DataFlowState.this, indexSnapshot);
                if (assignments.size() == 1) {
                    ArrayEntry entry = assignments.get(0);
                    if (entry instanceof ArrayEntry.DefaultValue defaultAssignment) {
                        Expression value = defaultAssignment.defaultValue();
                        if (!(value instanceof ReferenceExpression defaultValue)) {
                            SnapshotExpression variableSnapshot = createSnapshot(value.getType(), null);
                            add(new BinaryExpression(BinaryOperator.EQUAL_TO, variableSnapshot, value));
                            return variableSnapshot;
                        }
                        SnapshotExpression defaultSnapshot = getSnapshot(defaultValue);
                        if (defaultSnapshot != null) {
                            arraySnapshot.assign(indexSnapshot, defaultSnapshot);
                        }
                        return defaultSnapshot;
                    }
                    if (entry instanceof ArrayEntry.Assignment assignment) {
                        Expression value = assignment.value();
                        if (!(value instanceof ReferenceExpression assignmentValue)) {
                            SnapshotExpression variableSnapshot = createSnapshot(value.getType(), null);
                            add(new BinaryExpression(BinaryOperator.EQUAL_TO, variableSnapshot, value));
                            return variableSnapshot;
                        }
                        return getSnapshot(assignmentValue);
                    }
                }
                SnapshotExpression result = createSnapshot(expression.getType(), null);
                for (int i = 0; i < assignments.size(); i++) {
                    ArrayEntry entry = assignments.get(i);
                    Expression expr;
                    BinaryExpression resultEquality = new BinaryExpression(BinaryOperator.EQUAL_TO, result, entry.getValue());
                    if (entry instanceof ArrayEntry.Assignment assignment) {
                        expr = new BinaryExpression(BinaryOperator.EQUAL_TO, indexSnapshot, getSnapshot(assignment.index()));
                        expr = new BinaryExpression(BinaryOperator.AND, expr, resultEquality);
                    } else {
                        expr = resultEquality;
                    }
                    for (int j = 0; j < i; j++) {
                        if (entry instanceof ArrayEntry.Assignment assignment) {
                            expr = new BinaryExpression(BinaryOperator.AND, expr, new BinaryExpression(BinaryOperator.NOT_EQUAL_TO, getSnapshot(assignment.index()), indexSnapshot));
                        }
                    }
                }
                return result;
            }

            @Override
            public SnapshotExpression visitComponentExpression(@NotNull ComponentExpression expression) {
                ReferenceExpression variable = expression.getVariable();
                SnapshotExpression snapshot = getSnapshot(variable);
                if (!(snapshot instanceof RecordSnapshot recordSnapshot)) {
                    throw new IllegalArgumentException("Unexpected snapshot: " + snapshot + " for variable of type: " + variable.getType());
                }
                Expression value = recordSnapshot.getValue(expression.getComponent());
                if (value instanceof ReferenceExpression referenceExpression) {
                    return getSnapshot(referenceExpression);
                } else {
                    SnapshotExpression variableSnapshot = SnapshotExpression.createSnapshot(value.getType());
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
                '}';
    }
}

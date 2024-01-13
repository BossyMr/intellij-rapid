package com.bossymr.rapid.language.flow.data;

import com.bossymr.network.MultiMap;
import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.snapshots.ArraySnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.RecordSnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.Snapshot;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.expression.*;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidLiteralExpression;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.type.RapidArrayType;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * A {@code DataFlowState} represents the state of the program at a specific point. Together, states form the data flow
 * graph of a method.
 */
public class DataFlowState {

    private final @NotNull List<DataFlowState> successors = new ArrayList<>();
    private final @NotNull Instruction instruction;
    private final @NotNull Block functionBlock;
    private final @NotNull ControlFlowBlock block;

    private final @NotNull Set<Expression> conditions = new HashSet<>();

    /**
     * The latest snapshot of each variable. The latest snapshot for a given variable also represents the variable.
     */
    private final @NotNull Map<Field, Snapshot> snapshots = new HashMap<>();

    /**
     * The first snapshot of each variable.
     */
    private final @NotNull Map<Field, Snapshot> roots;
    private @Nullable DataFlowState predecessor;

    public DataFlowState(@NotNull ControlFlowBlock block, @NotNull Instruction instruction, @Nullable DataFlowState predecessor) {
        this.predecessor = predecessor;
        this.block = block;
        this.functionBlock = instruction.getBlock();
        this.instruction = instruction;
        this.roots = predecessor == null ? new HashMap<>() : Map.of();
        if (predecessor != null) {
            predecessor.getSuccessors().add(this);
        }
    }

    public static @NotNull DataFlowState createState(@NotNull ControlFlowBlock block, @NotNull Instruction instruction) {
        DataFlowState state = new DataFlowState(block, instruction, null);
        state.initializeDefault();
        return state;
    }

    public static @NotNull DataFlowState createSuccessorState(@NotNull Instruction instruction, @NotNull DataFlowState predecessor) {
        return new DataFlowState(predecessor.getBlock(), instruction, predecessor);
    }

    public @NotNull ControlFlowBlock getBlock() {
        return block;
    }

    private @NotNull Snapshot createDefaultSnapshot(@NotNull RapidType type, @Nullable Snapshot parent) {
        if (type.isArray()) {
            ArraySnapshot snapshot = new ArraySnapshot(parent, type, Optionality.PRESENT);
            ArraySnapshot copy = snapshot.copy();
            Snapshot defaultSnapshot = createDefaultSnapshot(type.createArrayType(type.getDimensions() - 1), snapshot);
            add(new BinaryExpression(BinaryOperator.EQUAL_TO, new SnapshotExpression(copy), FunctionCallExpression.constant(new SnapshotExpression(snapshot), new SnapshotExpression(defaultSnapshot))));
            RapidType arrayType = type;
            for (int i = 0; i < 3; i++) {
                Expression value = getLength(arrayType);
                if (value == null) {
                    break;
                }
                arrayType = ((RapidArrayType) arrayType).getUnderlyingType();
                add(new BinaryExpression(BinaryOperator.EQUAL_TO, FunctionCallExpression.length(new SnapshotExpression(copy), new LiteralExpression(i + 1)), value));
            }
            return copy;
        }
        if (type.isRecord()) {
            RecordSnapshot snapshot = new RecordSnapshot(parent, type, Optionality.PRESENT);
            Map<String, RapidType> components = getComponents(snapshot);
            for (String componentName : components.keySet()) {
                RapidType componentType = components.get(componentName);
                Snapshot defaultSnapshot = createDefaultSnapshot(componentType, snapshot);
                FunctionCallExpression select = FunctionCallExpression.select(new SnapshotExpression(snapshot), componentName);
                add(new BinaryExpression(BinaryOperator.EQUAL_TO, select, new SnapshotExpression(defaultSnapshot)));
            }
            return snapshot;
        }
        VariableSnapshot snapshot = new VariableSnapshot(parent, type, Optionality.PRESENT);
        Object defaultValue = getDefaultValue(type);
        if (defaultValue != null) {
            add(new BinaryExpression(BinaryOperator.EQUAL_TO, new SnapshotExpression(snapshot), new LiteralExpression(defaultValue)));
        }
        return snapshot;
    }

    private @NotNull Map<String, RapidType> getComponents(@NotNull RecordSnapshot snapshot) {
        if (!(snapshot.getType().getRootStructure() instanceof RapidRecord record)) {
            throw new IllegalArgumentException();
        }
        Map<String, RapidType> components = new HashMap<>();
        for (RapidComponent component : record.getComponents()) {
            String componentName = component.getName();
            RapidType componentType = component.getType();
            if (componentName == null) {
                continue;
            }
            components.put(componentName, Objects.requireNonNullElse(componentType, RapidPrimitiveType.ANYTYPE));
        }
        return components;
    }

    private @Nullable Object getDefaultValue(@NotNull RapidType type) {
        if (type.isAssignable(RapidPrimitiveType.BOOLEAN)) {
            return false;
        } else if (type.isAssignable(RapidPrimitiveType.STRING)) {
            return "";
        } else if (type.isAssignable(RapidPrimitiveType.NUMBER) || type.isAssignable(RapidPrimitiveType.DOUBLE)) {
            return 0;
        }
        return null;
    }

    private @Nullable Expression getLength(@NotNull RapidType type) {
        if (!(type instanceof RapidArrayType arrayType)) {
            return null;
        }
        if (!(arrayType.getLength() instanceof RapidLiteralExpression expression)) {
            return null;
        }
        if (!(expression.getValue() instanceof Number value)) {
            return null;
        }
        return new LiteralExpression(value);
    }

    public @NotNull DataFlowState createSuccessorState() {
        return DataFlowState.createSuccessorState(getInstruction(), this);
    }

    /**
     * Creates a new {@code DataFlowState} which contains all conditions dependent one of the specified targets, which
     * are registered in either this state or in one of its predecessors. All snapshots and roots to variables dependent
     * on one the specified targets are also added to the state.
     * <p>
     * If a condition dependent on one of the specified targets is found, all other variables which the condition
     * depends on are added to the set of targets.
     *
     * @param snapshots the targets.
     * @return a new state, with no predecessors or successors.
     */
    public @NotNull DataFlowState createCompactState(@NotNull Set<Snapshot> snapshots) {
        DataFlowState compactState = new DataFlowState(getBlock(), getInstruction(), null);
        Map<DataFlowState, MultiMap<Expression, Snapshot>> dependencies = new HashMap<>();
        Map<DataFlowState, MultiMap<Snapshot, Expression>> dependents = new HashMap<>();
        List<DataFlowState> chain = getPredecessorChain();
        for (DataFlowState state : chain) {
            getTargets(state, snapshots, dependencies.computeIfAbsent(state, key -> new MultiMap<>(HashSet::new)), dependents.computeIfAbsent(state, key -> new MultiMap<>(HashSet::new)));
        }
        for (int i = chain.size() - 1; i >= 0; i--) {
            DataFlowState state = chain.get(i);
            getTargets(state, snapshots, dependencies.get(state), dependents.get(state));
            state.getSnapshots().forEach((field, snapshot) -> {
                if (snapshots.contains(snapshot)) {
                    compactState.snapshots.put(field, snapshot);
                    if (!(compactState.roots.containsKey(field))) {
                        compactState.roots.put(field, snapshot);
                    }
                }
            });
            for (Snapshot snapshot : snapshots) {
                compactState.conditions.addAll(dependents.get(state).getAll(snapshot));
            }
        }
        return compactState;
    }

    private void getTargets(@NotNull DataFlowState state, @NotNull Set<Snapshot> snapshots, @NotNull MultiMap<Expression, Snapshot> dependencies, @NotNull MultiMap<Snapshot, Expression> dependents) {
        for (Expression expression : state.getConditions()) {
            expression.iterate(component -> {
                if (component instanceof SnapshotExpression snapshot) {
                    dependencies.put(expression, snapshot.getSnapshot());
                    dependents.put(snapshot.getSnapshot(), expression);
                }
                return false;
            });
        }
        Deque<Snapshot> workList = new ArrayDeque<>(snapshots);
        while (!(workList.isEmpty())) {
            Snapshot snapshot = workList.removeLast();
            for (Expression expression : dependents.getAll(snapshot)) {
                for (Snapshot dependency : dependencies.getAll(expression)) {
                    if (snapshots.add(dependency)) {
                        workList.add(dependency);
                    }
                }
            }
        }
    }

    public @NotNull List<DataFlowState> getPredecessorChain() {
        List<DataFlowState> states = new ArrayList<>();
        for (DataFlowState predecessor = this; predecessor != null; predecessor = predecessor.getPredecessor()) {
            states.add(predecessor);
        }
        return states;
    }

    public @NotNull Block getFunctionBlock() {
        return functionBlock;
    }

    public @Nullable DataFlowState getPredecessor() {
        return predecessor;
    }

    public @NotNull List<DataFlowState> getSuccessors() {
        return successors;
    }

    public @NotNull Instruction getInstruction() {
        return instruction;
    }

    private void initializeDefault() {
        for (Variable variable : functionBlock.getVariables()) {
            snapshots.put(variable, createDefaultSnapshot(variable.getType(), null));
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
                } else if (argument.getParameterType() == ParameterType.REFERENCE && argument.getType().equals(RapidPrimitiveType.ANYTYPE)) {
                    snapshots.put(argument, Snapshot.createSnapshot(argument.getType(), Optionality.UNKNOWN));
                } else {
                    snapshots.put(argument, Snapshot.createSnapshot(argument.getType()));
                }
            }
        }
    }

    public @NotNull DataFlowState merge(@NotNull DataFlowState state, @NotNull Map<Snapshot, Snapshot> modifications) {
        DataFlowState successorState = DataFlowState.createSuccessorState(instruction, this);
        for (Expression expression : state.getConditions()) {
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
    }

    private @NotNull Optionality getQuickOptionality(@NotNull Expression expression) {
        if (expression instanceof SnapshotExpression snapshot) {
            return snapshot.getSnapshot().getOptionality();
        }
        return Optionality.PRESENT;
    }

    public void add(@NotNull Expression expression) {
        insert(expression.replace(this::getSnapshot));
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
                        add(new BinaryExpression(BinaryOperator.EQUAL_TO, FunctionCallExpression.length(referenceExpression, new LiteralExpression(1)), new LiteralExpression(components.size())));
                        for (int i = 0; i < components.size(); i++) {
                            Expression component = components.get(i);
                            IndexExpression indexExpression = new IndexExpression(referenceExpression, new LiteralExpression(i + 1));
                            assign(indexExpression, component);
                        }
                        return;
                    } else if (referenceExpression.getType().getRootStructure() instanceof RapidRecord record) {
                        List<? extends RapidComponent> fields = record.getComponents();
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
        if (conditions.contains(expression)) {
            return;
        }
        assert expression.getComponents().stream().noneMatch(expr -> expr instanceof ReferenceExpression && !(expr instanceof SnapshotExpression));
        conditions.add(expression);
    }

    public @NotNull Set<Expression> getConditions() {
        return conditions;
    }

    public @NotNull Map<Field, Snapshot> getSnapshots() {
        return snapshots;
    }

    public @NotNull Map<Field, Snapshot> getRoots() {
        return roots;
    }

    public @NotNull Constraint getConstraint(@NotNull Expression expression) {
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

    public @NotNull SnapshotExpression createSnapshot(@NotNull Expression expression, @NotNull Optionality optionality) {
        if (expression instanceof SnapshotExpression reference) {
            return reference;
        }
        if (expression instanceof IndexExpression indexExpression) {
            SnapshotExpression snapshot = getSnapshot(indexExpression.getVariable());
            if (!(snapshot.getSnapshot() instanceof ArraySnapshot array)) {
                throw new IllegalArgumentException("Could not find array snapshot for expression: " + indexExpression + ", found: " + snapshot + " of type: " + snapshot.getType());
            }
            Expression index = getSnapshot(indexExpression.getIndex());
            ArraySnapshot replacement = array.copy();
            SnapshotExpression arrayExpression = new SnapshotExpression(array);
            SnapshotExpression replacementExpression = new SnapshotExpression(replacement);
            assign(indexExpression.getVariable(), replacementExpression);
            SnapshotExpression value = new SnapshotExpression(Snapshot.createSnapshot(indexExpression.getType()));
            BinaryExpression equalToUpdate = new BinaryExpression(BinaryOperator.EQUAL_TO, replacementExpression, FunctionCallExpression.store(arrayExpression, index, value));
            assert equalToUpdate.getComponents().stream().noneMatch(expr -> expr instanceof ReferenceExpression && !(expr instanceof SnapshotExpression));
            conditions.add(equalToUpdate);
            BinaryExpression presentEqual = new BinaryExpression(BinaryOperator.EQUAL_TO, FunctionCallExpression.present(arrayExpression), FunctionCallExpression.present(replacementExpression));
            assert presentEqual.getComponents().stream().noneMatch(expr -> expr instanceof ReferenceExpression && !(expr instanceof SnapshotExpression));
            conditions.add(presentEqual);
            for (int i = 0; i < arrayExpression.getType().getDimensions(); i++) {
                LiteralExpression depth = new LiteralExpression(i + 1);
                BinaryExpression lengthEqual = new BinaryExpression(BinaryOperator.EQUAL_TO, FunctionCallExpression.length(arrayExpression, depth), FunctionCallExpression.length(replacementExpression, depth));
                assert lengthEqual.getComponents().stream().noneMatch(expr -> expr instanceof ReferenceExpression && !(expr instanceof SnapshotExpression));
                conditions.add(lengthEqual);
            }
            return value;
        }
        if (expression instanceof ComponentExpression componentExpression) {
            SnapshotExpression snapshot = getSnapshot(componentExpression.getVariable());
            if (!(snapshot.getSnapshot() instanceof RecordSnapshot record)) {
                throw new IllegalArgumentException("Could not find record snapshot for expression: " + componentExpression + ", found: " + snapshot + " of type: " + snapshot.getType());
            }
            RecordSnapshot replacement = record.copy();
            SnapshotExpression recordExpression = new SnapshotExpression(record);
            SnapshotExpression replacementExpression = new SnapshotExpression(replacement);
            assign(componentExpression.getVariable(), replacementExpression);
            SnapshotExpression value = new SnapshotExpression(Snapshot.createSnapshot(componentExpression.getType()));
            BinaryExpression equalToUpdate = new BinaryExpression(BinaryOperator.EQUAL_TO, replacementExpression, FunctionCallExpression.store(recordExpression, componentExpression.getComponent(), value));
            assert equalToUpdate.getComponents().stream().noneMatch(expr -> expr instanceof ReferenceExpression && !(expr instanceof SnapshotExpression));
            conditions.add(equalToUpdate);
            BinaryExpression presentEqual = new BinaryExpression(BinaryOperator.EQUAL_TO, FunctionCallExpression.present(recordExpression), FunctionCallExpression.present(replacementExpression));
            assert equalToUpdate.getComponents().stream().noneMatch(expr -> expr instanceof ReferenceExpression && !(expr instanceof SnapshotExpression));
            conditions.add(presentEqual);
            return value;
        }
        Snapshot snapshot = Snapshot.createSnapshot(expression.getType(), optionality);
        if (expression instanceof VariableExpression variable) {
            snapshots.put(variable.getField(), snapshot);
        }
        SnapshotExpression reference = new SnapshotExpression(snapshot, expression);
        if (!(expression instanceof ReferenceExpression)) {
            add(new BinaryExpression(BinaryOperator.EQUAL_TO, reference, expression));
        }
        return reference;
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
            if (predecessor.getInstruction().equals(getInstruction())) {
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
     * the predecessors of this state are recursively queried until a snapshot is found.
     *
     * @param expression the variable.
     * @return the latest snapshot for the variable.
     */
    public @NotNull SnapshotExpression getSnapshot(@NotNull ReferenceExpression expression) {
        return expression.accept(new ControlFlowVisitor<>() {
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
                return FunctionCallExpression.select(getSnapshot(expression.getVariable()), getSnapshot(expression.getIndex()));
            }

            @Override
            public @NotNull SnapshotExpression visitComponentExpression(@NotNull ComponentExpression expression) {
                return FunctionCallExpression.select(getSnapshot(expression.getVariable()), expression.getComponent());
            }
        });
    }

    public void prune() {
        prune(state -> {});
    }

    public void prune(@NotNull Consumer<DataFlowState> consumer) {
        consumer.accept(this);
        block.getFunction().unregisterOutput(this);
        if (predecessor != null) {
            predecessor.getSuccessors().remove(this);
        }
        predecessor = null;
        for (DataFlowState successor : Set.copyOf(successors)) {
            successor.prune(consumer);
        }
    }

    public void clear() {
        conditions.clear();
        snapshots.clear();
        getSnapshots().putAll(getRoots());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataFlowState that = (DataFlowState) o;
        return Objects.equals(instruction, that.instruction) && Objects.equals(functionBlock, that.functionBlock) && Objects.equals(conditions, that.conditions) && Objects.equals(snapshots, that.snapshots) && Objects.equals(roots, that.roots) && Objects.equals(predecessor, that.predecessor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instruction, functionBlock, conditions, snapshots, roots, predecessor);
    }

    @Override
    public String toString() {
        return "DataFlowState{" +
                "name=" + functionBlock.getModuleName() + ":" + functionBlock.getName() +
                ", index=" + instruction.getIndex() +
                ", instruction=" + instruction +
                ", conditions=" + conditions.size() +
                '}';
    }
}

package com.bossymr.flow.state;

import com.bossymr.flow.Method;
import com.bossymr.flow.constraint.Constraint;
import com.bossymr.flow.expression.BinaryExpression;
import com.bossymr.flow.expression.Expression;
import com.bossymr.flow.value.Variable;

import java.util.*;

/**
 * A snapshot of the program at a specific instruction.
 */
public class MemorySnapshot {

    private final MemorySnapshot predecessor;
    private final Set<MemorySnapshot> successors = new HashSet<>();

    private final Set<Expression> constraints = new HashSet<>();
    private final Map<Variable, VariableSnapshot> snapshots = new HashMap<>();

    private MemorySnapshot() {
        this.predecessor = null;
    }

    private MemorySnapshot(MemorySnapshot predecessor) {
        Objects.requireNonNull(predecessor);
        this.predecessor = predecessor;
        this.successors.add(predecessor);
    }

    /**
     * Create a new, empty, snapshot.
     *
     * @return a new snapshot.
     */
    public static MemorySnapshot emptyState() {
        return new MemorySnapshot();
    }

    /**
     * Create a snapshot representing the state at the start of the specified method.
     * <p>
     * By default, all variables have an unknown value.
     *
     * @param method the method.
     * @return a new snapshot.
     */
    public static MemorySnapshot initialState(Method method) {
        return new MemorySnapshot();
    }

    /**
     * Create a successor to this snapshot.
     *
     * @return a new snapshot.
     */
    public MemorySnapshot successorState() {
        return new MemorySnapshot(this);
    }

    /**
     * Returns the successors of this snapshot.
     *
     * @return the successors of this snapshot.
     */
    public Set<MemorySnapshot> getSuccessors() {
        return successors;
    }

    /**
     * Returns the predecessor of this snapshot.
     *
     * @return the predecessor of this snapshot.
     */
    public MemorySnapshot getPredecessor() {
        return predecessor;
    }

    /**
     * Returns all constraints defined in this snapshot.
     *
     * @return all constraints defined in this snapshot.
     */
    public Set<Expression> getConstraints() {
        return constraints;
    }

    /**
     * Checks if this snapshot is reachable.
     *
     * @return if this snapshot is reachable.
     */
    public boolean isReachable() {
        return false;
    }

    /**
     * Attempts to compute the result of the provided expression.
     *
     * @param expression the expression to compute.
     * @return the result of the provided expression.
     */
    public Constraint getConstraint(Expression expression) {
        return null;
    }

    /**
     * Assigns the specified expression to the specified variable.
     *
     * @param variable the variable.
     * @param expression the expression.
     */
    public void assign(Variable variable, Expression expression) {
        // TODO: Check if the expression is assignable to the variable.
        VariableSnapshot snapshot = new VariableSnapshot(variable.getType());
        snapshots.put(variable, snapshot);
        // TODO: Replace references to variables with their respective snapshots.
        constraints.add(new BinaryExpression(BinaryExpression.Operator.EQUAL_TO, snapshot, expression));
    }

    public void require(Expression expression) {
        // TODO: Check if the expression is a condition.
        // TODO: Replace references to variables with their respective snapshots.
        constraints.add(expression);
    }

}

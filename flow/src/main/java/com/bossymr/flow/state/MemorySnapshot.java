package com.bossymr.flow.state;

import com.bossymr.flow.constraint.Constraint;
import com.bossymr.flow.expression.BinaryExpression;
import com.bossymr.flow.expression.Expression;
import com.bossymr.flow.expression.LiteralExpression;
import com.bossymr.flow.expression.UnaryExpression;
import com.bossymr.flow.instruction.Instruction;
import com.bossymr.flow.value.Variable;

import java.util.*;

/**
 * A snapshot of the program at a specific instruction.
 */
public class MemorySnapshot {

    private final MemorySnapshot predecessor;
    private final Set<MemorySnapshot> successors = new HashSet<>();
    private final Instruction instruction;

    private final Set<Expression> constraints = new HashSet<>();
    private final Map<Variable, VariableSnapshot> snapshots = new HashMap<>();

    private MemorySnapshot() {
        this.instruction = null;
        this.predecessor = null;
    }

    private MemorySnapshot(Instruction instruction) {
        this.instruction = instruction;
        this.predecessor = null;
    }

    private MemorySnapshot(MemorySnapshot predecessor, Instruction instruction) {
        this.instruction = instruction;
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
     * Create a new, empty, snapshot, representing the specified instruction.
     *
     * @param instruction the instruction.
     * @return a new snapshot.
     */
    public static MemorySnapshot emptyState(Instruction instruction) {
        return new MemorySnapshot(instruction);
    }

    /**
     * Create a successor to this snapshot which represents the same instruction as this snapshot.
     *
     * @return a new snapshot.
     */
    public MemorySnapshot successorState() {
        return new MemorySnapshot(this, this.instruction);
    }

    /**
     * Create a successor to this snapshot.
     *
     * @param instruction the instruction.
     * @return a new snapshot.
     */
    public MemorySnapshot successorState(Instruction instruction) {
        return new MemorySnapshot(this, instruction);
    }

    /**
     * Returns the instruction this snapshot represents.
     *
     * @return the instruction this snapshot represents.
     */
    public Instruction getInstruction() {
        return instruction;
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
     * Returns a copy of the provided expression, where all variables are replaced by their respective latest snapshot.
     *
     * @param expression the expression.
     * @return a copy of the provided expression.
     */
    public Expression clean(Expression expression) {
        return switch (expression) {
            case BinaryExpression binaryExpression -> new BinaryExpression(binaryExpression.getOperator(), clean(binaryExpression.getLeft()), clean(binaryExpression.getRight()));
            case UnaryExpression unaryExpression -> new UnaryExpression(unaryExpression.getOperator(), clean(unaryExpression.getExpression()));
            case LiteralExpression literalExpression -> literalExpression;
            default -> throw new IllegalStateException("unexpected expression: " + expression);
        };
    }

    /**
     * Assigns the specified expression to the specified variable.
     *
     * @param variable the variable.
     * @param expression the expression.
     */
    public void assign(Variable variable, Expression expression) {
        if (!variable.getType().equals(expression.getType())) {

        }
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

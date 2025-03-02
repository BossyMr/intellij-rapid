package com.bossymr.flow.state;

import com.bossymr.flow.Instruction;
import com.bossymr.flow.constraint.Constraint;
import com.bossymr.flow.expression.Expression;

import java.util.*;

/**
 * A snapshot of the program at a specific instruction.
 */
public class FlowSnapshot {

    private final FlowEngine engine;

    private final FlowSnapshot predecessor;
    private final Set<FlowSnapshot> successors = new HashSet<>();
    private final Instruction instruction;

    private final Set<Expression> constraints;
    private final List<Expression> stack;

    private FlowSnapshot(FlowEngine engine, Instruction instruction) {
        this.engine = engine;
        this.instruction = instruction;
        this.predecessor = null;
        this.stack = new ArrayList<>();
        this.constraints = new HashSet<>();
    }

    private FlowSnapshot(FlowEngine engine, FlowSnapshot predecessor, Instruction instruction) {
        this.engine = engine;
        this.instruction = instruction;
        Objects.requireNonNull(predecessor);
        this.predecessor = predecessor;
        this.stack = new ArrayList<>(predecessor.stack);
        this.constraints = new HashSet<>(predecessor.constraints);
    }

    /**
     * Create a new, empty, snapshot.
     *
     * @param engine the current flow engine.
     * @return a new snapshot.
     */
    public static FlowSnapshot emptyState(FlowEngine engine) {
        return new FlowSnapshot(engine, null);
    }

    /**
     * Create a new, empty, snapshot.
     *
     * @param engine the current flow engine.
     * @param instruction the instruction.
     * @return a new snapshot.
     */
    public static FlowSnapshot emptyState(FlowEngine engine, Instruction instruction) {
        return new FlowSnapshot(engine, instruction);
    }

    /**
     * Create a successor to this snapshot representing the same instruction as this snapshot.
     *
     * @return a new snapshot.
     */
    public FlowSnapshot successorState() {
        FlowSnapshot successor = new FlowSnapshot(this.engine, this, this.instruction);
        getSuccessors().add(successor);
        return successor;
    }

    /**
     * Create a successor to this snapshot.
     *
     * @param instruction the instruction.
     * @return a new snapshot.
     */
    public FlowSnapshot successorState(Instruction instruction) {
        FlowSnapshot successor = new FlowSnapshot(this.engine, this, instruction);
        getSuccessors().add(successor);
        return successor;
    }

    /**
     * Create a successor to this snapshot. The new snapshot will have the instruction after this snapshot's
     * instruction, as declared in the provided method.
     *
     * @param method the method.
     * @return a new snapshot, or {@code null} if this is the last instruction is the provided method.
     * @throws IllegalArgumentException if this instruction is not in the provided method or if this snapshot does not
     * have an instruction.
     */
    public FlowSnapshot successorState(FlowMethod method) {
        return successorState(method, this.instruction);
    }

    /**
     * Create a successor to this snapshot. The new snapshot will have the instruction after the specified instruction,
     * as declared in the provided method.
     *
     * @param method the method.
     * @param instruction the current instruction.
     * @return a new snapshot, or {@code null} if this is the last instruction is the provided method.
     * @throws IllegalArgumentException if this instruction is not in the provided method or if this snapshot does not
     * have an instruction.
     */
    public FlowSnapshot successorState(FlowMethod method, Instruction instruction) {
        if (instruction == null) {
            throw new IllegalArgumentException("cannot find successor to this snapshot: snapshot does not refer to an instruction");
        }
        List<Instruction> instructions = method.getMethod().getInstructions();
        int index = instructions.indexOf(instruction);
        if (index < 0) {
            throw new IllegalArgumentException("cannot find successor to this snapshot: instruction not found in method '" + method.getMethod().getName() + "'");
        }
        if (index + 1 >= instructions.size()) {
            return null;
        }
        Instruction successor = instructions.get(index + 1);
        return successorState(successor);
    }

    /**
     * Creates a successor to this snapshot, which is not stored as a successor to this snapshot.
     *
     * @return a new snapshot.
     */
    public FlowSnapshot disconnectedState() {
        return new FlowSnapshot(this.engine, this, null);
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
    public Set<FlowSnapshot> getSuccessors() {
        return successors;
    }

    /**
     * Returns the predecessor of this snapshot.
     *
     * @return the predecessor of this snapshot.
     */
    public FlowSnapshot getPredecessor() {
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
     * Returns the current program stack.
     *
     * @return the current program stack.
     */
    public List<Expression> getStack() {
        return stack;
    }

    /**
     * Checks if this snapshot is reachable.
     *
     * @return if this snapshot is reachable.
     */
    public boolean isReachable() {
        return switch (engine.getConstraintEngine().isReachable(this)) {
            case REACHABLE, UNKNOWN -> true;
            case NOT_REACHABLE -> false;
        };
    }

    /**
     * Attempts to compute the result of the provided expression.
     *
     * @param expression the expression to compute.
     * @return the result of the provided expression.
     */
    public Constraint compute(Expression expression) {
        return engine.getConstraintEngine().getConstraint(this, expression);
    }

    /**
     * Pushes the specified expression to the stack.
     *
     * @param expression the expression.
     */
    public void push(Expression expression) {
        stack.addLast(expression);
    }

    /**
     * Pops an expression from the stack.
     *
     * @return an expression.
     */
    public Expression pop() {
        return stack.removeLast();
    }

    /**
     * Adds the specified expression as a constraint.
     *
     * @param expression the expression.
     */
    public void require(Expression expression) {
        constraints.add(expression);
    }
}

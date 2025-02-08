package com.bossymr.flow.state;

import com.bossymr.flow.Method;
import com.bossymr.flow.constraint.Constraint;
import com.bossymr.flow.expression.Expression;
import com.bossymr.flow.value.variable.Variable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A snapshot of the program at a specific instruction.
 */
public class MemorySnapshot {

    private final MemorySnapshot predecessor;
    private final Set<MemorySnapshot> successors;

    private final Set<Expression> conditions = new HashSet<>();
    private final Map<Variable, VariableSnapshot> snapshots = new HashMap<>();

    private MemorySnapshot(MemorySnapshot predecessor) {
        this.predecessor = predecessor;
        this.successors = new HashSet<>();
    }

    /**
     * Create a snapshot representing the state at the start of the specified method.
     * <p>
     * All variables are initialized to their default value.
     *
     * @param method the method.
     * @return a new snapshot.
     */
    public static MemorySnapshot initialState(Method method) {

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
    public Constraint execute(Expression expression) {
        return null;
    }

}

package com.bossymr.flow.constraint;

import com.bossymr.flow.expression.BinaryExpression;
import com.bossymr.flow.expression.Expression;
import com.bossymr.flow.expression.LiteralExpression;
import com.bossymr.flow.state.MemorySnapshot;
import io.github.cvc5.*;

import java.util.ArrayList;
import java.util.List;

public class ConstraintEngine {

    private ConstraintEngine() {}

    /**
     * Checks whether the provided snapshot is reachable. A snapshot is reachable if all constraints are satisfiable,
     * that is, whether there exists some value for all variables where all constraints are met. Likewise, a snapshot is
     * unreachable if all constraints can't be met at the same time.
     *
     * @param snapshot the snapshot.
     * @return if the snapshot is reachable.
     */
    public static Reachable isReachable(MemorySnapshot snapshot) {
        try {
            Solver solver = createSolver(snapshot);
            Result result = solver.checkSat();
            if (result.isSat()) {
                return Reachable.REACHABLE;
            } else if (result.isUnsat()) {
                return Reachable.NOT_REACHABLE;
            } else {
                return Reachable.UNKNOWN;
            }
        } catch (CVC5ApiException e) {
            return Reachable.UNKNOWN;
        }
    }

    /**
     * Checks the possible values of the provided predicate.
     *
     * @param snapshot the snapshot.
     * @param predicate the expression.
     * @return the possible values of the provided predicate.
     * @throws IllegalArgumentException if the provided expression is not a predicate.
     */
    public static Constraint getConstraint(MemorySnapshot snapshot, Expression predicate) {
        try {
            Solver solver = createSolver(snapshot);
            TermManager manager = solver.getTermManager();
            Term expression = predicate.getConstraint(manager, variable -> {
                // TODO: Implement
                return null;
            });
            solver.push();
            solver.assertFormula(manager.mkTerm(manager.mkOp(Kind.EQUAL), expression, manager.mkBoolean(true)));
            Result maybeTrue = solver.checkSat();
            if (maybeTrue.isUnknown()) {
                return Constraint.UNKNOWN;
            }
            solver.pop();
            solver.assertFormula(manager.mkTerm(manager.mkOp(Kind.EQUAL), expression, manager.mkBoolean(false)));
            Result maybeFalse = solver.checkSat();
            if (maybeFalse.isUnknown()) {
                return Constraint.UNKNOWN;
            }
            if (maybeTrue.isSat() && maybeFalse.isSat()) {
                return Constraint.ANY_VALUE;
            }
            if (maybeTrue.isSat()) {
                return Constraint.ALWAYS_TRUE;
            }
            if (maybeFalse.isSat()) {
                return Constraint.ALWAYS_FALSE;
            }
            return Constraint.NO_VALUE;
        } catch (CVC5ApiException e) {
            return Constraint.UNKNOWN;
        }
    }

    private static Solver createSolver(MemorySnapshot snapshot) throws CVC5ApiException {
        TermManager manager = new TermManager();
        Solver solver = new Solver(manager);
        solver.setLogic("ALL");
        for (MemorySnapshot state : getSnapshotBranch(snapshot)) {
            for (Expression constraint : state.getConstraints()) {
                Term expression = constraint.getConstraint(manager, variable -> {
                    // TODO: Implement
                    return null;
                });
                solver.assertFormula(expression);
            }
        }
        return solver;
    }

    private static List<MemorySnapshot> getSnapshotBranch(MemorySnapshot snapshot) {
        List<MemorySnapshot> snapshots = new ArrayList<>();
        while (snapshot != null) {
            snapshots.add(snapshot);
            snapshot = snapshot.getPredecessor();
        }
        return snapshots.reversed();
    }
}

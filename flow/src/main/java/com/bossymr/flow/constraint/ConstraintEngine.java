package com.bossymr.flow.constraint;

import com.bossymr.flow.expression.BinaryExpression;
import com.bossymr.flow.expression.Expression;
import com.bossymr.flow.expression.LiteralExpression;
import com.bossymr.flow.expression.UnaryExpression;
import com.bossymr.flow.state.MemorySnapshot;
import com.bossymr.flow.type.*;
import com.bossymr.flow.value.Variable;
import io.github.cvc5.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            Term expression = createTerm(manager, new HashMap<>(), predicate);
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
                Term expression = createTerm(manager, new HashMap<>(), constraint);
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

    private static Term createTerm(TermManager manager, Map<Variable, Term> variables, Expression expression) {
        return switch (expression) {
            case UnaryExpression unary -> {
                Term component = createTerm(manager, variables, unary.getExpression());
                Op operator = switch (unary.getOperator()) {
                    case NOT -> manager.mkOp(Kind.NOT);
                    case NEGATE -> manager.mkOp(Kind.NEG);
                };
                yield manager.mkTerm(operator, component);
            }
            case BinaryExpression binary -> {
                Term left = createTerm(manager, variables, binary.getLeft());
                Term right = createTerm(manager, variables, binary.getRight());
                if (binary.getLeft().getType() instanceof IntegerType && binary.getRight().getType() instanceof RealType) {
                    // Expression: integer <operator> real
                    // The left-most expression must be cast to a real number, so that both expressions are of the same
                    // type. Otherwise, an exception will be thrown by the solver.
                    left = manager.mkTerm(manager.mkOp(Kind.TO_REAL), left);
                }
                if (binary.getLeft().getType() instanceof RealType && binary.getRight().getType() instanceof IntegerType) {
                    // Expression: real <operator> integer
                    // Same as the previous check, but with the right-most expression.
                    right = manager.mkTerm(manager.mkOp(Kind.TO_REAL), right);
                }
                Op operator = switch (binary.getOperator()) {
                    case EQUAL_TO -> manager.mkOp(Kind.EQUAL);
                    case GREATER_THAN -> manager.mkOp(Kind.GT);
                    case LESS_THAN -> manager.mkOp(Kind.LT);
                    case ADD -> manager.mkOp(Kind.ADD);
                    case SUBTRACT -> manager.mkOp(Kind.SUB);
                    case MULTIPLY -> manager.mkOp(Kind.MULT);
                    case DIVIDE -> manager.mkOp(Kind.DIVISION);
                    case MODULO -> manager.mkOp(Kind.INTS_MODULUS);
                    case AND -> manager.mkOp(Kind.AND);
                    case XOR -> manager.mkOp(Kind.XOR);
                    case OR -> manager.mkOp(Kind.OR);
                };
                yield manager.mkTerm(operator, left, right);
            }
            case LiteralExpression literal -> switch (literal.getValue()) {
                case Boolean value -> manager.mkBoolean(value);
                case String value -> manager.mkString(value);
                case Integer value -> manager.mkInteger(value);
                case Long value -> manager.mkInteger(value);
                case RealType.Fraction(long numerator, long denominator) -> manager.mkReal(numerator, denominator);
                default -> throw new IllegalStateException();
            };
            default -> throw new IllegalStateException();
        };
    }
}

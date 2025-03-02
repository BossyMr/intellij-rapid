package com.bossymr.flow.constraint;

import com.bossymr.flow.expression.BinaryExpression;
import com.bossymr.flow.expression.LiteralExpression;
import com.bossymr.flow.expression.UnaryExpression;
import com.bossymr.flow.state.FlowEngine;
import com.bossymr.flow.state.FlowSnapshot;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConstraintEngineTest {

    @DisplayName("Assert 0 != 1")
    @Test
    void zeroEqualToOneNotReachable() {
        FlowEngine engine = new FlowEngine();
        FlowSnapshot snapshot = FlowSnapshot.emptyState(engine);
        snapshot.require(new BinaryExpression(BinaryExpression.Operator.EQUAL_TO, LiteralExpression.integerLiteral(0), LiteralExpression.integerLiteral(1)));
        Reachable reachable = engine.getConstraintEngine().isReachable(snapshot);
        assertEquals(Reachable.NOT_REACHABLE, reachable);
    }

    @DisplayName("Assert 0 == 0")
    @Test
    void zeroEqualToZeroReachable() {
        FlowEngine engine = new FlowEngine();
        FlowSnapshot snapshot = FlowSnapshot.emptyState(engine);
        snapshot.require(new BinaryExpression(BinaryExpression.Operator.EQUAL_TO, LiteralExpression.integerLiteral(0), LiteralExpression.integerLiteral(0)));
        Reachable reachable = engine.getConstraintEngine().isReachable(snapshot);
        assertEquals(Reachable.REACHABLE, reachable);
    }

    @DisplayName("Assert 0 (int) == 0 (real) as int")
    @Test
    void zeroIntEqualToZeroRealReachable() {
        FlowEngine engine = new FlowEngine();
        FlowSnapshot snapshot = FlowSnapshot.emptyState(engine);
        snapshot.require(new BinaryExpression(BinaryExpression.Operator.EQUAL_TO, LiteralExpression.integerLiteral(0), new UnaryExpression(UnaryExpression.Operator.REAL_TO_INTEGER, LiteralExpression.numericLiteral(0))));
        Reachable reachable = engine.getConstraintEngine().isReachable(snapshot);
        assertEquals(Reachable.REACHABLE, reachable);
    }
}
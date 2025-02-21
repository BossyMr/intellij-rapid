package com.bossymr.flow.constraint;

import com.bossymr.flow.expression.BinaryExpression;
import com.bossymr.flow.expression.LiteralExpression;
import com.bossymr.flow.state.MemorySnapshot;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConstraintEngineTest {

    @DisplayName("Assert 0 != 1")
    @Test
    void zeroEqualToOneNotReachable() {
        MemorySnapshot snapshot = MemorySnapshot.emptyState();
        snapshot.require(new BinaryExpression(BinaryExpression.Operator.EQUAL_TO, LiteralExpression.integerLiteral(0), LiteralExpression.integerLiteral(1)));
        Reachable reachable = ConstraintEngine.isReachable(snapshot);
        assertEquals(Reachable.NOT_REACHABLE, reachable);
    }

    @DisplayName("Assert 0 == 0")
    @Test
    void zeroEqualToZeroReachable() {
        MemorySnapshot snapshot = MemorySnapshot.emptyState();
        snapshot.require(new BinaryExpression(BinaryExpression.Operator.EQUAL_TO, LiteralExpression.integerLiteral(0), LiteralExpression.integerLiteral(0)));
        Reachable reachable = ConstraintEngine.isReachable(snapshot);
        assertEquals(Reachable.REACHABLE, reachable);
    }

    @DisplayName("Assert 0 (int) == 0 (real)")
    @Test
    void zeroIntEqualToZeroRealReachable() {
        MemorySnapshot snapshot = MemorySnapshot.emptyState();
        snapshot.require(new BinaryExpression(BinaryExpression.Operator.EQUAL_TO, LiteralExpression.integerLiteral(0), LiteralExpression.numericLiteral(0)));
        Reachable reachable = ConstraintEngine.isReachable(snapshot);
        assertEquals(Reachable.REACHABLE, reachable);
    }
}
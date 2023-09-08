package com.bossymr.rapid.language.flow.condition;

import com.bossymr.rapid.language.flow.constraint.NumericConstraint;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.symbol.RapidType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConditionTest {

    @Test
    void iterateVariables() {
        VariableSnapshot variable = new VariableSnapshot(RapidType.NUMBER);
        VariableSnapshot left = new VariableSnapshot(RapidType.NUMBER);
        VariableSnapshot right = new VariableSnapshot(RapidType.NUMBER);
        Expression expression = new BinaryExpression(BinaryOperator.ADD, left, right);
        Condition condition = new Condition(variable, ConditionType.EQUALITY, expression);
        List<ReferenceValue> variables = condition.getVariables();
        assertEquals(2, variables.size());
        assertEquals(left, variables.get(0));
        assertEquals(right, variables.get(1));
    }

    @Test
    void negateTest() {
        NumericConstraint constraint = NumericConstraint.any();
        NumericConstraint negate = (NumericConstraint) constraint.negate();
        assertTrue(negate.isEmpty());
        assertFalse(negate.isFull());
        assertTrue(negate.getRanges().isEmpty());
        assertTrue(negate.getPoint().isEmpty());
        assertTrue(negate.getMaximum().isEmpty());
        assertTrue(negate.getMinimum().isEmpty());
    }

    @Test
    void solveAddition() {
        VariableSnapshot variable = new VariableSnapshot(RapidType.NUMBER);
        VariableSnapshot left = new VariableSnapshot(RapidType.NUMBER);
        VariableSnapshot right = new VariableSnapshot(RapidType.NUMBER);
        Expression expression = new BinaryExpression(BinaryOperator.ADD, left, right);
        Condition condition = new Condition(variable, ConditionType.EQUALITY, expression);
        List<Condition> variants = condition.getVariants();
        assertEquals(3, variants.size());
        assertEquals(condition, variants.get(0));
        assertEquals(new Condition(left, ConditionType.EQUALITY, new BinaryExpression(BinaryOperator.SUBTRACT, variable, right)), variants.get(1));
        assertEquals(new Condition(right, ConditionType.EQUALITY, new BinaryExpression(BinaryOperator.SUBTRACT, variable, left)), variants.get(2));
    }

    @Test
    void doNotSolve() {
        VariableSnapshot variable = new VariableSnapshot(RapidType.NUMBER);
        VariableSnapshot left = new VariableSnapshot(RapidType.NUMBER);
        Expression expression = new BinaryExpression(BinaryOperator.EQUAL_TO, left, ConstantValue.of(RapidType.NUMBER, 0));
        Condition condition = new Condition(variable, ConditionType.EQUALITY, expression);
        List<Condition> variants = condition.getVariants();
        assertEquals(1, variants.size());
        assertEquals(condition, variants.get(0));
    }
}

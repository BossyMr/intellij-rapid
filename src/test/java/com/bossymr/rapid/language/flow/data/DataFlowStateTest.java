package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.constraint.NumericConstraint;
import com.bossymr.rapid.language.flow.constraint.Optionality;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.RoutineType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataFlowStateTest {

    @Test
    void getConstraintFromConstraint() {
        DataFlowState state = new DataFlowState(getEmptyFunctionBlock());
        VariableSnapshot snapshot = new VariableSnapshot(RapidType.NUMBER);
        NumericConstraint constraint = new NumericConstraint(Optionality.UNKNOWN, new NumericConstraint.Bound(true, 0), new NumericConstraint.Bound(true, 10));
        state.assign(snapshot, constraint);
        assertEquals(constraint, state.getConstraint(snapshot));
    }

    @Test
    void getConstraintFromCondition() {
        DataFlowState state = new DataFlowState(getEmptyFunctionBlock());
        VariableSnapshot snapshot = new VariableSnapshot(RapidType.NUMBER);
        state.assign(snapshot, Expression.numericConstant(0));
        assertEquals(NumericConstraint.equalTo(0), state.getConstraint(snapshot));
        state.assign(snapshot, new BinaryExpression(BinaryOperator.MULTIPLY, new ConstantValue(RapidType.NUMBER, 5), new ConstantValue(RapidType.NUMBER, 10)));
        assertEquals(NumericConstraint.equalTo(50), state.getConstraint(snapshot));
    }

    @Test
    void getConstraintWithRelation() {
        DataFlowState state = new DataFlowState(getEmptyFunctionBlock());
        VariableSnapshot reference = new VariableSnapshot(RapidType.NUMBER);
        state.assign(reference, new NumericConstraint(Optionality.PRESENT, new NumericConstraint.Bound(true, 0), new NumericConstraint.Bound(true, 10)));
        VariableSnapshot snapshot = new VariableSnapshot(RapidType.NUMBER);
        state.assign(snapshot, new BinaryExpression(BinaryOperator.MULTIPLY, new ConstantValue(RapidType.NUMBER, 5), reference));
        assertEquals(new NumericConstraint(Optionality.PRESENT, new NumericConstraint.Bound(true, 0), new NumericConstraint.Bound(true, 50)), state.getConstraint(snapshot));
    }

    private @NotNull Block.FunctionBlock getEmptyFunctionBlock() {
        return new Block.FunctionBlock("foo", "bar", null, RoutineType.PROCEDURE);
    }
}

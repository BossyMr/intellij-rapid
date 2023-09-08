package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.constraint.NumericConstraint;
import com.bossymr.rapid.language.flow.constraint.Optionality;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.value.BinaryExpression;
import com.bossymr.rapid.language.flow.value.BinaryOperator;
import com.bossymr.rapid.language.flow.value.ConstantValue;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.virtual.VirtualRoutine;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataFlowStateTest {

    @Test
    void getConstraintFromConstraint() {
        DataFlowState state = DataFlowState.createState(getEmptyFunctionBlock());
        VariableSnapshot snapshot = new VariableSnapshot(RapidType.NUMBER);
        NumericConstraint constraint = new NumericConstraint(Optionality.UNKNOWN, new NumericConstraint.Bound(true, 0), new NumericConstraint.Bound(true, 10));
        state.assign(snapshot, constraint);
        assertEquals(constraint, state.getConstraint(snapshot));
    }

    public static @NotNull Block.FunctionBlock getEmptyFunctionBlock() {
        return new Block.FunctionBlock(new VirtualRoutine(RoutineType.PROCEDURE, "bar", null, List.of()), "foo");
    }

    @Test
    void getConstraintFromCondition() {
        DataFlowState state = DataFlowState.createState(getEmptyFunctionBlock());
        VariableSnapshot snapshot = new VariableSnapshot(RapidType.NUMBER);
        state.assign(new Condition(snapshot, ConditionType.EQUALITY, Expression.of(0)), true);
        assertEquals(NumericConstraint.equalTo(0), state.getConstraint(snapshot));
        snapshot = new VariableSnapshot(RapidType.NUMBER);
        state.assign(new Condition(snapshot, ConditionType.EQUALITY, new BinaryExpression(BinaryOperator.MULTIPLY, ConstantValue.of(RapidType.NUMBER, 5), ConstantValue.of(RapidType.NUMBER, 10))), true);
        assertEquals(NumericConstraint.equalTo(50), state.getConstraint(snapshot));
    }

    @Test
    void getConstraintWithRelation() {
        DataFlowState state = DataFlowState.createState(getEmptyFunctionBlock());
        VariableSnapshot reference = new VariableSnapshot(RapidType.NUMBER);
        state.assign(reference, new NumericConstraint(Optionality.PRESENT, new NumericConstraint.Bound(true, 0), new NumericConstraint.Bound(true, 10)));
        VariableSnapshot snapshot = new VariableSnapshot(RapidType.NUMBER);
        state.assign(new Condition(snapshot, ConditionType.EQUALITY, new BinaryExpression(BinaryOperator.MULTIPLY, ConstantValue.of(RapidType.NUMBER, 5), reference)), true);
        assertEquals(new NumericConstraint(Optionality.PRESENT, new NumericConstraint.Bound(true, 0), new NumericConstraint.Bound(true, 50)), state.getConstraint(snapshot));
    }
}

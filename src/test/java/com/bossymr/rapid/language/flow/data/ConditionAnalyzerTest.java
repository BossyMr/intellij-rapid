package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.constraint.BooleanConstraint;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.value.BinaryExpression;
import com.bossymr.rapid.language.flow.value.BinaryOperator;
import com.bossymr.rapid.language.flow.value.ConstantValue;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.symbol.RapidType;
import org.junit.jupiter.api.Test;

class ConditionAnalyzerTest {

    @Test
    void testCondition() {
        DataFlowState state = DataFlowState.createState(DataFlowStateTest.getEmptyFunctionBlock());
        VariableSnapshot integerSnapshot = new VariableSnapshot(RapidType.NUMBER);
        VariableSnapshot booleanSnapshot = new VariableSnapshot(RapidType.BOOLEAN);
        state.assign(new Condition(booleanSnapshot, ConditionType.EQUALITY, Expression.of(0)), true);
        ConditionAnalyzer conditionAnalyzer = new ConditionAnalyzer(state);
        Condition condition = new Condition(booleanSnapshot, ConditionType.EQUALITY, new BinaryExpression(BinaryOperator.EQUAL_TO, integerSnapshot, ConstantValue.of(0)));
        BooleanConstraint constraint = condition.accept(conditionAnalyzer);
    }
}

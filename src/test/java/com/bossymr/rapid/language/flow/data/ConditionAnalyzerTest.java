package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.BooleanValue;
import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.value.BinaryExpression;
import com.bossymr.rapid.language.flow.value.BinaryOperator;
import com.bossymr.rapid.language.flow.value.ConstantValue;
import com.bossymr.rapid.language.flow.value.Expression;
import org.junit.jupiter.api.Test;

class ConditionAnalyzerTest {

    @Test
    void testCondition() {
        DataFlowState state = DataFlowState.createState(DataFlowStateTest.getEmptyFunctionBlock());
        VariableSnapshot integerSnapshot = new VariableSnapshot(RapidPrimitiveType.NUMBER);
        VariableSnapshot booleanSnapshot = new VariableSnapshot(RapidPrimitiveType.BOOLEAN);
        state.assign(new Condition(integerSnapshot, ConditionType.EQUALITY, Expression.of(0)), true);
        ConditionAnalyzer conditionAnalyzer = new ConditionAnalyzer(state);
        Condition condition = new Condition(booleanSnapshot, ConditionType.EQUALITY, new BinaryExpression(BinaryOperator.EQUAL_TO, integerSnapshot, ConstantValue.of(0)));
        BooleanValue value = condition.accept(conditionAnalyzer);
        System.out.println(value);
    }
}

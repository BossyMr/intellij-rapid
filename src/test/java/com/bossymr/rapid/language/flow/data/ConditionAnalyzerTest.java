package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.BooleanValue;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.value.BinaryExpression;
import com.bossymr.rapid.language.flow.value.BinaryOperator;
import com.bossymr.rapid.language.flow.value.ConstantExpression;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.virtual.VirtualRoutine;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ConditionAnalyzerTest {

    public static @NotNull Block.FunctionBlock getEmptyFunctionBlock() {
        return new Block.FunctionBlock(new VirtualRoutine(RoutineType.PROCEDURE, "bar", null, List.of()), "foo");
    }

    @Test
    void testCondition() {
        /*
         * x := 0
         * z := x == 0
         * z -> true
         */
        DataFlowState state = DataFlowState.createState(getEmptyFunctionBlock());
        VariableSnapshot x = new VariableSnapshot(RapidPrimitiveType.NUMBER);
        VariableSnapshot z = new VariableSnapshot(RapidPrimitiveType.BOOLEAN);
        state.add(new BinaryExpression(BinaryOperator.EQUAL_TO, x, new ConstantExpression(RapidPrimitiveType.NUMBER, 0)));
        BinaryExpression expression = new BinaryExpression(BinaryOperator.EQUAL_TO, x, new ConstantExpression(RapidPrimitiveType.NUMBER, 0));
        Expression condition = new BinaryExpression(BinaryOperator.EQUAL_TO, z, expression);
        BooleanValue value = state.getConstraint(condition);
        Assertions.assertEquals(BooleanValue.ALWAYS_TRUE, value);
    }
}

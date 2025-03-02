package com.bossymr.flow;

import com.bossymr.flow.constraint.Constraint;
import com.bossymr.flow.expression.BinaryExpression;
import com.bossymr.flow.expression.LiteralExpression;
import com.bossymr.flow.state.FlowEngine;
import com.bossymr.flow.state.FlowMethod;
import com.bossymr.flow.state.FlowSnapshot;
import com.bossymr.flow.state.Variable;
import com.bossymr.flow.type.ValueType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ReturnValueTest {

    @Test
    void returnConstant() {
        Instruction.Label label = new Instruction.Label();
        Variable variable = new Variable("variable", ValueType.booleanType());
        Method method = new Method("foo", ValueType.emptyType(), List.of(), codeBuilder -> codeBuilder
                .pushInteger(1)
                .pushInteger(1)
                .add()
                .pushInteger(2)
                .equalTo()
                .insertLabel(label)
                .assign(variable)
                .returnValue());
        FlowEngine engine = new FlowEngine();
        FlowMethod dataFlow = engine.getMethod(method);
        List<FlowSnapshot> snapshots = dataFlow.afterElement(label);
        Assertions.assertEquals(1, snapshots.size());
        FlowSnapshot snapshot = snapshots.getFirst();
        Constraint constraint = snapshot.compute(new BinaryExpression(BinaryExpression.Operator.EQUAL_TO, variable, LiteralExpression.booleanLiteral(true)));
        Assertions.assertEquals(Constraint.ALWAYS_TRUE, constraint);
    }
}

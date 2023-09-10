package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.condition.ConditionType;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.psi.StatementListType;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.virtual.VirtualRoutine;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

class DataFlowBlockTest {

    @Test
    void isCyclicDirect() {
        /*
         * a := a + 1 -> true
         */
        DataFlowBlock block = createBlock();
        Block functionBlock = block.getBasicBlock().getBlock();
        VariableValue variable = new VariableValue(functionBlock.createVariable(null, null, RapidPrimitiveType.NUMBER));
        DataFlowState state = DataFlowState.createState(block);
        block.getStates().add(state);
        Assertions.assertTrue(block.isCyclic(state, new Condition(variable, ConditionType.EQUALITY, new BinaryExpression(BinaryOperator.ADD, variable, ConstantValue.of(1)))));
    }

    @Test
    void isCyclicIndirect() {
        /*
         * a := b + 1
         * b := a -> true
         */
        DataFlowBlock block = createBlock();
        Block functionBlock = block.getBasicBlock().getBlock();
        VariableValue variableA = new VariableValue(functionBlock.createVariable(null, null, RapidPrimitiveType.NUMBER));
        VariableValue variableB = new VariableValue(functionBlock.createVariable(null, null, RapidPrimitiveType.NUMBER));
        DataFlowState state = DataFlowState.createState(block);
        block.getStates().add(state);
        state.assign(new Condition(variableA, ConditionType.EQUALITY, new BinaryExpression(BinaryOperator.ADD, variableB, ConstantValue.of(1))), true);
        Assertions.assertTrue(block.isCyclic(state, new Condition(variableB, ConditionType.EQUALITY, new ValueExpression(variableA))));
    }

    private @NotNull DataFlowBlock createBlock() {
        VirtualRoutine routine = new VirtualRoutine(RoutineType.PROCEDURE, "bar", null, List.of());
        Block.FunctionBlock functionBlock = new Block.FunctionBlock(routine, "foo");
        BasicBlock.EntryBasicBlock basicBlock = new BasicBlock.EntryBasicBlock(functionBlock, StatementListType.STATEMENT_LIST);
        HashSet<BlockCycle> cycles = new HashSet<>();
        DataFlowBlock block = new DataFlowBlock(basicBlock, cycles);
        cycles.add(new BlockCycle(List.of(block)));
        return block;
    }
}

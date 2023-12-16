package com.bossymr.rapid.language.flow.data.block;

import com.bossymr.rapid.language.flow.BooleanValue;
import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.flow.value.SnapshotExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DataFlowBlock {

    private final @NotNull Instruction instruction;
    private final @NotNull List<DataFlowState> states = new ArrayList<>();

    public DataFlowBlock(@NotNull Instruction instruction) {
        this.instruction = instruction;
    }

    public @NotNull Instruction getInstruction() {
        return instruction;
    }

    public @NotNull List<DataFlowState> getStates() {
        return states;
    }

    public @NotNull Optionality getOptionality(@NotNull ReferenceExpression variable) {
        Optionality optionality = null;
        for (DataFlowState state : states) {
            SnapshotExpression snapshot = state.getSnapshot(variable);
            ReferenceExpression expression = snapshot != null ? snapshot : variable;
            if (optionality == null) {
                optionality = state.getOptionality(expression);
            } else {
                Optionality result = state.getOptionality(expression);
                optionality = optionality.or(result);
            }
        }
        if (optionality == null) {
            return Optionality.NO_VALUE;
        }
        return optionality;
    }

    public @NotNull BooleanValue getConstraint(@NotNull Expression expression) {
        BooleanValue booleanValue = BooleanValue.NO_VALUE;
        for (DataFlowState state : states) {
            booleanValue = booleanValue.or(state.getConstraint(expression));
        }
        return booleanValue;
    }

    @Override
    public String toString() {
        return "DataFlowBlock{" +
                "index=" + instruction.getIndex() +
                ", basicBlock=" + instruction +
                ", states=" + states +
                '}';
    }
}

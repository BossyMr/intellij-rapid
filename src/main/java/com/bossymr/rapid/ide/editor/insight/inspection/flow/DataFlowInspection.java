package com.bossymr.rapid.ide.editor.insight.inspection.flow;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlowBlock;
import com.bossymr.rapid.language.flow.ControlFlowService;
import com.bossymr.rapid.language.flow.data.DataFlowState;
import com.bossymr.rapid.language.flow.expression.Expression;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.codeInspection.LocalInspectionTool;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class DataFlowInspection extends LocalInspectionTool {

    protected @NotNull Map<DataFlowState, Expression> getExpressions(@NotNull RapidExpression expression) {
        PhysicalRoutine routine = PhysicalRoutine.getRoutine(expression);
        if (routine == null) {
            return Map.of();
        }
        ControlFlowBlock block = ControlFlowService.getInstance().getDataFlow(routine);
        return getExpressions(expression, block);
    }

    private @NotNull Map<DataFlowState, Expression> getExpressions(@NotNull RapidExpression expression, @NotNull ControlFlowBlock block) {
        Map<DataFlowState, Expression> states = new HashMap<>();
        Block functionBlock = block.getControlFlow();
        for (Instruction instruction : functionBlock.getInstructions()) {
            for (DataFlowState state : block.getDataFlow(instruction)) {
                Expression component = state.getExpression(expression);
                if (component != null) {
                    states.put(state, component);
                }
            }
        }
        return states;
    }
}

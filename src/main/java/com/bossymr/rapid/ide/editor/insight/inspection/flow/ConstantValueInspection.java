package com.bossymr.rapid.ide.editor.insight.inspection.flow;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.Snapshot;
import com.bossymr.rapid.language.flow.expression.SnapshotExpression;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantValueInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new RapidElementVisitor() {
            @Override
            public void visitIfStatement(@NotNull RapidIfStatement statement) {
                RapidExpression condition = statement.getCondition();
                if (condition == null) {
                    return;
                }
                Map<DataFlowState, Snapshot> expressions = getExpressions(condition);
                registerValue(condition, expressions, holder);
            }

            @Override
            public void visitReferenceExpression(@NotNull RapidReferenceExpression expression) {
                Map<DataFlowState, Snapshot> expressions = getExpressions(expression);
                registerOptionality(expression, expressions, holder);
            }

            @Override
            public void visitIndexExpression(@NotNull RapidIndexExpression expression) {
                Map<DataFlowState, Snapshot> expressions = getExpressions(expression);
            }
        };
    }

    private void registerOptionality(@NotNull RapidReferenceExpression element, @NotNull Map<DataFlowState, Snapshot> expressions, @NotNull ProblemsHolder holder) {
        Optionality optionality = Optionality.NO_VALUE;
        for (DataFlowState state : expressions.keySet()) {
            SnapshotExpression snapshot = new SnapshotExpression(expressions.get(state));
            optionality = optionality.or(state.getOptionality(snapshot));
        }
        if (optionality == Optionality.MISSING) {
            holder.registerProblem(element, RapidBundle.message("inspection.message.missing.variable", element.getText()));
        }
        if (optionality == Optionality.UNKNOWN) {
            holder.registerProblem(element, RapidBundle.message("inspection.message.unknown.variable", element.getText()));
        }
    }

    private void registerValue(@NotNull RapidExpression element, @NotNull Map<DataFlowState, Snapshot> expressions, @NotNull ProblemsHolder holder) {
        Constraint value = Constraint.NO_VALUE;
        for (DataFlowState state : expressions.keySet()) {
            List<DataFlowState> successors = state.getSuccessors();
            if (successors.isEmpty()) {
                value = value.or(Constraint.NO_VALUE);
            } else if (successors.size() == 1) {
                SnapshotExpression expression = new SnapshotExpression(expressions.get(state));
                value = value.or(state.getConstraint(expression));
            } else if (successors.size() == 2) {
                value = value.or(Constraint.ANY_VALUE);
            }
        }
        if (value == Constraint.ALWAYS_TRUE) {
            holder.registerProblem(element, RapidBundle.message("inspection.message.constant.expression", "true"));
        }
        if (value == Constraint.ALWAYS_FALSE) {
            holder.registerProblem(element, RapidBundle.message("inspection.message.constant.expression", "false"));
        }
    }

    private @NotNull Map<DataFlowState, Snapshot> getExpressions(@NotNull RapidExpression expression) {
        PhysicalRoutine routine = PhysicalRoutine.getRoutine(expression);
        if (routine == null) {
            return Map.of();
        }
        ControlFlowBlock block = ControlFlowService.getInstance().getDataFlow(routine);
        return getExpressions(expression, block);
    }

    private @NotNull Map<DataFlowState, Snapshot> getExpressions(@NotNull RapidExpression expression, @NotNull ControlFlowBlock block) {
        Map<DataFlowState, Snapshot> states = new HashMap<>();
        Block functionBlock = block.getControlFlow();
        for (Instruction instruction : functionBlock.getInstructions()) {
            for (DataFlowState state : block.getDataFlow(instruction)) {
                Snapshot snapshot = state.getSnapshot(expression);
                if (snapshot != null) {
                    states.put(state, snapshot);
                }
            }
        }
        return states;
    }

}

package com.bossymr.rapid.ide.editor.insight.inspection.flow;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.DataFlow;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.Snapshot;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.value.SnapshotExpression;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidIfStatement;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConstantValueInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new RapidElementVisitor() {
            @Override
            public void visitExpression(@NotNull RapidExpression expression) {
                PhysicalRoutine routine = PhysicalRoutine.getRoutine(expression);
                if (routine == null) {
                    return;
                }
                DataFlow dataFlow = ControlFlowService.getInstance().getDataFlow(expression);
                Block.FunctionBlock functionBlock = getBlock(routine, dataFlow.getControlFlow());
                if (functionBlock == null) {
                    return;
                }
                Map<DataFlowState, Snapshot> expressions = getExpressions(expression, functionBlock, dataFlow);
                if (expressions.isEmpty()) {
                    return;
                }
                registerOptionality(expression, expressions, holder);
                registerValue(expression, expressions, holder);
            }
        };
    }

    private void registerOptionality(@NotNull RapidExpression element, @NotNull Map<DataFlowState, Snapshot> expressions, @NotNull ProblemsHolder holder) {
        if (!(element instanceof RapidReferenceExpression)) {
            return;
        }
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
        PsiElement parent = element.getParent();
        if (!(parent instanceof RapidIfStatement)) {
            return;
        }
        BooleanValue value = BooleanValue.NO_VALUE;
        for (DataFlowState state : expressions.keySet()) {
            List<DataFlowState> successors = state.getSuccessors();
            if (successors.isEmpty()) {
                value = value.or(BooleanValue.NO_VALUE);
            } else if (successors.size() == 1) {
                SnapshotExpression expression = new SnapshotExpression(expressions.get(state));
                value = value.or(state.getConstraint(expression));
            } else if (successors.size() == 2) {
                value = value.or(BooleanValue.ANY_VALUE);
            }
        }
        if (value == BooleanValue.ALWAYS_TRUE) {
            holder.registerProblem(element, RapidBundle.message("inspection.message.constant.expression", "true"));
        }
        if (value == BooleanValue.ALWAYS_FALSE) {
            holder.registerProblem(element, RapidBundle.message("inspection.message.constant.expression", "false"));
        }
    }

    private @NotNull Map<DataFlowState, Snapshot> getExpressions(@NotNull RapidExpression expression, @NotNull Block functionBlock, @NotNull DataFlow dataFlow) {
        Map<DataFlowState, Snapshot> states = new HashMap<>();
        for (Instruction instruction : functionBlock.getInstructions()) {
            DataFlowBlock block = dataFlow.getBlock(instruction);
            if (block == null) {
                continue;
            }
            for (DataFlowState state : block.getStates()) {
                Snapshot snapshot = state.getSnapshot(expression);
                if (snapshot != null) {
                    states.put(state, snapshot);
                }
            }
        }
        return states;
    }

    private @Nullable Block.FunctionBlock getBlock(@NotNull PhysicalRoutine routine, @NotNull ControlFlow controlFlow) {
        for (Block block : controlFlow.getBlocks()) {
            if (!(block instanceof Block.FunctionBlock functionBlock)) {
                continue;
            }
            RapidSymbol element = block.getElement();
            if (Objects.equals(element.getCanonicalName(), routine.getCanonicalName())) {
                return functionBlock;
            }
        }
        return null;
    }
}

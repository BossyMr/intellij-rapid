package com.bossymr.rapid.ide.editor.insight.inspection.flow;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.Snapshot;
import com.bossymr.rapid.language.flow.expression.*;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
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
                Map<DataFlowState, Expression> expressions = getExpressions(condition);
                registerValue(condition, expressions, holder);
            }

            @Override
            public void visitReferenceExpression(@NotNull RapidReferenceExpression expression) {
                Map<DataFlowState, Expression> expressions = getExpressions(expression);
                registerOptionality(expression, expressions, holder);
            }

            @Override
            public void visitIndexExpression(@NotNull RapidIndexExpression expression) {
                Map<DataFlowState, Expression> expressions = getExpressions(expression.getExpression());
                registerIndex(expression, expressions, holder);
            }
        };
    }

    private void registerOptionality(@NotNull RapidReferenceExpression element, @NotNull Map<DataFlowState, Expression> expressions, @NotNull ProblemsHolder holder) {
        Optionality optionality = Optionality.NO_VALUE;
        for (DataFlowState state : expressions.keySet()) {
            optionality = optionality.or(state.getOptionality(expressions.get(state)));
        }
        if (optionality == Optionality.MISSING) {
            holder.registerProblem(element, RapidBundle.message("inspection.message.missing.variable", element.getText()));
        }
        if (optionality == Optionality.UNKNOWN) {
            holder.registerProblem(element, RapidBundle.message("inspection.message.unknown.variable", element.getText()));
        }
    }

    private void registerValue(@NotNull RapidExpression element, @NotNull Map<DataFlowState, Expression> expressions, @NotNull ProblemsHolder holder) {
        Constraint value = Constraint.NO_VALUE;
        for (DataFlowState state : expressions.keySet()) {
            List<DataFlowState> successors = state.getSuccessors();
            if (successors.isEmpty()) {
                value = value.or(Constraint.NO_VALUE);
            } else if (successors.size() == 1) {
                value = value.or(state.getConstraint(expressions.get(state)));
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

    private @NotNull Map<DataFlowState, Expression> getExpressions(@NotNull RapidExpression expression) {
        PhysicalRoutine routine = PhysicalRoutine.getRoutine(expression);
        if (routine == null) {
            return Map.of();
        }
        ControlFlowBlock block = ControlFlowService.getInstance().getDataFlow(routine);
        return getExpressions(expression, block);
    }

    private void registerIndex(@NotNull RapidIndexExpression element, @NotNull Map<DataFlowState, Expression> expressions, @NotNull ProblemsHolder holder) {
        for (DataFlowState state : expressions.keySet()) {
            Expression expression = expressions.get(state);
            if (!(expression instanceof ReferenceExpression referenceExpression)) {
                continue;
            }
            List<RapidExpression> array = element.getArray().getDimensions();
            Snapshot snapshot = state.getSnapshot(referenceExpression).getSnapshot();
            List<RapidExpression> dimensions = element.getArray().getDimensions();
            for (int i = dimensions.size() - 1; i >= 0; i--) {
                RapidExpression dimension = dimensions.get(i);
                Expression index = state.getExpression(array.get(i));
                if (index == null) {
                    break;
                }
                BinaryExpression lowerBound = new BinaryExpression(BinaryOperator.GREATER_THAN_OR_EQUAL, index, new LiteralExpression(1));
                BinaryExpression upperBound = new BinaryExpression(BinaryOperator.LESS_THAN_OR_EQUAL, index, FunctionCallExpression.length(snapshot, new LiteralExpression(i + 1)));
                BinaryExpression integerType = new BinaryExpression(BinaryOperator.EQUAL_TO, new BinaryExpression(BinaryOperator.INTEGER_DIVIDE, index, new LiteralExpression(1)), index);
                Constraint constraint = state.getConstraint(new BinaryExpression(BinaryOperator.AND, integerType, new BinaryExpression(BinaryOperator.AND, lowerBound, upperBound)));
                if (constraint == Constraint.ALWAYS_FALSE) {
                    ProblemHighlightType highlightType = dimension instanceof RapidLiteralExpression ? ProblemHighlightType.ERROR : ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
                    holder.registerProblem(dimension, RapidBundle.message("inspection.message.out.of.bounds"), highlightType);
                }
            }
        }
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

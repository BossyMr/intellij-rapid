package com.bossymr.rapid.ide.editor.insight.inspection.flow;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.editor.insight.fix.SurroundWithIfStatementFix;
import com.bossymr.rapid.language.flow.Constraint;
import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.flow.data.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.Snapshot;
import com.bossymr.rapid.language.flow.expression.*;
import com.bossymr.rapid.language.psi.*;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.options.OptPane;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.codeInspection.options.OptPane.checkbox;
import static com.intellij.codeInspection.options.OptPane.pane;

public class DataFlowProblemInspection extends DataFlowInspection {

    public boolean reportPossibleProblem = true;

    @Override
    public @NotNull OptPane getOptionsPane() {
        return pane(checkbox("reportPossibleProblem", RapidBundle.message("inspection.option.report.possible.problem")));
    }

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new RapidElementVisitor() {
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
        if (reportPossibleProblem)
            if (optionality == Optionality.UNKNOWN) {
                SurroundWithIfStatementFix quickFix = new SurroundWithIfStatementFix("Present(" + element.getText() + ")");
                holder.registerProblem(element, RapidBundle.message("inspection.message.unknown.variable", element.getText()), quickFix);
            }
    }

    private void registerIndex(@NotNull RapidIndexExpression element, @NotNull Map<DataFlowState, Expression> expressions, @NotNull ProblemsHolder holder) {
        Map<RapidExpression, Constraint> constraints = new HashMap<>();
        List<RapidExpression> dimensions = element.getArray().getDimensions();
        for (DataFlowState state : expressions.keySet()) {
            Expression expression = expressions.get(state);
            if (!(expression instanceof ReferenceExpression referenceExpression)) {
                continue;
            }
            Snapshot snapshot = state.getSnapshot(referenceExpression).getSnapshot();
            for (int i = dimensions.size() - 1; i >= 0; i--) {
                RapidExpression dimension = dimensions.get(i);
                Expression index = state.getExpression(dimension);
                if (index == null) {
                    break;
                }
                BinaryExpression lowerBound = new BinaryExpression(BinaryOperator.GREATER_THAN_OR_EQUAL, index, new LiteralExpression(1));
                BinaryExpression upperBound = new BinaryExpression(BinaryOperator.LESS_THAN_OR_EQUAL, index, FunctionCallExpression.length(snapshot, new LiteralExpression(i + 1)));
                BinaryExpression integerType = new BinaryExpression(BinaryOperator.EQUAL_TO, new BinaryExpression(BinaryOperator.INTEGER_DIVIDE, index, new LiteralExpression(1)), index);
                Constraint constraint = state.getConstraint(new BinaryExpression(BinaryOperator.AND, integerType, new BinaryExpression(BinaryOperator.AND, lowerBound, upperBound)));
                if (constraint != Constraint.ANY_VALUE) {
                    if (constraints.containsKey(dimension)) {
                        constraints.put(dimension, constraint.or(constraints.get(dimension)));
                    } else {
                        constraints.put(dimension, constraint);
                    }
                }
            }
        }
        for (RapidExpression dimension : dimensions) {
            Constraint constraint = constraints.getOrDefault(dimension, Constraint.ALWAYS_TRUE);
            ProblemHighlightType highlightType = dimension instanceof RapidLiteralExpression ? ProblemHighlightType.ERROR : ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
            if (constraint == Constraint.ALWAYS_FALSE) {
                holder.registerProblem(dimension, RapidBundle.message("inspection.message.out.of.bounds"), highlightType);
            }
            if (reportPossibleProblem) {
                if (constraint == Constraint.ANY_VALUE) {
                    int depth = dimensions.indexOf(dimension) + 1;
                    String variable = element.getExpression().getText();
                    String index = dimension.getText();
                    SurroundWithIfStatementFix quickFix = new SurroundWithIfStatementFix("Dim(" + variable + ", " + depth + ") >= " + index);
                    holder.registerProblem(dimension, RapidBundle.message("inspection.message.out.of.bounds"), highlightType, quickFix);
                }
            }
        }
    }
}

package com.bossymr.rapid.ide.editor.insight.inspection.flow;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.editor.insight.fix.RemoveElementFix;
import com.bossymr.rapid.ide.editor.insight.fix.UnwrapIfStatementFix;
import com.bossymr.rapid.language.flow.Constraint;
import com.bossymr.rapid.language.flow.data.DataFlowState;
import com.bossymr.rapid.language.flow.expression.Expression;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidIfStatement;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.modcommand.ModCommandService;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class ConstantValueInspection extends DataFlowInspection {

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
                registerValue(statement, condition, expressions, holder);
            }
        };
    }

    private void registerValue(@NotNull RapidIfStatement statement, @NotNull RapidExpression element, @NotNull Map<DataFlowState, Expression> expressions, @NotNull ProblemsHolder holder) {
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
        ModCommandService service = ModCommandService.getInstance();
        if (value == Constraint.ALWAYS_TRUE) {
            holder.registerProblem(element, RapidBundle.message("inspection.message.constant.expression", "true"), service.wrapToQuickFix(new UnwrapIfStatementFix(statement)));
        }
        if (value == Constraint.ALWAYS_FALSE) {
            holder.registerProblem(element, RapidBundle.message("inspection.message.constant.expression", "false"), service.wrapToQuickFix(new RemoveElementFix(statement, RapidBundle.message("quick.fix.family.remove.if.statement"))));
        }
    }
}

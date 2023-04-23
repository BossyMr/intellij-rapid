package com.bossymr.rapid.ide.inspection;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.insight.quickfix.SafeDeleteFix;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidTargetVariable;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;

public class UnusedDeclarationInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new RapidElementVisitor() {
            @Override
            public void visitSymbol(@NotNull PhysicalSymbol symbol) {
                if (symbol instanceof PhysicalModule || symbol instanceof RapidTargetVariable) return;
                if (symbol instanceof PhysicalRoutine routine) {
                    if (routine.getRoutineType().equals(RoutineType.PROCEDURE) && "Main".equals(routine.getName())) {
                        return;
                    }
                }
                String name = symbol.getName();
                PsiElement nameIdentifier = symbol.getNameIdentifier();
                if (nameIdentifier == null || name == null) return;
                Query<PsiReference> query = ReferencesSearch.search(symbol);
                if (query.findFirst() != null) return;
                holder.registerProblem(symbol, RapidBundle.message("inspection.message.unused.declaration", name), ProblemHighlightType.LIKE_UNUSED_SYMBOL, nameIdentifier.getTextRangeInParent(), new SafeDeleteFix(symbol));
                super.visitSymbol(symbol);
            }
        };
    }
}

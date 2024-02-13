package com.bossymr.rapid.ide.editor.insight.inspection;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.editor.insight.fix.SafeDeleteFix;
import com.bossymr.rapid.ide.search.RapidSymbolUsageSearcher;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.find.usages.api.PsiUsage;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;

public class UnusedDeclarationInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new RapidElementVisitor() {
            @SuppressWarnings("UnstableApiUsage")
            @Override
            public void visitSymbol(@NotNull PhysicalSymbol symbol) {
                if (symbol instanceof PhysicalModule) {
                    return;
                }
                if (isEntryPoint(symbol)) {
                    return;
                }
                String name = symbol.getName();
                PsiElement nameIdentifier = symbol.getNameIdentifier();
                if (nameIdentifier == null || name == null) return;
                Query<? extends PsiUsage> query = RapidSymbolUsageSearcher.collectSearchRequests(symbol, name, symbol.getProject(), symbol.getUseScope());
                if (query.findFirst() != null) return;
                holder.registerProblem(symbol, RapidBundle.message("inspection.message.unused.declaration", name), ProblemHighlightType.LIKE_UNUSED_SYMBOL, nameIdentifier.getTextRangeInParent(), new SafeDeleteFix(symbol));
            }
        };
    }

    private boolean isEntryPoint(@NotNull PhysicalSymbol symbol) {
        if (!(symbol instanceof PhysicalRoutine routine)) {
            return false;
        }
        RoutineType routineType = routine.getRoutineType();
        if (routineType != RoutineType.PROCEDURE) {
            return false;
        }
        String name = routine.getName();
        if (name == null) {
            return false;
        }
        return name.equalsIgnoreCase("main");
    }
}

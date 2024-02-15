package com.bossymr.rapid.ide.editor.insight.inspection;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.editor.insight.fix.SafeDeleteFix;
import com.bossymr.rapid.ide.search.RapidSymbolUsageSearcher;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.find.usages.api.PsiUsage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.SearchScope;
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
                SearchScope searchScope = getSearchScope(symbol, symbol.getProject(), symbol.getUseScope());
                Query<? extends PsiUsage> query = RapidSymbolUsageSearcher.collectSearchRequests(symbol, name, symbol.getProject(), searchScope)
                                                                          .filtering(usage -> !(usage.getDeclaration()));
                if (query.findFirst() != null) return;
                holder.registerProblem(symbol, RapidBundle.message("inspection.message.unused.declaration", name), ProblemHighlightType.LIKE_UNUSED_SYMBOL, nameIdentifier.getTextRangeInParent(), new SafeDeleteFix(symbol));
            }
        };
    }

    private @NotNull SearchScope getSearchScope(@NotNull PhysicalSymbol symbol, @NotNull Project project, @NotNull SearchScope searchScope) {
        RapidRobot robot = RobotService.getInstance().getRobot();
        if (robot != null) {
            VirtualFile file = symbol.getContainingFile().getViewProvider().getVirtualFile();
            SearchScope robotSearchScope = robot.getSearchScope(project);
            if (robotSearchScope.contains(file)) {
                return robotSearchScope;
            }
        }
        return searchScope;
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

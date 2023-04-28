package com.bossymr.rapid.ide.hierarchy;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RapidCalleeTreeStructure extends HierarchyTreeStructure {

    public RapidCalleeTreeStructure(@NotNull Project project, @NotNull PhysicalSymbol symbol) {
        super(project, new RapidCallHierarchyNodeDescriptor(project, null, symbol, true));
    }

    protected @NotNull Project getProject() {
        return myProject;
    }

    @Override
    protected Object @NotNull [] buildChildren(@NotNull HierarchyNodeDescriptor descriptor) {
        if (!(descriptor instanceof RapidCallHierarchyNodeDescriptor nodeDescriptor)) {
            throw new IllegalArgumentException();
        }
        RapidSymbol element = nodeDescriptor.getContextElement();
        if (!(element instanceof PhysicalRoutine routine)) {
            return new Object[0];
        }
        if (nodeDescriptor.isRecursive(routine)) {
            return new Object[0];
        }
        List<RapidRoutine> routines = new ArrayList<>();
        routine.accept(new RapidRecursiveElementWalkingVisitor() {
            @Override
            public void visitFunctionCallExpression(@NotNull RapidFunctionCallExpression expression) {
                RapidReferenceExpression referenceExpression = expression.getReferenceExpression();
                RapidSymbol symbol = referenceExpression.getSymbol();
                if (symbol instanceof RapidRoutine target) {
                    routines.add(target);
                }
                super.visitFunctionCallExpression(expression);
            }

            @Override
            public void visitProcedureCallStatement(@NotNull RapidProcedureCallStatement statement) {
                RapidExpression expression = statement.getReferenceExpression();
                if (expression instanceof RapidReferenceExpression referenceExpression) {
                    RapidSymbol symbol = referenceExpression.getSymbol();
                    if (symbol instanceof RapidRoutine target) {
                        routines.add(target);
                    }
                }
                super.visitProcedureCallStatement(statement);
            }
        });
        return routines.stream()
                .map(result -> {
                    if (result instanceof PsiElement target) {
                        return target;
                    } else {
                        // The Call Hierarchy API requires a PsiElement.
                        return new RapidFakeVirtualSymbol(PsiManager.getInstance(getProject()), result);
                    }
                })
                .distinct()
                .map(result -> new RapidCallHierarchyNodeDescriptor(getProject(), nodeDescriptor, result, false))
                .toArray();
    }
}

package com.bossymr.rapid.ide.editor.insight.surrounder;

import com.bossymr.rapid.language.psi.RapidElementFactory;
import com.bossymr.rapid.language.psi.RapidStatementList;
import com.intellij.lang.ASTNode;
import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class RapidStatementSurrounder implements Surrounder {

    @Override
    public boolean isApplicable(PsiElement @NotNull [] elements) {
        return true;
    }

    @Override
    public @Nullable TextRange surroundElements(@NotNull Project project, @NotNull Editor editor, PsiElement @NotNull [] elements) throws IncorrectOperationException {
        PsiElement element = surroundElements(project, elements, "");
        ASTNode node = element.getNode();
        ASTNode keyword = node.getFirstChildNode();
        assert keyword != null;
        int offset = keyword.getStartOffset() + keyword.getTextLength() + 1;
        return TextRange.create(offset, offset);
    }

    protected @NotNull PsiElement surroundElements(@NotNull Project project, PsiElement @NotNull [] elements, @NotNull String condition)  {
        PsiElement parent = elements[0].getParent();
        PsiElement block = createElement(project, condition);
        RapidStatementList statementList = PsiTreeUtil.getChildOfType(block, RapidStatementList.class);
        assert statementList != null;
        statementList.addRange(elements[0], elements[elements.length - 1]);
        PsiElement element = parent.addBefore(block, elements[0]);
        parent.deleteChildRange(elements[0], elements[elements.length - 1]);
        return element;
    }

    private @NotNull PsiElement createElement(@NotNull Project project, @NotNull String condition) {
        RapidElementFactory factory = RapidElementFactory.getInstance(project);
        return factory.createStatementFromText(getPrefix() + " " + condition + " " + getSuffix());
    }

    protected abstract @NotNull String getPrefix();

    protected abstract @NotNull String getSuffix();
}

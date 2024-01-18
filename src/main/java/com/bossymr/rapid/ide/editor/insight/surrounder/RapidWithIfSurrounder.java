package com.bossymr.rapid.ide.editor.insight.surrounder;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidElementFactory;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidIfStatement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidWithIfSurrounder extends RapidStatementSurrounder {
    @Override
    public String getTemplateDescription() {
        return RapidBundle.message("surrounder.with.if");
    }

    @Override
    public @Nullable TextRange surroundElements(@NotNull Project project, @NotNull Editor editor, PsiElement @NotNull [] elements) throws IncorrectOperationException {
        PsiElement element = elements[0].getParent();
        if (element == null) {
            return null;
        }
        RapidIfStatement statement = surroundElements(project, element, elements, "");
        RapidExpression condition = statement.getCondition();
        if (condition == null) {
            return null;
        }
        return condition.getTextRange();
    }

    public @NotNull RapidIfStatement surroundElements(@NotNull Project project, @NotNull PsiElement element, PsiElement @NotNull [] elements, @NotNull String condition) {
        RapidElementFactory elementFactory = RapidElementFactory.getInstance(project);
        RapidIfStatement statement = (RapidIfStatement) elementFactory.createStatementFromText("IF " + condition + "THEN\nENDIF");
        element.addBefore(statement, elements[0]);
        Objects.requireNonNull(statement.getThenBranch()).addRange(elements[0], elements[elements.length - 1]);
        element.deleteChildRange(elements[0], elements[elements.length - 1]);
        return statement;
    }
}

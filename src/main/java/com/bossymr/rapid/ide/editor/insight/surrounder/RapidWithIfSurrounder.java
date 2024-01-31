package com.bossymr.rapid.ide.editor.insight.surrounder;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidElementFactory;
import com.bossymr.rapid.language.psi.RapidIfStatement;
import com.bossymr.rapid.language.psi.RapidStatementList;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.intellij.lang.ASTNode;
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
        // FIXME
        PsiElement element = elements[0].getParent();
        if (element == null) {
            return null;
        }
        RapidIfStatement statement = surroundElements(project, element, elements, "");
        ASTNode node = statement.getNode();
        ASTNode ifKeyword = node.findChildByType(RapidTokenTypes.IF_KEYWORD);
        Objects.requireNonNull(ifKeyword);
        int offset = ifKeyword.getStartOffset() + ifKeyword.getTextLength() + 1;
        return TextRange.create(offset, offset);
    }

    public @NotNull RapidIfStatement surroundElements(@NotNull Project project, @NotNull PsiElement parent, PsiElement @NotNull [] elements, @NotNull String condition) {
        RapidElementFactory elementFactory = RapidElementFactory.getInstance(project);
        RapidIfStatement statement = (RapidIfStatement) elementFactory.createStatementFromText("IF " + condition + " THEN\nENDIF");
        RapidStatementList thenBranch = statement.getThenBranch();
        Objects.requireNonNull(thenBranch);
        thenBranch.addRange(elements[0], elements[elements.length - 1]);
        PsiElement element = parent.addBefore(statement, elements[0]);
        parent.deleteChildRange(elements[0], elements[elements.length - 1]);
        return (RapidIfStatement) element;
    }
}

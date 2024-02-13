package com.bossymr.rapid.language.psi.impl.fragment;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidExpressionCodeFragment;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidExpressionCodeFragmentImpl extends RapidCodeFragmentImpl implements RapidExpressionCodeFragment {

    public RapidExpressionCodeFragmentImpl(@NotNull Project project,
                                           boolean isPhysical,
                                           @NotNull String fileName,
                                           @NotNull String text,
                                           @Nullable PsiElement context) {
        super(project, RapidElementTypes.EXPRESSION_TEXT, isPhysical, fileName, text, context);
    }

    @Override
    public @Nullable RapidExpression getExpression() {
        TokenSet tokenSet = TokenSet.orSet(RapidElementTypes.EXPRESSIONS, TokenSet.create(RapidElementTypes.ASSIGNMENT_STATEMENT));
        ASTNode node = calcTreeElement().findChildByType(tokenSet);
        if (node == null) return null;
        return node.getPsi(RapidExpression.class);
    }
}

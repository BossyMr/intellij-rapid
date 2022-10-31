package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LightTreeUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Visibility {
    LOCAL, TASK, GLOBAL;

    public static @NotNull Visibility getVisibility(@NotNull PsiElement element) {
        TokenSet tokenSet = TokenSet.create(RapidTokenTypes.LOCAL_KEYWORD, RapidTokenTypes.TASK_KEYWORD);
        ASTNode node = element.getNode().findChildByType(tokenSet);
        IElementType elementType = node != null ? node.getElementType() : null;
        return getVisibility(elementType);
    }

    public static @NotNull Visibility getVisibility(@NotNull LighterAST tree, @NotNull LighterASTNode node) {
        TokenSet tokenSet = TokenSet.create(RapidTokenTypes.LOCAL_KEYWORD, RapidTokenTypes.TASK_KEYWORD);
        LighterASTNode lighterASTNode = LightTreeUtil.firstChildOfType(tree, node, tokenSet);
        IElementType elementType = lighterASTNode != null ? lighterASTNode.getTokenType() : null;
        return getVisibility(elementType);
    }

    public static @NotNull Visibility getVisibility(@Nullable IElementType elementType) {
        if (elementType == null) {
            return Visibility.GLOBAL;
        }
        if (elementType == RapidTokenTypes.LOCAL_KEYWORD) {
            return Visibility.LOCAL;
        }
        if (elementType == RapidTokenTypes.TASK_KEYWORD) {
            return Visibility.TASK;
        }
        throw new AssertionError();
    }
}

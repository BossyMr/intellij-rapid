package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidBundle;
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
    LOCAL(RapidTokenTypes.LOCAL_KEYWORD, "LOCAL", RapidBundle.message("visibility.local")),
    TASK(RapidTokenTypes.TASK_KEYWORD, "GLOBAL", RapidBundle.message("visibility.task")),
    GLOBAL(RapidBundle.message("visibility.global"));

    public static final @NotNull TokenSet TOKEN_SET = TokenSet.create(RapidTokenTypes.LOCAL_KEYWORD, RapidTokenTypes.TASK_KEYWORD);

    private final IElementType elementType;
    private final String text, name;

    Visibility(@Nullable IElementType elementType, @Nullable String text, @NotNull String name) {
        this.elementType = elementType;
        this.text = text;
        this.name = name;
    }

    Visibility(@NotNull String name) {
        this(null, null, name);
    }

    public static @NotNull Visibility getVisibility(@NotNull PsiElement element) {
        ASTNode node = element.getNode().findChildByType(TOKEN_SET);
        IElementType elementType = node != null ? node.getElementType() : null;
        return getVisibility(elementType);
    }

    public static @NotNull Visibility getVisibility(@NotNull LighterAST tree, @NotNull LighterASTNode node) {
        LighterASTNode lighterASTNode = LightTreeUtil.firstChildOfType(tree, node, TOKEN_SET);
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

    public @Nullable IElementType getElementType() {
        return elementType;
    }

    public @Nullable String getText() {
        return text;
    }

    public @NotNull String getName() {
        return name;
    }
}

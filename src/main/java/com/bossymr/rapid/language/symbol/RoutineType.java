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

public enum RoutineType {
    FUNCTION(RapidTokenTypes.FUNC_KEYWORD, "FUNC"),
    PROCEDURE(RapidTokenTypes.PROC_KEYWORD, "PROC"),
    TRAP(RapidTokenTypes.TRAP_KEYWORD, "TRAP");

    private final IElementType elementType;
    private final String text;

    RoutineType(@NotNull IElementType elementType, @NotNull String text) {
        this.elementType = elementType;
        this.text = text;
    }

    public static @NotNull RoutineType getAttribute(@NotNull PsiElement element) {
        TokenSet tokenSet = TokenSet.create(RapidTokenTypes.FUNC_KEYWORD, RapidTokenTypes.PROC_KEYWORD, RapidTokenTypes.TRAP_KEYWORD);
        ASTNode node = element.getNode().findChildByType(tokenSet);
        if (node == null) {
            throw new IllegalArgumentException();
        }
        IElementType elementType = node.getElementType();
        return getAttribute(elementType);
    }

    public static @NotNull RoutineType getAttribute(@NotNull LighterAST tree, @NotNull LighterASTNode node) {
        TokenSet tokenSet = TokenSet.create(RapidTokenTypes.FUNC_KEYWORD, RapidTokenTypes.PROC_KEYWORD, RapidTokenTypes.TRAP_KEYWORD);
        LighterASTNode element = LightTreeUtil.firstChildOfType(tree, node, tokenSet);
        if (element == null) {
            throw new IllegalArgumentException();
        }
        IElementType elementType = element.getTokenType();
        return getAttribute(elementType);
    }

    public static @NotNull RoutineType getAttribute(@NotNull IElementType elementType) {
        if (elementType == RapidTokenTypes.FUNC_KEYWORD) {
            return RoutineType.FUNCTION;
        }
        if (elementType == RapidTokenTypes.PROC_KEYWORD) {
            return RoutineType.PROCEDURE;
        }
        if (elementType == RapidTokenTypes.TRAP_KEYWORD) {
            return RoutineType.TRAP;
        }
        throw new AssertionError();
    }

    public @NotNull IElementType getElementType() {
        return elementType;
    }

    public @NotNull String getText() {
        return text;
    }
}

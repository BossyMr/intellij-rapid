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

public enum RoutineType {
    FUNCTION(RapidTokenTypes.FUNC_KEYWORD, RapidTokenTypes.ENDFUNC_KEYWORD, "FUNC"),
    PROCEDURE(RapidTokenTypes.PROC_KEYWORD, RapidTokenTypes.ENDPROC_KEYWORD, "PROC"),
    TRAP(RapidTokenTypes.TRAP_KEYWORD, RapidTokenTypes.ENDTRAP_KEYWORD, "TRAP");

    private final @NotNull IElementType headType;
    private final @NotNull IElementType tailType;
    private final @NotNull String text;

    RoutineType(@NotNull IElementType elementType, @NotNull IElementType tailType, @NotNull String text) {
        this.headType = elementType;
        this.tailType = tailType;
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
        return headType;
    }

    public @NotNull IElementType getTailType() {
        return tailType;
    }

    public @NotNull String getText() {
        return text;
    }

    public @NotNull String getPresentableText() {
        return switch (this) {
            case FUNCTION -> RapidBundle.message("element.type.function");
            case PROCEDURE -> RapidBundle.message("element.type.procedure");
            case TRAP -> RapidBundle.message("element.type.trap");
        };
    }
}

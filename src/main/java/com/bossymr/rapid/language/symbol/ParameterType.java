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

public enum ParameterType {
    INPUT("INPUT"),

    VARIABLE(RapidTokenTypes.VAR_KEYWORD, "VAR"),

    PERSISTENT(RapidTokenTypes.PERS_KEYWORD, "PERS"),

    INOUT(RapidTokenTypes.INOUT_KEYWORD, "INOUT"),

    REFERENCE("REF");

    private final @Nullable IElementType elementType;
    private final @NotNull String text;

    ParameterType(@NotNull String text) {
        this(null, text);
    }

    ParameterType(@Nullable IElementType elementType, @NotNull String text) {
        this.elementType = elementType;
        this.text = text;
    }

    public @Nullable IElementType getElementType() {
        return elementType;
    }

    public @NotNull String getText() {
        return text;
    }

    public static @NotNull ParameterType getAttribute(@NotNull PsiElement element) {
        TokenSet tokenSet = TokenSet.create(RapidTokenTypes.VAR_KEYWORD, RapidTokenTypes.PERS_KEYWORD, RapidTokenTypes.INOUT_KEYWORD);
        ASTNode node = element.getNode().findChildByType(tokenSet);
        IElementType elementType = node != null ? node.getElementType() : null;
        return getAttribute(elementType);
    }

    public static @NotNull ParameterType getAttribute(@NotNull LighterAST tree, @NotNull LighterASTNode node) {
        TokenSet tokenSet = TokenSet.create(RapidTokenTypes.VAR_KEYWORD, RapidTokenTypes.PERS_KEYWORD, RapidTokenTypes.INOUT_KEYWORD);
        LighterASTNode element = LightTreeUtil.firstChildOfType(tree, node, tokenSet);
        IElementType elementType = element != null ? element.getTokenType() : null;
        return getAttribute(elementType);
    }

    public static @NotNull ParameterType getAttribute(@Nullable IElementType elementType) {
        if (elementType == null) {
            return ParameterType.INPUT;
        }
        if (elementType == RapidTokenTypes.VAR_KEYWORD) {
            return ParameterType.VARIABLE;
        }
        if (elementType == RapidTokenTypes.PERS_KEYWORD) {
            return ParameterType.PERSISTENT;
        }
        if (elementType == RapidTokenTypes.INOUT_KEYWORD) {
            return ParameterType.INOUT;
        }
        throw new AssertionError();
    }
}

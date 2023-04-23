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

public enum FieldType {

    /**
     * A variable is a modifiable value which forgets its value when the program is stopped.
     */
    VARIABLE(RapidTokenTypes.VAR_KEYWORD, "VAR"),

    /**
     * A constant cannot be modified and must be initialized to a constant.
     */
    CONSTANT(RapidTokenTypes.CONST_KEYWORD, "CONST"),

    /**
     * A persistent field is a modifiable value which persists its value when the program is stopped.
     */
    PERSISTENT(RapidTokenTypes.PERS_KEYWORD, "PERS");

    private final IElementType elementType;
    private final String text;

    FieldType(@NotNull IElementType elementType, @NotNull String text) {
        this.elementType = elementType;
        this.text = text;
    }

    public static @NotNull FieldType getAttribute(@NotNull PsiElement element) {
        TokenSet tokenSet = TokenSet.create(RapidTokenTypes.VAR_KEYWORD, RapidTokenTypes.CONST_KEYWORD, RapidTokenTypes.PERS_KEYWORD);
        ASTNode node = element.getNode().findChildByType(tokenSet);
        if (node == null) {
            throw new IllegalArgumentException();
        }
        IElementType elementType = node.getElementType();
        return getAttribute(elementType);
    }

    public static @NotNull FieldType getAttribute(@NotNull LighterAST tree, @NotNull LighterASTNode node) {
        TokenSet tokenSet = TokenSet.create(RapidTokenTypes.VAR_KEYWORD, RapidTokenTypes.CONST_KEYWORD, RapidTokenTypes.PERS_KEYWORD);
        LighterASTNode element = LightTreeUtil.firstChildOfType(tree, node, tokenSet);
        if (element == null) {
            throw new IllegalArgumentException();
        }
        IElementType elementType = element.getTokenType();
        return getAttribute(elementType);
    }

    private static @NotNull FieldType getAttribute(@NotNull IElementType elementType) {
        if (elementType == RapidTokenTypes.VAR_KEYWORD) {
            return FieldType.VARIABLE;
        }
        if (elementType == RapidTokenTypes.CONST_KEYWORD) {
            return FieldType.CONSTANT;
        }
        if (elementType == RapidTokenTypes.PERS_KEYWORD) {
            return FieldType.PERSISTENT;
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

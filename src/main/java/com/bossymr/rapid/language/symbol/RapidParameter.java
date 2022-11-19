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

public interface RapidParameter extends RapidVariable {

    @NotNull Attribute getAttribute();

    enum Attribute {
        INPUT, VARIABLE, PERSISTENT, INOUT, REFERENCE;

        public static @NotNull Attribute getAttribute(@NotNull PsiElement element) {
            TokenSet tokenSet = TokenSet.create(RapidTokenTypes.VAR_KEYWORD, RapidTokenTypes.PERS_KEYWORD, RapidTokenTypes.INOUT_KEYWORD);
            ASTNode node = element.getNode().findChildByType(tokenSet);
            IElementType elementType = node != null ? node.getElementType() : null;
            return getAttribute(elementType);
        }

        public static @NotNull Attribute getAttribute(@NotNull LighterAST tree, @NotNull LighterASTNode node) {
            TokenSet tokenSet = TokenSet.create(RapidTokenTypes.VAR_KEYWORD, RapidTokenTypes.PERS_KEYWORD, RapidTokenTypes.INOUT_KEYWORD);
            LighterASTNode element = LightTreeUtil.firstChildOfType(tree, node, tokenSet);
            IElementType elementType = element != null ? element.getTokenType() : null;
            return getAttribute(elementType);
        }

        public static @NotNull Attribute getAttribute(@Nullable IElementType elementType) {
            if (elementType == null) {
                return Attribute.INPUT;
            }
            if (elementType == RapidTokenTypes.VAR_KEYWORD) {
                return Attribute.VARIABLE;
            }
            if (elementType == RapidTokenTypes.PERS_KEYWORD) {
                return Attribute.PERSISTENT;
            }
            if (elementType == RapidTokenTypes.INOUT_KEYWORD) {
                return Attribute.INOUT;
            }
            throw new AssertionError();
        }
    }
}

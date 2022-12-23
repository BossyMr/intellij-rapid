package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.navigation.TargetPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LightTreeUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface RapidField extends RapidVariable, RapidAccessibleSymbol {
    @NotNull Attribute getAttribute();

    @Nullable RapidExpression getInitializer();

    default void setInitializer(@Nullable RapidExpression expression) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    boolean hasInitializer();

    @Override
    default @NotNull TargetPresentation getTargetPresentation() {
        return TargetPresentation.builder(Objects.requireNonNullElse(getName(), ""))
                .icon(switch (getAttribute()) {
                    case CONSTANT -> RapidIcons.CONSTANT;
                    case VARIABLE -> RapidIcons.VARIABLE;
                    case PERSISTENT -> RapidIcons.PERSISTENT;
                })
                .presentation();
    }

    enum Attribute {
        VARIABLE, CONSTANT, PERSISTENT;

        public static @NotNull Attribute getAttribute(@NotNull PsiElement element) {
            TokenSet tokenSet = TokenSet.create(RapidTokenTypes.VAR_KEYWORD, RapidTokenTypes.CONST_KEYWORD, RapidTokenTypes.PERS_KEYWORD);
            ASTNode node = element.getNode().findChildByType(tokenSet);
            if (node == null) {
                throw new IllegalArgumentException();
            }
            IElementType elementType = node.getElementType();
            return getAttribute(elementType);
        }

        public static @NotNull Attribute getAttribute(@NotNull LighterAST tree, @NotNull LighterASTNode node) {
            TokenSet tokenSet = TokenSet.create(RapidTokenTypes.VAR_KEYWORD, RapidTokenTypes.CONST_KEYWORD, RapidTokenTypes.PERS_KEYWORD);
            LighterASTNode element = LightTreeUtil.firstChildOfType(tree, node, tokenSet);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            IElementType elementType = element.getTokenType();
            return getAttribute(elementType);
        }

        private static @NotNull Attribute getAttribute(@NotNull IElementType elementType) {
            if (elementType == RapidTokenTypes.VAR_KEYWORD) {
                return Attribute.VARIABLE;
            }
            if (elementType == RapidTokenTypes.CONST_KEYWORD) {
                return Attribute.CONSTANT;
            }
            if (elementType == RapidTokenTypes.PERS_KEYWORD) {
                return Attribute.PERSISTENT;
            }
            throw new AssertionError();
        }
    }
}
package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.language.psi.RapidStatement;
import com.bossymr.rapid.language.psi.RapidStatementList;
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

import java.util.List;

public interface RapidRoutine extends RapidAccessibleSymbol {

    @NotNull Attribute getAttribute();

    @Nullable RapidType getType();

    @Nullable List<RapidParameterGroup> getParameters();

    @NotNull List<RapidField> getFields();

    @NotNull List<RapidStatement> getStatements();

    @Nullable List<RapidStatement> getStatements(@NotNull RapidStatementList.Attribute attribute);

    enum Attribute {
        FUNCTION, PROCEDURE, TRAP;

        public static @NotNull Attribute getAttribute(@NotNull PsiElement element) {
            TokenSet tokenSet = TokenSet.create(RapidTokenTypes.FUNC_KEYWORD, RapidTokenTypes.PROC_KEYWORD, RapidTokenTypes.TRAP_KEYWORD);
            ASTNode node = element.getNode().findChildByType(tokenSet);
            if (node == null) {
                throw new IllegalArgumentException();
            }
            IElementType elementType = node.getElementType();
            return getAttribute(elementType);
        }

        public static @NotNull Attribute getAttribute(@NotNull LighterAST tree, @NotNull LighterASTNode node) {
            TokenSet tokenSet = TokenSet.create(RapidTokenTypes.FUNC_KEYWORD, RapidTokenTypes.PROC_KEYWORD, RapidTokenTypes.TRAP_KEYWORD);
            LighterASTNode element = LightTreeUtil.firstChildOfType(tree, node, tokenSet);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            IElementType elementType = element.getTokenType();
            return getAttribute(elementType);
        }

        public static @NotNull Attribute getAttribute(@NotNull IElementType elementType) {
            if (elementType == RapidTokenTypes.FUNC_KEYWORD) {
                return Attribute.FUNCTION;
            }
            if (elementType == RapidTokenTypes.PROC_KEYWORD) {
                return Attribute.PROCEDURE;
            }
            if (elementType == RapidTokenTypes.TRAP_KEYWORD) {
                return Attribute.TRAP;
            }
            throw new AssertionError();
        }
    }

}

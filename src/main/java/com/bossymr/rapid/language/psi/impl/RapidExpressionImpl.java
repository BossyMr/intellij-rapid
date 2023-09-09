package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.lang.ASTFactory;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.DummyHolder;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.SharedImplUtil;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.CharTable;
import org.jetbrains.annotations.NotNull;

import static com.bossymr.rapid.language.psi.RapidElementTypes.*;
import static com.bossymr.rapid.language.psi.RapidTokenTypes.*;

public abstract class RapidExpressionImpl extends PhysicalElement implements RapidExpression {

    protected RapidExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    private static boolean getParenthesis(@NotNull ASTNode oldElement, @NotNull ASTNode newElement) {
        final ASTNode node = oldElement.getTreeParent();
        if (!RapidElementTypes.EXPRESSIONS.contains(node.getElementType())) return false;
        int newPriority = getPriority(newElement);
        int priority = getPriority(node);
        if (newPriority < 0 || priority < 0) return true;
        if (newPriority > priority) return false;
        IElementType elementType = node.getElementType();
        if (elementType.equals(RapidElementTypes.BINARY_EXPRESSION)) {
            if (newPriority < priority) return true;
            RapidBinaryExpression expression = node.getPsi(RapidBinaryExpression.class);
            assert expression != null;
            IElementType sign = expression.getSign().getNode().getElementType();
            IElementType type = newElement.getElementType();
            if (expression.getLeft().getNode().equals(oldElement)) return false;
            if (type.equals(RapidElementTypes.BINARY_EXPRESSION)) {
                IElementType newSign = ((RapidBinaryExpression) newElement).getSign().getNode().getElementType();
                if (TokenSet.create(DIV, DIV_KEYWORD, MOD_KEYWORD).contains(newSign)) return true;
            }
            return !TokenSet.create(PLUS, ASTERISK, AND_KEYWORD, OR_KEYWORD).contains(sign);
        }
        if (TokenSet.create(RapidElementTypes.UNARY_EXPRESSION, RapidElementTypes.REFERENCE_EXPRESSION).contains(elementType)) {
            return newPriority < priority;
        }
        if (TokenSet.create(INDEX_EXPRESSION, FUNCTION_CALL_EXPRESSION, AGGREGATE_EXPRESSION, PARENTHESISED_EXPRESSION,
                LITERAL_EXPRESSION).contains(elementType)) {
            return false;
        }
        throw new IllegalArgumentException();
    }

    private static int getPriority(@NotNull ASTNode expression) {
        IElementType elementType = expression.getElementType();
        assert RapidElementTypes.EXPRESSIONS.contains(elementType);
        if (elementType.equals(RapidElementTypes.BINARY_EXPRESSION)) {
            IElementType type = expression.getPsi(RapidBinaryExpression.class).getSign().getNode().getElementType();
            if (TokenSet.create(XOR_KEYWORD, OR_KEYWORD).contains(elementType)) {
                return 1;
            } else if (TokenSet.create(AND_KEYWORD).contains(type)) {
                return 2;
            } else if (TokenSet.create(LT, GT, LTGT, LE, GE, EQ).contains(type)) {
                return 3;
            } else if (TokenSet.create(PLUS, MINUS).contains(type)) {
                return 4;
            } else if (TokenSet.create(ASTERISK, DIV, DIV_KEYWORD, MOD_KEYWORD).contains(type)) {
                return 5;
            }
        }
        if (elementType.equals(RapidElementTypes.UNARY_EXPRESSION)) {
            IElementType type = expression.getPsi(RapidUnaryExpression.class).getSign().getNode().getElementType();
            if (TokenSet.create(NOT_KEYWORD).contains(type)) {
                return 1;
            } else if (TokenSet.create(PLUS, MINUS).contains(type)) {
                return 4;
            }
        }
        return 6;
    }

    @Override
    public boolean isConditional() {
        RapidType type = getType();
        return type != null && RapidPrimitiveType.BOOLEAN.isAssignable(type);
    }


    @Override
    public void replaceChildInternal(@NotNull PsiElement element, @NotNull TreeElement newElement) {
        ASTNode child = element.getNode();
        boolean add = EXPRESSIONS.contains(child.getElementType()) && EXPRESSIONS.contains(newElement.getElementType()) &&
                getParenthesis(child, newElement);
        if (add) {
            CompositeElement parenthesised = ASTFactory.composite(PARENTHESISED_EXPRESSION);

            TreeElement dummy = (TreeElement) newElement.clone();
            CharTable table = SharedImplUtil.findCharTableByTree(newElement);
            new DummyHolder(getManager(), parenthesised, null, table);
            parenthesised.putUserData(CharTable.CHAR_TABLE_KEY, table);

            parenthesised.rawAddChildren(ASTFactory.leaf(LPARENTH, "("));
            parenthesised.rawAddChildren(dummy);
            parenthesised.rawAddChildren(ASTFactory.leaf(RPARENTH, ")"));

            CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(getProject());
            PsiElement formatted = codeStyleManager.reformat(parenthesised.getPsi());
            parenthesised = (CompositeElement) formatted.getNode();

            newElement.putUserData(CharTable.CHAR_TABLE_KEY, SharedImplUtil.findCharTableByTree(newElement));
            dummy.getTreeParent().replaceChild(dummy, newElement);

            super.replaceChildInternal(element, parenthesised);
        } else {
            super.replaceChildInternal(element, newElement);
        }
    }
}

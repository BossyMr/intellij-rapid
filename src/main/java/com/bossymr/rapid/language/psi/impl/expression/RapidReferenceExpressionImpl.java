package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidExpressionElement;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.resolve.ResolveUtil;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class RapidReferenceExpressionImpl extends RapidExpressionElement implements RapidReferenceExpression {

    public RapidReferenceExpressionImpl() {
        super(RapidElementTypes.REFERENCE_EXPRESSION);
    }

    @Override
    public void deleteChildInternal(@NotNull ASTNode child) {
        if (child.getElementType().equals(RapidElementTypes.REFERENCE_EXPRESSION)) {
            ASTNode node = findChildByType(RapidTokenTypes.DOT);
            assert node != null;
            deleteChildInternal(node);
        }
        super.deleteChildInternal(child);
    }

    @Override
    public @Nullable RapidType getType() {
        PsiElement parent = getParent();
        if (parent instanceof RapidFunctionCallExpression) {
            return ((RapidFunctionCallExpression) parent).getType();
        }
        RapidSymbol element = getSymbol();
        if (element != null) {
            if (element instanceof RapidVariable) {
                return ((RapidVariable) element).getType();
            }
        }
        return null;
    }

    @Override
    public boolean isConstant() {
        RapidSymbol resolve = getSymbol();
        return resolve instanceof RapidField field && field.getAttribute() == RapidField.Attribute.CONSTANT;
    }

    @Override
    public @Nullable RapidExpression getQualifier() {
        return (RapidExpression) findChildByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public @Nullable PsiElement getIdentifier() {
        return findPsiChildByType(RapidTokenTypes.IDENTIFIER);
    }

    @Override
    public @NotNull String getCanonicalText() {
        return getText();
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        PsiElement previous = getIdentifier();
        if (previous == null) throw new IncorrectOperationException();
        PsiElement identifier = RapidElementFactory.getInstance(getProject()).createIdentifier(newElementName);
        previous.replace(identifier);
        return this;
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        if (isReferenceTo(element)) return this;
        if (element instanceof RapidComponent) {
            RapidRecord record = (RapidRecord) element.getParent();
            String expression = record.getName() + "." + ((RapidComponent) element).getName();
            RapidExpression reference = RapidElementFactory.getInstance(getProject()).createExpression(expression);
            getTreeParent().replaceChildInternal(this, (TreeElement) reference.getNode());
        } else if (element instanceof RapidSymbol) {
            String expression = String.valueOf(((RapidSymbol) element).getName());
            RapidExpression reference = RapidElementFactory.getInstance(getProject()).createExpression(expression);
            getTreeParent().replaceChildInternal(this, (TreeElement) reference.getNode());
        }
        return this;
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        return element.getManager().areElementsEquivalent(element, resolve());
    }

    @Override
    public boolean isSoft() {
        return false;
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitReferenceExpression(this);
    }

    @Override
    public @NotNull PsiElement getElement() {
        return this;
    }

    @Override
    public @NotNull TextRange getRangeInElement() {
        PsiElement identifier = getIdentifier();
        if (identifier != null) {
            return TextRange.from(identifier.getStartOffsetInParent(), identifier.getTextLength());
        }
        ASTNode node = findChildByType(RapidTokenTypes.DOT);
        assert node != null;
        return TextRange.from(node.getStartOffsetInParent() + node.getTextLength(), 0);
    }

    @Override
    public @NotNull Collection<RapidSymbol> getSymbols() {
        PsiElement identifier = getIdentifier();
        if (identifier == null) return Collections.emptyList();
        String name = identifier.getText();
        return ResolveUtil.getSymbols(this, name);
    }

    @Override
    public @Nullable RapidSymbol getSymbol() {
        Collection<RapidSymbol> symbols = getSymbols();
        return symbols.size() == 1 ? symbols.iterator().next() : null;
    }

    @Override
    public String toString() {
        return "RapidReferenceExpression:" + getText();
    }
}

package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidExpressionImpl;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.resolve.ResolveUtil;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class RapidReferenceExpressionImpl extends RapidExpressionImpl implements RapidReferenceExpression {

    public RapidReferenceExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull PsiReference getReference() {
        return this;
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
        return findChildByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public @Nullable PsiElement getIdentifier() {
        return findChildByType(RapidTokenTypes.IDENTIFIER);
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
            RapidExpression reference = RapidElementFactory.getInstance(getProject()).createExpressionFromText(expression);
            replace(reference);
        } else if (element instanceof RapidSymbol) {
            String expression = String.valueOf(((RapidSymbol) element).getName());
            RapidExpression reference = RapidElementFactory.getInstance(getProject()).createExpressionFromText(expression);
            replace(reference);
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
    public @NotNull List<RapidSymbol> getSymbols() {
        return CachedValuesManager.getProjectPsiDependentCache(this, (ignored) -> doResolve());
    }

    private @NotNull List<RapidSymbol> doResolve() {
        PsiElement identifier = getIdentifier();
        if (identifier == null) return Collections.emptyList();
        String name = identifier.getText();
        return ResolveUtil.getSymbols(this, name);
    }


    @Override
    public @Nullable RapidSymbol getSymbol() {
        List<RapidSymbol> symbols = getSymbols();
        return symbols.size() > 0 ? symbols.get(0) : null;
    }

    @Override
    public String toString() {
        return "RapidReferenceExpression:" + getText();
    }
}

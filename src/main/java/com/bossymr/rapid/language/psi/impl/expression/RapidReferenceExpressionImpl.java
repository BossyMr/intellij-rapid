package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidExpressionElement;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.RapidVariable;
import com.bossymr.rapid.language.symbol.resolve.ResolveUtil;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
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
        RapidSymbol element = resolve();
        if (element != null) {
            if (element instanceof RapidVariable) {
                return ((RapidVariable) element).getType();
            }
        }
        return null;
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
    public @NotNull Collection<RapidSymbol> resolveReference() {
        PsiElement identifier = getIdentifier();
        if (identifier == null) return Collections.emptyList();
        String name = identifier.getText();
        return ResolveUtil.getSymbols(this, name);
    }

    public @Nullable RapidSymbol resolve() {
        Collection<RapidSymbol> symbols = resolveReference();
        return symbols.size() == 1 ? symbols.iterator().next() : null;
    }

    @Override
    public String toString() {
        return "RapidReferenceExpression:" + getText();
    }
}

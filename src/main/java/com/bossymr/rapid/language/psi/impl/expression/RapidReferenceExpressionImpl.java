package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidExpressionImpl;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.symbol.resolve.ResolveService;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class RapidReferenceExpressionImpl extends RapidExpressionImpl implements RapidReferenceExpression {

    public RapidReferenceExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiReference getReference() {
        PsiElement element = getIdentifier();
        if (element != null) {
            return this;
        } else {
            return null;
        }
    }

    @Override
    public @NotNull Collection<RapidReferenceExpression> getOwnReferences() {
        PsiElement element = getIdentifier();
        if (element != null) {
            return List.of(this);
        } else {
            return List.of();
        }
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
        return resolve instanceof RapidField field && field.getFieldType() == FieldType.CONSTANT;
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
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitReferenceExpression(this);
    }

    @Override
    public @NotNull TextRange getRangeInElement() {
        PsiElement identifier = Objects.requireNonNull(getIdentifier());
        return TextRange.from(identifier.getStartOffsetInParent(), identifier.getTextLength());
    }

    @Override
    public @Nullable PsiElement resolve() {
        RapidSymbol symbol = getSymbol();
        return symbol instanceof PhysicalSymbol physicalSymbol ? physicalSymbol : null;
    }

    @Override
    public @NotNull ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        List<RapidSymbol> symbols = getSymbols();
        return symbols.stream()
                .filter(symbol -> symbol instanceof PhysicalSymbol)
                .map(symbol -> (PhysicalSymbol) symbol)
                .map(PsiElementResolveResult::new)
                .toList().toArray(ResolveResult.EMPTY_ARRAY);
    }

    @Override
    public @NotNull @NlsSafe String getCanonicalText() {
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
    public @NotNull List<RapidSymbol> getSymbols() {
        return CachedValuesManager.getProjectPsiDependentCache(this, (context) -> ResolveService.getInstance(context.getProject()).findSymbols(context));
    }

    @Override
    public @Nullable RapidSymbol getSymbol() {
        List<RapidSymbol> symbols = getSymbols();
        return symbols.isEmpty() ? null : symbols.get(0);
    }

    @Override
    public String toString() {
        return "RapidReferenceExpressionImpl{" +
               "text=" + getCanonicalText() +
               '}';
    }
}

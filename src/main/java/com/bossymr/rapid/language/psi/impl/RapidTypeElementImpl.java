package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.psi.RapidTypeElement;
import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.RapidType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidTypeElementImpl extends RapidElementImpl implements RapidTypeElement {

    public RapidTypeElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitTypeElement(this);
    }

    @Override
    public @Nullable RapidType getType() {
        return CachedValuesManager.getProjectPsiDependentCache(this, (ignored) -> {
            RapidReferenceExpression expression = getReferenceExpression();
            if (expression != null) {
                RapidSymbol element = expression.getSymbol();
                return new RapidType(element instanceof RapidStructure ? (RapidStructure) element : null, expression.getText());
            } else {
                return null;
            }
        });
    }

    @Override
    public @Nullable RapidReferenceExpression getReferenceExpression() {
        return findChildByType(RapidElementTypes.REFERENCE_EXPRESSION);
    }

    @Override
    public String toString() {
        return "RapidTypeElement:" + getText();
    }
}

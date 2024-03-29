package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.type.RapidType;
import com.bossymr.rapid.language.type.RapidUnknownType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidTypeElementImpl extends PhysicalElement implements RapidTypeElement {

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
                if(element instanceof RapidStructure structure) {
                    return structure.createType();
                }
                return new RapidUnknownType(expression.getText());
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

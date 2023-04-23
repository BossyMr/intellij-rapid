package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.*;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RapidArrayImpl extends PhysicalElement implements RapidArray {

    public RapidArrayImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitArray(this);
    }

    @Override
    public @NotNull List<RapidExpression> getDimensions() {
        return findChildrenByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public String toString() {
        return "RapidArray:" + getText();
    }
}

package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidExpressionList;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RapidExpressionListImpl extends RapidElementListImpl implements RapidExpressionList {

    public RapidExpressionListImpl(@NotNull ASTNode node) {
        super(node, RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitExpressionList(this);
    }

    @Override
    public List<RapidExpression> getExpressions() {
        return findChildrenByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public String toString() {
        return "RapidExpressionList:" + getText();
    }
}

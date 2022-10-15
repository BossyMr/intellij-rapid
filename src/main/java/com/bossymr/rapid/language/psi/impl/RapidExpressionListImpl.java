package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidExpressionList;

import java.util.List;

public class RapidExpressionListImpl extends RapidArgumentListElement implements RapidExpressionList {

    public RapidExpressionListImpl() {
        super(RapidElementTypes.EXPRESSION_LIST, RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public List<RapidExpression> getExpressions() {
        return List.of(getChildrenAsPsiElements(RapidElementTypes.EXPRESSIONS, RapidExpression[]::new));
    }

    @Override
    public String toString() {
        return "RapidExpressionList:" + getText();
    }
}

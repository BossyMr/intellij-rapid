package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.RapidArray;
import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RapidArrayImpl extends RapidCompositeElement implements RapidArray {

    public RapidArrayImpl() {
        super(RapidElementTypes.ARRAY);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitArray(this);
    }

    @Override
    public @NotNull List<RapidExpression> getDimensions() {
        return List.of(getChildrenAsPsiElements(RapidElementTypes.EXPRESSIONS, RapidExpression[]::new));
    }
}

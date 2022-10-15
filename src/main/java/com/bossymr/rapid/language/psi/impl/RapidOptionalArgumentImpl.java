package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidOptionalArgumentImpl extends RapidCompositeElement implements RapidOptionalArgument {

    public RapidOptionalArgumentImpl() {
        super(RapidElementTypes.OPTIONAL_ARGUMENT);
    }

    @Override
    public @NotNull RapidReferenceExpression getParameter() {
        return (RapidReferenceExpression) Objects.requireNonNull(findChildByType(RapidElementTypes.REFERENCE_EXPRESSION));
    }

    @Override
    public @Nullable RapidExpression getArgument() {
        return (RapidExpression) findChildByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitOptionalArgument(this);
    }

    @Override
    public String toString() {
        return "RapidOptionalArgument:" + getParameter();
    }
}

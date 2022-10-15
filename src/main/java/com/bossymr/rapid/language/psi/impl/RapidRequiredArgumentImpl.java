package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidRequiredArgumentImpl extends RapidCompositeElement implements RapidRequiredArgument {

    public RapidRequiredArgumentImpl() {
        super(RapidElementTypes.REQUIRED_ARGUMENT);
    }

    @Override
    public @Nullable RapidReferenceExpression getParameter() {
        return (RapidReferenceExpression) findChildByType(RapidElementTypes.REFERENCE_EXPRESSION);
    }

    @Override
    public @NotNull RapidExpression getArgument() {
        return (RapidExpression) Objects.requireNonNull(findChildByType(RapidElementTypes.EXPRESSIONS));
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitRequiredArgument(this);
    }

    @Override
    public String toString() {
        return "RapidRequiredArgument:" + getParameter();
    }
}

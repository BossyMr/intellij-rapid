package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

public interface RapidConditionalArgument extends RapidArgument {

    @Override
    @Nullable RapidReferenceExpression getParameter();

    @Override
    @Nullable RapidExpression getArgument();
}

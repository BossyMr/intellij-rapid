package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RapidOptionalArgument extends RapidArgument {

    @Override
    @NotNull RapidReferenceExpression getParameter();

    @Override
    @Nullable RapidExpression getArgument();
}

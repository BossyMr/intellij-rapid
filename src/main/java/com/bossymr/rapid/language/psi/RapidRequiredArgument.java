package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RapidRequiredArgument extends RapidArgument {

    @Override
    @Nullable RapidReferenceExpression getParameter();

    @Override
    @NotNull RapidExpression getArgument();
}

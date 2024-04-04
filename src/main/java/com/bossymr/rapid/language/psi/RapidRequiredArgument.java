package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

public interface RapidRequiredArgument extends RapidArgument {

    @Override
    @Nullable RapidReferenceExpression getParameter();

    @Override
    @Nullable RapidExpression getArgument();
}

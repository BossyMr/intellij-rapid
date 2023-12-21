package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.NotNull;

public interface RapidCallExpression extends RapidElement {

    @NotNull RapidArgumentList getArgumentList();

    @NotNull RapidExpression getReferenceExpression();

}

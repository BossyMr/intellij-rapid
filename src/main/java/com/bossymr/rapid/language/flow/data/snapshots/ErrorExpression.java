package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ErrorExpression extends VariableSnapshot {

    private final @Nullable RapidExpression expression;

    public ErrorExpression(@NotNull RapidType type) {
        this(null, type);
    }

    public ErrorExpression(@Nullable RapidExpression expression, @NotNull RapidType type) {
        super(type);
        this.expression = expression;
    }

    @Override
    public @Nullable RapidExpression getElement() {
        return expression;
    }
}

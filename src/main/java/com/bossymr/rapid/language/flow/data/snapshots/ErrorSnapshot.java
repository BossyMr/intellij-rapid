package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ErrorSnapshot extends VariableSnapshot {

    public ErrorSnapshot(@NotNull RapidType type) {
        super(type);
    }

    @Override
    public @Nullable ReferenceExpression getUnderlyingVariable() {
        return null;
    }
}

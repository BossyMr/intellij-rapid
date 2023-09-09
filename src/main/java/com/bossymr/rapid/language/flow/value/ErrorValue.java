package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

public final class ErrorValue implements Value {

    private static final @NotNull ErrorValue INSTANCE = new ErrorValue();

    private ErrorValue() {}

    public static @NotNull ErrorValue getInstance() {
        return INSTANCE;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitErrorValue(this);
    }

    @Override
    public @NotNull RapidType getType() {
        return RapidPrimitiveType.ANYTYPE;
    }
}

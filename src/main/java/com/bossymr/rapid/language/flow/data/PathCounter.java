package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.flow.value.SnapshotExpression;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public record PathCounter(@NotNull ReferenceExpression variable, @NotNull BlockCycle increment, @NotNull Set<BlockCycle> reset) implements SnapshotExpression {

    @Override
    public @Nullable ReferenceExpression getUnderlyingVariable() {
        return null;
    }

    @Override
    public @NotNull RapidType getType() {
        return RapidPrimitiveType.NUMBER;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitPathCounterExpression(this);
    }

    @Override
    public String toString() {
        return "K{" + increment + ", " + reset + "}";
    }
}

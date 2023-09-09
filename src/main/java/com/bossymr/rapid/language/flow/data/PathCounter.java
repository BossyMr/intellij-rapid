package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.value.ReferenceSnapshot;
import com.bossymr.rapid.language.flow.value.ReferenceValue;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public class PathCounter implements ReferenceSnapshot {

    private final @NotNull Set<BlockCycle> incrementPath;
    private final @NotNull Set<BlockCycle> resetPath;

    public PathCounter(@NotNull Set<BlockCycle> incrementPath) {
        this.incrementPath = Set.copyOf(incrementPath);
        this.resetPath = Set.of();
    }

    public PathCounter(@NotNull Set<BlockCycle> incrementPath, @NotNull Set<BlockCycle> resetPath) {
        this.incrementPath = Set.copyOf(incrementPath);
        this.resetPath = Set.copyOf(resetPath);
    }

    public @NotNull Set<BlockCycle> getIncrementPath() {
        return incrementPath;
    }

    public @NotNull Set<BlockCycle> getResetPath() {
        return resetPath;
    }

    @Override
    public @Nullable ReferenceValue getVariable() {
        return null;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitPathCounter(this);
    }

    @Override
    public @NotNull RapidType getType() {
        return RapidPrimitiveType.NUMBER;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        PathCounter that = (PathCounter) object;
        return Objects.equals(incrementPath, that.incrementPath) && Objects.equals(resetPath, that.resetPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(incrementPath, resetPath);
    }

    @Override
    public String toString() {
        return "PathCounter{" +
                "update=" + incrementPath +
                ", kill=" + resetPath +
                ", variable=" + getVariable() +
                ", type=" + getType() +
                '}';
    }
}

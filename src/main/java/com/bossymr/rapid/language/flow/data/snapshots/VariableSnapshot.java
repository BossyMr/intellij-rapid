package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.value.ReferenceSnapshot;
import com.bossymr.rapid.language.flow.value.ReferenceValue;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code VariableSnapshot} represents the previous state of a variable. The below example shows how snapshots can be
 * used, as each variable state is represented as a snapshot. This is useful, as although {@code x} is modified, if the
 * value {@code z} and {@code y} is discovered, the value of the other is immediately discovered.
 * <pre>{@code
 *                                          // State:
 * 0: x = <some value between 0 and 10>     // x1 = 10
 * 1: z = x + 2                             // z1 = x1 + 2
 * 2: y = (x == 2)                          // y1 = x1 == 2
 * 3: x = 10                                // x2 = 10
 * }
 */
public class VariableSnapshot implements ReferenceSnapshot {

    private final @Nullable ReferenceValue referenceValue;
    private final @NotNull RapidType type;

    public VariableSnapshot(@NotNull RapidType type) {
        this.referenceValue = null;
        this.type = type;
    }

    public VariableSnapshot(@NotNull ReferenceValue referenceValue) {
        this.referenceValue = referenceValue;
        this.type = referenceValue.getType();
    }

    public @Nullable ReferenceValue getVariable() {
        return referenceValue;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitVariableSnapshot(this);
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public @NotNull String toString() {
        return "VariableSnapshot{" +
                "identity='" + hashCode() + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}

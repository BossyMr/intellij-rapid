package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.value.SnapshotExpression;
import com.bossymr.rapid.language.psi.RapidExpression;
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
public class VariableSnapshot implements SnapshotExpression {

    private final @Nullable RapidExpression expression;
    private final @NotNull RapidType type;

    public VariableSnapshot(@NotNull RapidType type) {
        this(type, null);
    }

    public VariableSnapshot(@NotNull RapidType type, @Nullable RapidExpression expression) {
        this.type = type;
        this.expression = expression;
    }

    @Override
    public @Nullable RapidExpression getElement() {
        return expression;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitVariableSnapshotExpression(this);
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public @NotNull String toString() {
        return "~" + hashCode();
    }
}

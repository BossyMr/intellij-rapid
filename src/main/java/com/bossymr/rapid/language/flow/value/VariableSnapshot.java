package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
@SuppressWarnings("ClassCanBeRecord")
public final class VariableSnapshot implements ReferenceValue {

    private final @NotNull ReferenceValue variable;

    public VariableSnapshot(@NotNull ReferenceValue variable) {
        this.variable = variable;
    }

    @Override
    public void accept(@NotNull ControlFlowVisitor visitor) {
        variable.accept(visitor);
    }

    @Override
    public @NotNull RapidType type() {
        return variable.type();
    }

    public @NotNull ReferenceValue getVariable() {
        return variable;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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
public final class VariableSnapshot implements ReferenceValue {

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

    @Override
    public void accept(@NotNull ControlFlowVisitor visitor) {
        if (referenceValue != null) {
            referenceValue.accept(visitor);
        }
    }

    public @NotNull Optional<ReferenceValue> getReferenceValue() {
        // TODO: 2023-07-21 Rebuild with correct reference value after reference function call argument -> OptPar -> x
        return Optional.ofNullable(referenceValue);
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

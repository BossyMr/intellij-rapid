package com.bossymr.rapid.language.flow.data.snapshots;

import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code Snapshot} represents the value of a variable at a specific point. If a variable is reassigned, a new
 * snapshot is created to represent that new assignment. As a result, it is possible to find the value of expressions
 * which refer to a specific variable even after that variable is reassigned.
 * <p>
 * For example: {@code y := x + 1} and {@code z := x * 2} both refer to the same variable, {@code x}. Even if {@code x}
 * is reassigned to some other value, it is still possible to calculate the value of {@code z} if {@code y} is given,
 * and vice-versa. This is because, when {@code x} is reassigned, a new snapshot is created, meaning that all previous
 * references to {@code x} still refer to its previous snapshot, and are unchanged.
 * <p>
 * Each snapshot has a default optionality which represents which optionality the snapshot has by default. It is used to
 * avoid adding a new condition asserting the optionality of every created snapshot. It is also used to optimize
 * calculating the optionality of a given snapshot, as the vast majority of snapshots are present and can therefore not
 * have any other optionality. A new snapshot has by default the optionality, present, however, another optionality can
 * be specified using {@link #createSnapshot(RapidType, Optionality)}.
 */
public interface Snapshot {

    /**
     * Creates a new snapshot of the specific type.
     *
     * @param type the type.
     * @return a new snapshot.
     */
    static @NotNull Snapshot createSnapshot(@NotNull RapidType type) {
        return createSnapshot(type, Optionality.PRESENT);
    }

    /**
     * Creates a new snapshot, which is a child to the specific snapshot.
     *
     * @param type the type.
     * @param parent the parent.
     * @return a new snapshot.
     * @throws IllegalArgumentException if the specified parent is not a record or array.
     */
    static @NotNull Snapshot createSnapshot(@NotNull RapidType type, @Nullable Snapshot parent) {
        if (parent != null && !(parent instanceof ArraySnapshot || parent instanceof RecordSnapshot)) {
            throw new IllegalArgumentException("Cannot create snapshot with parent: " + parent + ", of type: " + parent.getType());
        }
        return createSnapshot(type, parent, Optionality.PRESENT, true);
    }

    /**
     * Creates a new snapshot with the specified optionality.
     *
     * @param type the type.
     * @param optionality the optionality.
     * @return a new snapshot.
     */
    static @NotNull Snapshot createSnapshot(@NotNull RapidType type, @NotNull Optionality optionality) {
        return createSnapshot(type, null, optionality, true);
    }

    static @NotNull Snapshot createSnapshot(@NotNull RapidType type, @Nullable Snapshot parent, @NotNull Optionality optionality, boolean hasIdentity) {
        if (type.isArray()) {
            return new ArraySnapshot(parent, type, optionality, hasIdentity);
        } else if (type.isRecord()) {
            return new RecordSnapshot(parent, type, optionality, hasIdentity);
        } else {
            return new VariableSnapshot(parent, type, optionality, hasIdentity);
        }
    }

    /**
     * Returns the parent of this snapshot.
     *
     * @return the parent of this snapshot.
     */
    @Nullable Snapshot getParent();

    /**
     * Returns the default optionality of this snapshot. If the default optionality is unknown, it might be modified by
     * additional constraints. If the default snapshot is not unknown, it cannot be modified.
     *
     * @return the default optionality of this snapshot.
     */
    @NotNull Optionality getOptionality();

    /**
     * Returns the type of this snapshot.
     *
     * @return the type of this snapshot.
     */
    @NotNull RapidType getType();

    boolean hasIdentity();
}

package com.bossymr.rapid.language.flow;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An {@code ArgumentGroup} represents a group of mutually exlusive state.
 *
 * @param isOptional if the state are optional.
 * @param arguments the state in this group.
 */
public record ArgumentGroup(
        boolean isOptional,
        @NotNull List<Argument> arguments
) {

    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitArgumentGroup(this);
    }

}

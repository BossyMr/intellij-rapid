package com.bossymr.rapid.language.flow;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An {@code ArgumentGroup} represents a group of mutually exlusive arguments.
 *
 * @param isOptional if the arguments are optional.
 * @param arguments the arguments in this group.
 */
public record ArgumentGroup(
        boolean isOptional,
        @NotNull List<Argument> arguments
) {

    public <T> T accept(@NotNull ControlFlowVisitor<T> visitor) {
        return visitor.visitArgumentGroup(this);
    }

}

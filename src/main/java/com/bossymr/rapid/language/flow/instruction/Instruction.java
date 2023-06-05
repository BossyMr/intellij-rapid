package com.bossymr.rapid.language.flow.instruction;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * An {@code Instruction} represents a statement.
 */
public sealed interface Instruction permits BranchingInstruction, LinearInstruction {

    void accept(@NotNull ControlFlowVisitor visitor);

}

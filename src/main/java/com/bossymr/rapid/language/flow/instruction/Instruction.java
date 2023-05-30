package com.bossymr.rapid.language.flow.instruction;

/**
 * An {@code Instruction} represents a statement.
 */
public sealed interface Instruction permits BranchingInstruction, LinearInstruction {
}

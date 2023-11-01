package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.value.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * A {@code BlockCycle} represents a chain of instructions which form a loop, i.e. the first instruction in the sequence
 * is also the last instruction, although it is not included in the specified list. All specified guard
 * expressions must be {@code true} for this sequence to be followed.
 *
 * @param instructions the instructions in the sequence.
 * @param guards the guards which must be {@code true}.
 * @param exits all instructions which lead out of this cycle (instruction in the cycle is the key, and the instruction
 *              outside of this cycle is the value).
 */
public record BlockCycle(@NotNull List<Instruction> instructions,
                         @NotNull Map<Instruction, Expression> guards,
                         @NotNull Map<Instruction, Instruction> exits) {
}

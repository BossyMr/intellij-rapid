package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.value.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record BlockCycle(@NotNull List<Instruction> sequences,
                         @NotNull Map<Instruction, Expression> guards,
                         @NotNull Map<Instruction, Instruction> exits) {

    @Override
    public String toString() {
        return sequences.stream()
                        .map(Instruction::getIndex)
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ", "[", "]"));
    }
}

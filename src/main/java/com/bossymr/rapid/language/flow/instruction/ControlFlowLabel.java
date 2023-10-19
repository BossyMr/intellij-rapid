package com.bossymr.rapid.language.flow.instruction;

import com.bossymr.rapid.language.builder.Label;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ControlFlowLabel implements Label {

    private final @Nullable String name;
    private @Nullable Instruction instruction;

    public ControlFlowLabel(@Nullable String name) {
        this.name = name;
    }

    @Override
    public @Nullable String getName() {
        return name;
    }

    public @Nullable Instruction getInstruction() {
        return instruction;
    }

    public void setInstruction(@NotNull Instruction instruction) {
        this.instruction = instruction;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ControlFlowLabel that = (ControlFlowLabel) object;
        return Objects.equals(name, that.name) && Objects.equals(instruction, that.instruction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, instruction);
    }

    @Override
    public String toString() {
        return "ControlFlowLabel{" +
                "name='" + name + '\'' +
                ", instruction=" + instruction +
                '}';
    }
}

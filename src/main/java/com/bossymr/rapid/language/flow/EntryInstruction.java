package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.expression.Expression;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.psi.BlockType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class EntryInstruction {

    private final @NotNull Instruction instruction;
    private final @NotNull BlockType entryType;

    public EntryInstruction(@NotNull BlockType entryType, @NotNull Instruction instruction) {
        this.instruction = instruction;
        this.entryType = entryType;
    }

    public @NotNull Instruction getInstruction() {
        return instruction;
    }

    public @NotNull BlockType getEntryType() {
        return entryType;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        EntryInstruction that = (EntryInstruction) object;
        return Objects.equals(instruction, that.instruction) && entryType == that.entryType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(instruction, entryType);
    }

    @Override
    public String toString() {
        return "EntryInstruction{" +
                "instruction=" + getInstruction() +
                ", entryType=" + getEntryType() +
                '}';
    }

    public static class ErrorEntryInstruction extends EntryInstruction {

        private final @NotNull List<Expression> exceptions;

        public ErrorEntryInstruction(@NotNull Instruction instruction, @NotNull List<Expression> exceptions) {
            super(BlockType.ERROR_CLAUSE, instruction);
            this.exceptions = exceptions;
        }

        public @NotNull List<Expression> getExceptions() {
            return exceptions;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            if (!super.equals(object)) return false;
            ErrorEntryInstruction that = (ErrorEntryInstruction) object;
            return Objects.equals(exceptions, that.exceptions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), exceptions);
        }

        @Override
        public String toString() {
            return "ErrorEntryInstruction{" +
                    "exceptions=" + getExceptions() +
                    ", instruction=" + getInstruction() +
                    ", entryType=" + getEntryType() +
                    '}';
        }
    }
}

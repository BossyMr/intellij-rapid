package com.bossymr.flow.instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An {@code Instruction} represents an instruction.
 */
public abstract class Instruction {

    private final List<Instruction> successors = new ArrayList<>();
    private final Instruction predecessor;

    /**
     * Create a new {@code Instruction}.
     *
     * @param predecessor the predecessor.
     */
    protected Instruction(Instruction predecessor) {
        this.predecessor = predecessor;
    }

    /**
     * Return the successors of this instruction.
     *
     * @return the successors of this instruction.
     */
    public List<Instruction> getSuccessors() {
        return successors;
    }

    /**
     * Return the predecessor of this instruction.
     *
     * @return the predecessor of this instruction.
     */
    public Instruction getPredecessor() {
        return predecessor;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Instruction that = (Instruction) o;
        return Objects.equals(successors, that.successors) && Objects.equals(predecessor, that.predecessor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(successors, predecessor);
    }
}

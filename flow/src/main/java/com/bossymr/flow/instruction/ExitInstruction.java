package com.bossymr.flow.instruction;

/**
 * An {@code ExitInstruction} represents an instruction where the program is terminated.
 */
public class ExitInstruction extends Instruction{

    /**
     * Create a new {@code ExitInstruction}.
     *
     * @param predecessor the predecessor.
     */
    public ExitInstruction(Instruction predecessor) {
        super(predecessor);
    }

    @Override
    public String toString() {
        return "exit";
    }
}

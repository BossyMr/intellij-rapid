package com.bossymr.flow.instruction;

import com.bossymr.flow.state.MemorySnapshot;

public interface Instruction {

    /**
     * Returns a new snapshot representing the state of the program before this instruction is executed.
     *
     * @return a new snapshot.
     */
    MemorySnapshot before();

    /**
     * Returns a new snapshot representing the state of the program after this instruction is executed.
     *
     * @return a new snapshot.
     */
    MemorySnapshot after();

}

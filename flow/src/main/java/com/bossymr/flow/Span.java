package com.bossymr.flow;

import com.bossymr.flow.state.MemorySnapshot;

/**
 * A {@code Span} represents an element in the code. A span is used to retrieve the program state before or after an
 * expression or instruction is executed.
 */
public class Span {

    /**
     * Returns a new snapshot representing the state of the program before this span is executed.
     *
     * @return a new snapshot.
     */
    public MemorySnapshot before() {
        return null;
    }

    /**
     * Returns a new snapshot representing the state of the program after this span is executed.
     *
     * @return a new snapshot.
     */
    public MemorySnapshot after() {
        return null;
    }
}

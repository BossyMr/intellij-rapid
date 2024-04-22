package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

/**
 * A {@code SymbolSearchMethod} determines the method with which a symbol search is performed.
 */
public enum SymbolSearchMethod {

    /**
     * Searches the current block. For example, specifying the block as a routine will return all symbols declared in
     * the routine.
     */
    @Deserializable("block")
    BLOCK,

    /**
     * Searches the current scope. For example, specifying the block as a routine will return all symbols which are
     * accessible from the routine.
     */
    @Deserializable("scope")
    SCOPE,

    /**
     * Searches the current stack.
     */
    @Deserializable("stack")
    STACK

}

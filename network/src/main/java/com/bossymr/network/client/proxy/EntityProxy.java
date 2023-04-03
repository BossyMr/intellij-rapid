package com.bossymr.network.client.proxy;

/**
 * A {@code EntityProxy} is an entity proxy with state.
 */
public interface EntityProxy extends NetworkProxy {

    /**
     * Refreshes the state of this entity.
     */
    void refresh();

}

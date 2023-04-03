package com.bossymr.network.client.proxy;

import com.bossymr.network.client.NetworkManager;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code NetworkProxy} is an entity proxy.
 * <p>
 * A {@code NetworkProxy} can be either managed by a {@link NetworkManager} or unmanaged. If a proxy is managed, all
 * requests will be handled by the manager. {@link #getNetworkManager()} can be used to retrieve the current manager.
 *
 * @see EntityProxy
 */
public interface NetworkProxy {

    /**
     * Returns the manager which manages this proxy.
     *
     * @return the manager which manages this proxy, or {@code null} if this proxy is unmanaged.
     */
    @Nullable NetworkManager getNetworkManager();

    /**
     * Assigns this proxy to the specified manager.
     *
     * @param manager the manager, or {@code null} to make this proxy unmanaged.
     */
    void move(@Nullable NetworkManager manager);
}

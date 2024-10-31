package com.bossymr.rapid.robot.api.client.proxy;

import com.bossymr.rapid.robot.api.NetworkManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code NetworkProxy} represents a network object. An instance can be either managed or unmanaged. All requests sent
 * by a proxy will be sent through it's managing {@link NetworkManager}. In case it is unmanaged, any attempt to send a
 * request will lead to a {@link ProxyException}.
 */
public interface NetworkProxy {

    /**
     * Returns the {@code NetworkManager} currently managing this object.
     *
     * @return the {@code NetworkManager} currently managing this object, or {@code null} if this object is unmanaged.
     * @see #attach(NetworkManager)
     */
    @Nullable NetworkManager getNetworkManager();

    /**
     * Attaches this manager to the specified {@code NetworkManager}.
     *
     * @param manager the new manager of this object.
     */
    void attach(@NotNull NetworkManager manager);

    /**
     * Detaches this object from any manager.
     */
    void detach();
}

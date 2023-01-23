package com.bossymr.network;

import java.io.IOException;

/**
 * A {@code SubscriptionEntity} represents an ongoing subscription.
 */
public interface SubscriptionEntity {

    /**
     * Unsubscribes from this subscription.
     *
     * @throws InterruptedException if this {@code SubscriptionEntity} is interrupted.
     * @throws IOException if an I/O error has occured.
     */
    void unsubscribe() throws InterruptedException, IOException;
}

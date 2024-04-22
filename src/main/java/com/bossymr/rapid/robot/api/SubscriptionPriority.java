package com.bossymr.rapid.robot.api;

/**
 * A {@code SubscriptionPriority} indicates the priority with which to subscribe to a resource.
 */
public enum SubscriptionPriority {

    /**
     * A low priority indicates that events are sent with a maximum delay of 5 seconds.
     */
    LOW,

    /**
     * A medium priority indicates that events are sent with a maximum delay of 200 ms.
     */
    MEDIUM,

    /**
     * A high priority indicates that events are sent as soon as possible. A high priority can only be used when
     * subscribing to variables and to I/O signals.
     */
    HIGH
}

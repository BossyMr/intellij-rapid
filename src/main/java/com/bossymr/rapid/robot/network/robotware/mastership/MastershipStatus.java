package com.bossymr.rapid.robot.network.robotware.mastership;

import com.bossymr.rapid.robot.api.annotations.Alias;

/**
 * {@code MastershipStatus} represents the current state of a mastership domain.
 */
public enum MastershipStatus {

    /**
     * Mastership is currently not being held.
     */
    @Alias("nomaster")
    NO_MASTER,

    /**
     * Mastership is currently being held by a remote user.
     */
    @Alias("remote")
    REMOTE,

    /**
     * Mastership is currently being held by a local user.
     */
    @Alias("local")
    LOCAL,

    /**
     * Mastership is currently being held by an internal user.
     */
    @Alias("internal")
    INTERNAL

}

package com.bossymr.rapid.robot.network.robotware.mastership;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

/**
 * {@code MastershipStatus} represents the current state of a mastership domain.
 */
public enum MastershipStatus {

    /**
     * Mastership is currently not being held.
     */
    @Deserializable("nomaster")
    NO_MASTER,

    /**
     * Mastership is currently being held by a remote user.
     */
    @Deserializable("remote")
    REMOTE,

    /**
     * Mastership is currently being held by a local user.
     */
    @Deserializable("local")
    LOCAL,

    /**
     * Mastership is currently being held by an internal user.
     */
    @Deserializable("internal")
    INTERNAL

}

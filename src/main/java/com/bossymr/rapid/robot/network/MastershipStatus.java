package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Deserializable;

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
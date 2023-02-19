package com.bossymr.rapid.robot.network.robotware.mastership;

import com.bossymr.network.annotations.Deserializable;

/**
 * {@code MastershipType} represents the possible mastership domains.
 */
public enum MastershipType {

    /**
     * Mastership domain required to update robot configuration.
     */
    @Deserializable("cfg")
    CONFIGURATION,

    /**
     * Mastership domain required to move robot.
     */
    @Deserializable("motion")
    MOTION,

    /**
     * Mastership domain required to update or run {@code RAPID} program.
     */
    @Deserializable("rapid")
    RAPID
}

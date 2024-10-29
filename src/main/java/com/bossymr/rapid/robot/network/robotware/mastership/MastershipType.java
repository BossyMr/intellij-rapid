package com.bossymr.rapid.robot.network.robotware.mastership;

import com.bossymr.rapid.robot.api.annotations.Alias;

/**
 * {@code MastershipType} represents the possible mastership domains.
 */
public enum MastershipType {

    /**
     * Mastership domain required to update robot configuration.
     */
    @Alias("cfg")
    CONFIGURATION,

    /**
     * Mastership domain required to move robot.
     */
    @Alias("motion")
    MOTION,

    /**
     * Mastership domain required to update or run {@code RAPID} program.
     */
    @Alias("rapid")
    RAPID
}

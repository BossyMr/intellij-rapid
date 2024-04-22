package com.bossymr.rapid.robot.network.robotware.panel;

import com.bossymr.rapid.robot.api.annotations.Deserializable;

public enum ControllerState {

    /**
     * The robot is initializing and will shift to {@link #MOTORS_OFF} when started.
     */
    @Deserializable("init")
    INITIALIZING,

    /**
     * The robot is ready to move.
     */
    @Deserializable("motoron")
    MOTORS_ON,

    /**
     * The robot is in standby mode. The state has to be shifted to {@link #MOTORS_ON} before the robot can move.
     */
    @Deserializable("motoroff")
    MOTORS_OFF,

    /**
     * The robot is stopped due to a safety precaution. For example, a door to the robot might be opened.
     */
    @Deserializable("guardstop")
    GUARD_STOP,

    /**
     * The robot is stopped due to an emergency stop.
     */
    @Deserializable("emergencystop")
    EMERGENCY_STOP,

    /**
     * The robot is ready to shift away from {@link #EMERGENCY_STOP} as emergency stop is no longer active, but the
     * transition isn't confirmed.
     */
    @Deserializable("emergencystopreset")
    EMERGENCY_STOP_RESET,

    /**
     * The robot is in a system failure state and must be restarted.
     */
    @Deserializable("sysfail")
    SYSTEM_FAIL,

}

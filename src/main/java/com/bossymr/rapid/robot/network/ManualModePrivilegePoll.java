package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Property;

/**
 * A Request Manual Mode Privilege (RMMP) poll update which contains information regarding the status of a requested
 * elevated privilege.
 */
@Entity("user-rmmp-poll")
public interface ManualModePrivilegePoll {

    /**
     * Returns the code of the status.
     *
     * @return the code of the status.
     */
    @Property("code")
    int getCode();

    /**
     * Returns the status of the request.
     *
     * @return the status of the request.
     */
    @Property("status")
    String getStatus();

}

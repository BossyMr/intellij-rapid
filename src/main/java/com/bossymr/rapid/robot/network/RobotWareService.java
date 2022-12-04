package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Service;
import org.jetbrains.annotations.NotNull;

/**
 * A service used to communicate with the RobotWare section of the robot.
 */
@Service("/rw")
public interface RobotWareService {

    /**
     * Returns the event log service.
     *
     * @return the event log service.
     */
    @NotNull EventLogService getEventLogService();

}

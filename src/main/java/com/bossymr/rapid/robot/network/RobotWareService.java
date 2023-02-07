package com.bossymr.rapid.robot.network;

import com.bossymr.network.annotations.Service;
import com.bossymr.rapid.robot.network.robotware.io.InputOutputService;
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

    @NotNull MastershipService getMastershipService();

    @NotNull RapidService getRapidService();

    @NotNull InputOutputService getInputOutputService();

}

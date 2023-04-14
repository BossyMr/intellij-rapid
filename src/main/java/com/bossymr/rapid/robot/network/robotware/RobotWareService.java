package com.bossymr.rapid.robot.network.robotware;

import com.bossymr.network.annotations.Service;
import com.bossymr.rapid.robot.network.EventLogService;
import com.bossymr.rapid.robot.network.robotware.io.InputOutputService;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipService;
import com.bossymr.rapid.robot.network.robotware.panel.PanelService;
import com.bossymr.rapid.robot.network.robotware.rapid.RapidService;
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

    /**
     * Returns the mastership service.
     *
     * @return the mastership service.
     */
    @NotNull MastershipService getMastershipService();

    /**
     * Returns the {@code RAPID} service.
     *
     * @return the {@code RAPID} service.
     */
    @NotNull RapidService getRapidService();

    /**
     * Returns the I/O service.
     *
     * @return the I/O service.
     */
    @NotNull InputOutputService getInputOutputService();

    @NotNull PanelService getPanelService();
}

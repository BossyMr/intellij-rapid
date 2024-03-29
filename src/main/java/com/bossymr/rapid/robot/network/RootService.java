package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.annotations.Fetch;
import com.bossymr.rapid.robot.api.annotations.Service;
import com.bossymr.rapid.robot.network.robotware.RobotWareService;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code Robot} represents a connection to a remote robot. An implementation to this interface is created
 * dynamically.
 */
@Service
public interface RootService {

    /**
     * Logs out of the currently logged-in user.
     */
    @Fetch("/logout")
    @NotNull NetworkQuery<Void> logout();

    /**
     * Returns the user service.
     *
     * @return the user service.
     */
    @NotNull UserService getUserService();

    /**
     * Returns the controller service.
     *
     * @return the controller service.
     */
    @NotNull ControllerService getControllerService();

    /**
     * Returns the RobotWare service.
     *
     * @return the RobotWare service.
     */
    @NotNull RobotWareService getRobotWareService();

}

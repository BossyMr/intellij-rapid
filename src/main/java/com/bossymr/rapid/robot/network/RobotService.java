package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.GET;
import com.bossymr.rapid.robot.network.annotations.Service;
import com.bossymr.rapid.robot.network.client.impl.NetworkClientImpl;
import com.bossymr.rapid.robot.network.query.Query;
import com.intellij.credentialStore.Credentials;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;

/**
 * A {@code Robot} represents a connection to a remote robot. An implementation to this interface is created
 * dynamically.
 * <p>
 * A {@code Robot} instance is created with {@link #connect(URI, Credentials)}.
 */
@Service
public interface RobotService {

    /**
     * Connects to the specified path, using the specified credentials. An attempt to connect to the remote robot will
     * be attempted.
     *
     * @param path the path of the robot to connect to.
     * @param credentials the credentials to authenticate with.
     * @return a robot which is connected to the specified robot.
     * @throws IOException if an I/O error occurs.
     */
    static @NotNull RobotService connect(@NotNull URI path, @NotNull Credentials credentials) throws IOException {
        return new NetworkClientImpl(path, credentials).newService(RobotService.class);
    }

    /**
     * Logs out of the currently logged-in user.
     */
    @GET("/logout")
    @NotNull Query<Void> logout();

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

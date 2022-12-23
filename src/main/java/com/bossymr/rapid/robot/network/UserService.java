package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Field;
import com.bossymr.rapid.robot.network.annotations.GET;
import com.bossymr.rapid.robot.network.annotations.POST;
import com.bossymr.rapid.robot.network.annotations.Service;
import com.bossymr.rapid.robot.network.query.Query;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A service used to configure users, grants, remote users and {@code Manual Mode Privilege (RMMP)}.
 */
@Service("/users")
public interface UserService {


    /**
     * Register the currently logged-in user with the specified username, application name, location and locale.
     * {@link UserLocale#LOCAL} can only be used if connected to the robot through a service or TPU port.
     *
     * @param username the username to register.
     * @param application the application name to register.
     * @param location the location of the user.
     * @param locale the locale of the user.
     */
    @POST("")
    @NotNull Query<Void> register(
            @NotNull @Field("username") String username,
            @NotNull @Field("application") String application,
            @NotNull @Field("location") String location,
            @NotNull @Field("ulocale") UserLocale locale
    );

    /**
     * Impersonate a user with the same credentials on the same machine.
     *
     * @param identifier the identifier of the user to impersonate.
     */
    @POST("?action=impersonate")
    @NotNull Query<Void> impersonate(
            @NotNull @Field("uid") String identifier
    );

    /**
     * Changes the locale of client. A client is normally connected as a remote client. To change the local to local an
     * enabling button should be pressed and released within 5 seconds of this request.
     *
     * @param locale the new locale.
     */
    @POST("?action=set-locale")
    @NotNull Query<Void> login(
            @NotNull @Field("type") UserLocale locale
    );

    /**
     * Returns the grants awarded to the currently logged-in user.
     *
     * @return the grants awarded to the currently logged-in user.
     */
    @GET("/grants")
    @NotNull Query<List<UserGrant>> getGrants();

    /**
     * Returns the manual mode privilege service.
     *
     * @return the manual mode privilege service.
     */
    @NotNull ManualModePrivilegeService getManualModePrivilegeService();

    /**
     * Returns the remote user service.
     *
     * @return the remote user service.
     */
    @NotNull RemoteUserService getRemoteUserService();

}
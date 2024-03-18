package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.annotations.Fetch;
import com.bossymr.rapid.robot.api.annotations.Field;
import com.bossymr.rapid.robot.api.annotations.Service;
import com.bossymr.rapid.robot.api.client.FetchMethod;
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
    @Fetch(method = FetchMethod.POST, value = "")
  @NotNull NetworkQuery<Void> register(@NotNull @Field("username") String username,
                                @NotNull @Field("application") String application,
                                @NotNull @Field("location") String location,
                                @NotNull @Field("ulocale") UserLocale locale);

    /**
     * Impersonate a user with the same credentials on the same machine.
     *
     * @param identifier the identifier of the user to impersonate.
     */
    @Fetch(method = FetchMethod.POST, value = "?action=impersonate")
  @NotNull NetworkQuery<Void> impersonate(@NotNull @Field("uid") String identifier);

    /**
     * Changes the locale of client. A client is normally connected as a remote client. To change the local to local an
     * enabling button should be pressed and released within 5 seconds of this request.
     *
     * @param locale the new locale.
     */
    @Fetch(method = FetchMethod.POST, value = "?action=set-locale")
  @NotNull NetworkQuery<Void> login(@NotNull @Field("type") UserLocale locale);

    /**
     * Returns the grants awarded to the currently logged-in user.
     *
     * @return the grants awarded to the currently logged-in user.
     */
        @Fetch("/grants")
  @NotNull NetworkQuery<List<UserGrant>> getGrants();

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

package com.bossymr.rapid.robot.network;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.SubscribableNetworkCall;
import com.bossymr.network.annotations.*;
import org.jetbrains.annotations.NotNull;

/**
 * A service used to configure {@code Manual Mode Privilege (RMMP)}.
 */
@Service("/users/rmmp")
public interface ManualModePrivilegeService {

    /**
     * Returns the current {@code Manual Mode Privilege (RMMP)} state.
     *
     * @return the current {@code Manual Mode Privilege (RMMP)} state.
     */
    @GET("")
    @NotNull NetworkCall<ManualModePrivilegeState> getStatus();

    /**
     * Requests {@code Manual Mode Privilege (RMMP)}. The request must be accepted by a local client within 10 seconds.
     * To extend the timeout, poll {@link #poll()}.
     * <p>
     * The remote robot must be in manual mode.
     *
     * @param privilege the privilege to request, must be {@link ManualModePrivilege#MODIFY MODIFY} or
     * {@link ManualModePrivilege#EXECUTE EXECUTE}.
     */
    @POST("")
    @NotNull NetworkCall<Void> request(
            @NotNull @Field("privilege") RequestManualModePrivilege privilege
    );

    /**
     * Responds to a request for {@code Manual Mode Privilege (RMMP)}.
     *
     * @param identifier the identifier of the user who made the request.
     * @param privilege the response to the request.
     */
    @POST("?action=set")
    @NotNull NetworkCall<Void> respond(
            @NotNull @Field("uid") String identifier,
            @NotNull @Field("privilege") RequestManualModePrivilege privilege
    );

    /**
     * Cancels a held {@code Manual Mode Privilege (RMMP)} or a requested privilege.
     */
    @POST("?action=cancel")
    @NotNull NetworkCall<Void> cancel();

    /**
     * Subscribes to requests for {@code Manual Mode Privilege (RMMP)}.
     */
    @Subscribable("/users/rmmp'")
    @NotNull SubscribableNetworkCall<ManualModePrivilegeEvent> onRequest();

    /**
     * Returns the state of a {@code Manual Mode Privilege (RMMP)} request. This method is also used to extend the
     * timeout of a request.
     *
     * @return the state of a {@code Manual Mode Privilege (RMMP)} request.
     */
    @GET("/users/rmmp/poll")
    @NotNull NetworkCall<ManualModePrivilegePoll> poll();

}

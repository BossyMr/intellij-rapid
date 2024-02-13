package com.bossymr.rapid.robot.network;

import com.bossymr.network.NetworkQuery;
import com.bossymr.network.SubscribableNetworkQuery;
import com.bossymr.network.annotations.Fetch;
import com.bossymr.network.annotations.Field;
import com.bossymr.network.annotations.Service;
import com.bossymr.network.annotations.Subscribable;
import com.bossymr.network.client.FetchMethod;
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
        @Fetch("")
  @NotNull NetworkQuery<ManualModePrivilegeState> getStatus();

    /**
     * Requests {@code Manual Mode Privilege (RMMP)}. The request must be accepted by a local client within 10 seconds.
     * To extend the timeout, poll {@link #poll()}.
     * <p>
     * The remote robot must be in manual mode.
     *
     * @param privilege the privilege to request, must be {@link ManualModePrivilege#MODIFY MODIFY} or
     * {@link ManualModePrivilege#EXECUTE EXECUTE}.
     */
    @Fetch(method = FetchMethod.POST, value = "")
  @NotNull NetworkQuery<Void> request(@NotNull @Field("privilege") RequestManualModePrivilege privilege);

    /**
     * Responds to a request for {@code Manual Mode Privilege (RMMP)}.
     *
     * @param identifier the identifier of the user who made the request.
     * @param privilege the response to the request.
     */
    @Fetch(method = FetchMethod.POST, value = "?action=set")
  @NotNull NetworkQuery<Void> respond(@NotNull @Field("uid") String identifier,
                               @NotNull @Field("privilege") RequestManualModePrivilege privilege);

    /**
     * Cancels a held {@code Manual Mode Privilege (RMMP)} or a requested privilege.
     */
    @Fetch(method = FetchMethod.POST, value = "?action=cancel")
  @NotNull NetworkQuery<Void> cancel();

    /**
     * Subscribes to requests for {@code Manual Mode Privilege (RMMP)}.
     */
    @Subscribable("/users/rmmp'")
    @NotNull SubscribableNetworkQuery<ManualModePrivilegeEvent> onRequest();

    /**
     * Returns the state of a {@code Manual Mode Privilege (RMMP)} request. This method is also used to extend the
     * timeout of a request.
     *
     * @return the state of a {@code Manual Mode Privilege (RMMP)} request.
     */
        @Fetch("/users/rmmp/poll")
  @NotNull NetworkQuery<ManualModePrivilegePoll> poll();

}

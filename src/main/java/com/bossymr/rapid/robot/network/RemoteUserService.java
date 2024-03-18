package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.SubscribableNetworkQuery;
import com.bossymr.rapid.robot.api.annotations.Fetch;
import com.bossymr.rapid.robot.api.annotations.Service;
import com.bossymr.rapid.robot.api.annotations.Subscribable;
import com.bossymr.rapid.robot.api.client.FetchMethod;
import org.jetbrains.annotations.NotNull;

/**
 * A service used to configure a remote user.
 */
@Service("/users/remoteuser")
public interface RemoteUserService {

    /**
     * Requests the TPU to login as a remote user. As a result, the TPU will be notified to log off as the current user,
     * and login as another user.
     * <p>
     * The grant "UAS_REMOTE_LOGIN" is required.
     */
    @Fetch(method = FetchMethod.POST, value = "?action=remotelogin")
    @NotNull NetworkQuery<Void> login();

    /**
     * Requests the TPU to logout of the current user.
     */
    @Fetch(method = FetchMethod.POST, value = "?action=remotelogout ")
    @NotNull NetworkQuery<Void> logout();

    /**
     * Subscribes to changes for remote user connections.
     */
    @Subscribable("/users/remoteuserstate")
    @NotNull SubscribableNetworkQuery<RemoteUserEvent> onRequest();

}

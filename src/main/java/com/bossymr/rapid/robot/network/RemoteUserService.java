package com.bossymr.rapid.robot.network;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.SubscribableNetworkCall;
import com.bossymr.network.annotations.POST;
import com.bossymr.network.annotations.Service;
import com.bossymr.network.annotations.Subscribable;
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
    @POST("?action=remotelogin")
    @NotNull NetworkCall<Void> login();

    /**
     * Requests the TPU to logout of the current user.
     */
    @POST("?action=remotelogout ")
    @NotNull NetworkCall<Void> logout();

    /**
     * Subscribes to changes for remote user connections.
     */
    @Subscribable("/users/remoteuserstate")
    @NotNull SubscribableNetworkCall<RemoteUserEvent> onRequest();

}

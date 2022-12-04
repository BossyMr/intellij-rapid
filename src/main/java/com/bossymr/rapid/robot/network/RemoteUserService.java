package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Service;
import com.bossymr.rapid.robot.network.query.Query;
import com.bossymr.rapid.robot.network.query.Query.POST;
import com.bossymr.rapid.robot.network.query.Query.Subscribable;
import com.bossymr.rapid.robot.network.query.SubscribableQuery;
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
    @NotNull Query<Void> login();

    /**
     * Requests the TPU to logout of the current user.
     */
    @POST("?action=remotelogout ")
    @NotNull Query<Void> logout();

    /**
     * Subscribes to changes for remote user connections.
     */
    @Subscribable("/users/remoteuser")
    @NotNull SubscribableQuery<Void> onRequest();

}

package com.bossymr.rapid.robot.network;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.SubscribableNetworkCall;
import com.bossymr.network.annotations.GET;
import com.bossymr.network.annotations.POST;
import com.bossymr.network.annotations.Service;
import com.bossymr.network.annotations.Subscribable;
import org.jetbrains.annotations.NotNull;

@Service("/rw/mastership")
public interface MastershipService {

    /**
     * Requests mastership for all domains.
     * <p>
     * If the robot is in manual mode, {@code Manual Mode Privilege (RMMP)} is required, see
     * {@link ManualModePrivilegeService}.
     */
    @POST("?action=request")
    @NotNull NetworkCall<Void> request();

    /**
     * Releases mastership for all domains.
     */
    @POST("?action=release")
    @NotNull NetworkCall<Void> release();

    @Subscribable("/rw/mastership")
    @NotNull SubscribableNetworkCall<MastershipEvent> onRequest();

    @GET("/cfg")
    @NotNull NetworkCall<MastershipDomain> getConfigurationDomain();

    @GET("/motion")
    @NotNull NetworkCall<MastershipDomain> getMotionDomain();

    @GET("/rapid")
    @NotNull NetworkCall<MastershipDomain> getRapidDomain();


}

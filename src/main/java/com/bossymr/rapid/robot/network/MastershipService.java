package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Service;
import com.bossymr.rapid.robot.network.query.Query;
import com.bossymr.rapid.robot.network.query.Query.GET;
import com.bossymr.rapid.robot.network.query.Query.POST;
import com.bossymr.rapid.robot.network.query.SubscribableQuery;
import com.bossymr.rapid.robot.network.query.SubscribableQuery.Subscribable;
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
    @NotNull Query<Void> request();

    /**
     * Releases mastership for all domains.
     */
    @POST("?action=release")
    @NotNull Query<Void> release();

    @Subscribable("/rw/mastership")
    @NotNull SubscribableQuery<MastershipEvent> onRequest();

    @GET("/cfg")
    @NotNull Query<MastershipDomain> getConfigurationDomain();

    @GET("/motion")
    @NotNull Query<MastershipDomain> getMotionDomain();

    @GET("/rapid")
    @NotNull Query<MastershipDomain> getRapidDomain();


}

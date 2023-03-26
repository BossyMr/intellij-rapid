package com.bossymr.rapid.robot.network.robotware.mastership;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.ServiceModel;
import com.bossymr.network.SubscribableNetworkCall;
import com.bossymr.network.annotations.*;
import com.bossymr.rapid.robot.network.ManualModePrivilegeService;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code Service} used to handle mastership.
 */
@Service("/rw/mastership")
public interface MastershipService extends ServiceModel {

    /**
     * Requests mastership for all mastership domains.
     * <p>
     * If the robot is in manual mode, {@code Manual Mode Privilege (RMMP)} is required, see
     * {@link ManualModePrivilegeService}.
     */
    @POST(arguments = "action=request")
    @NotNull NetworkCall<Void> request();

    /**
     * Releases mastership for all mastership domains.
     */
    @POST(arguments = "action=release")
    @NotNull NetworkCall<Void> release();

    /**
     * Subscribes to changes to mastership for all mastership domains.
     */
    @Subscribable("/rw/mastership")
    @NotNull SubscribableNetworkCall<MastershipEvent> onRequest();

    /**
     * Returns the specified mastership domain.
     *
     * @param mastershipType the mastership type.
     * @return the specified mastership domain.
     */
    @GET("/{domain}")
    @NotNull NetworkCall<MastershipDomain> getDomain(
            @NotNull @Path("domain") MastershipType mastershipType
    );
}

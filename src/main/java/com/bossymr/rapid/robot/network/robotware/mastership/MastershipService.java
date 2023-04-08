package com.bossymr.rapid.robot.network.robotware.mastership;

import com.bossymr.network.SubscribableNetworkQuery;
import com.bossymr.network.annotations.*;
import com.bossymr.network.NetworkQuery;
import com.bossymr.rapid.robot.network.ManualModePrivilegeService;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code Service} used to handle mastership.
 */
@Service("/rw/mastership")
public interface MastershipService {

    /**
     * Requests mastership for all mastership domains.
     * <p>
     * If the robot is in manual mode, {@code Manual Mode Privilege (RMMP)} is required, see
     * {@link ManualModePrivilegeService}.
     */
    @Fetch(method = FetchMethod.POST, value = "", arguments = "action=request")
  @NotNull NetworkQuery<Void> request();

    /**
     * Releases mastership for all mastership domains.
     */
    @Fetch(method = FetchMethod.POST, value = "", arguments = "action=release")
  @NotNull NetworkQuery<Void> release();

    /**
     * Subscribes to changes to mastership for all mastership domains.
     */
    @Subscribable("/rw/mastership")
    @NotNull SubscribableNetworkQuery<MastershipEvent> onRequest();

    /**
     * Returns the specified mastership domain.
     *
     * @param mastershipType the mastership type.
     * @return the specified mastership domain.
     */
        @Fetch("/{domain}")
  @NotNull NetworkQuery<MastershipDomain> getDomain(@NotNull @Path("domain") MastershipType mastershipType);
}

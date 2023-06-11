package com.bossymr.rapid.robot.network.robotware.mastership;

import com.bossymr.network.NetworkQuery;
import com.bossymr.network.SubscribableNetworkQuery;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Fetch;
import com.bossymr.network.annotations.Property;
import com.bossymr.network.annotations.Subscribable;
import com.bossymr.network.client.FetchMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code MastershipDomain} represents a specific mastership domain.
 */
@Entity("msh-resource")
public interface MastershipDomain {

    /**
     * The identifier of the user currently holding mastership for this domain.
     *
     * @return the identifier of the user, or {@code null} if mastership is not currently being held.
     */
    @Property("uid")
    @Nullable String getIdentifier();

    /**
     * Returns the application currently holding mastership for this domain.
     *
     * @return the application, or {@code null} if mastership is currently not being held.
     */
    @Property("application")
    @Nullable String getApplication();

    /**
     * Returns the alternate name of the location currently holding mastership.
     *
     * @return the alternate name of the location.
     */
    @Property("alias")
    @Nullable String getAlias();

    /**
     * Returns the location of the application currently holding mastership.
     *
     * @return the location of the application.
     */
    @Property("location")
    @Nullable String getLocation();

    /**
     * Checks if mastership is currently being held by this user.
     *
     * @return if the master is currently being held by this user.
     */
    @Property("mastershipheldbyme")
    @Nullable Boolean isHolding();

    /**
     * Returns the current mastership status.
     *
     * @return the current mastership status.
     */
    @Property("mastership")
    @NotNull MastershipStatus getStatus();

    /**
     * Requests mastership for this mastership domain.
     */
    @Fetch(method = FetchMethod.POST, value = "{@self}", arguments = "action=request")
  @NotNull NetworkQuery<Void> request();

    /**
     * Releases mastership for this mastership domain.
     */
    @Fetch(method = FetchMethod.POST, value = "{@self}", arguments = "action=release")
  @NotNull NetworkQuery<Void> release();

    /**
     * Subscribes to changes to mastership fot this mastership domain.
     */
    @Subscribable("{@self}}")
    @NotNull SubscribableNetworkQuery<MastershipEvent> onRequest();

}
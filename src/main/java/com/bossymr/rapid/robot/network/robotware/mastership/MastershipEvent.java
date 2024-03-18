package com.bossymr.rapid.robot.network.robotware.mastership;

import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code MastershipEvent} represents an event for a mastership change.
 */
@Entity("msh-resource-value")
public interface MastershipEvent {

    /**
     * Checks if mastership is currently being held.
     *
     * @return if mastership is currently being held.
     */
    @Property("holdmastership")
    boolean isHolding();

    /**
     * Returns the identifier of the user currently holding mastership.
     *
     * @return the identifier of the user, or {@code null} if mastership is currently not being held.
     */
    @Property("uid")
    @Nullable String getIdentifier();

    /**
     * Returns the location of the application currently holding mastership.
     *
     * @return the location of the application, or {@code null} if mastership is currently not being held.
     */
    @Property("location")
    @Nullable String getLocation();

    /**
     * Returns the application of the user currently holding mastership.
     *
     * @return the application of the user, or {@code null} if mastership is currently not being held.
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
     * Returns the current mastership status.
     *
     * @return the current mastership status.
     */
    @Property("mastership")
    @NotNull MastershipStatus getStatus();

}

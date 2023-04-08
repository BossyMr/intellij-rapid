package com.bossymr.rapid.robot.network;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Request Manual Mode Privilege (RMMP) state which contains information regarding the user requesting elevated
 * privilege.
 */
@Entity("user-rmmp")
public interface ManualModePrivilegeState {

    /**
     * Returns the user identifier.
     *
     * @return the user identifier.
     */
    @Property("userid")
    @NotNull String getIdentifier();

    /**
     * Returns the alias of the user. The alias for a Window PC user is the user's computer username.
     *
     * @return the alias of the user, or {@code null} if a user is not requesting manual mode privilege.
     */
    @Property("alias")
    @Nullable String getAlias();

    /**
     * Returns the location of the user. The location for a PC user is the PC's network name.
     *
     * @return the location of the user, or {@code null} if a user is not requesting manual mode privilege.
     */
    @Property("location")
    @Nullable String getLocation();

    /**
     * Returns the application of the user.
     *
     * @return the application of the user, or {@code null} if a user is not requesting manual mode privilege.
     */
    @Property("application")
    @Nullable String getApplication();

    /**
     * Returns the privilege of the user.
     *
     * @return the privilege of the user.
     */
    @Property("privilege")
    @NotNull ManualModePrivilege getPrivilege();

    /**
     * Checks if the privilege request and this request are made by the same user.
     *
     * @return if the privilege request and this request are made by the same user.
     */
    @Property("rmmpheldbyme")
    boolean isHolding();

}

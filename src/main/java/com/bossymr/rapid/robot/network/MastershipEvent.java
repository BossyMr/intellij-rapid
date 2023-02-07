package com.bossymr.rapid.robot.network;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.Nullable;

@Entity("msh-resource-value")
public interface MastershipEvent extends EntityModel {

    /**
     * Checks if mastership is currently being held.
     *
     * @return if the master is currently being held.
     */
    @Property("holdmastership")
    boolean isHolding();

    @Property("uid")
    @Nullable String getIdentifier();

    @Property("location")
    @Nullable String getLocation();

    /**
     * Returns the application of the user.
     *
     * @return the application of the user, or {@code null} if mastership is currently not being held.
     */
    @Property("application")
    @Nullable String getApplication();

    @Property("alias")
    @Nullable String getAlias();


}

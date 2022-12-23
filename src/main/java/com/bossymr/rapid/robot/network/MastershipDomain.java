package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.POST;
import com.bossymr.rapid.robot.network.annotations.Property;
import com.bossymr.rapid.robot.network.query.Query;
import com.bossymr.rapid.robot.network.query.SubscribableQuery;
import com.bossymr.rapid.robot.network.query.SubscribableQuery.Subscribable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity("msh-resource")
public interface MastershipDomain extends EntityModel {

    @Property("uid")
    @Nullable String getIdentifier();

    /**
     * Returns the application of the user.
     *
     * @return the application of the user, or {@code null} if mastership is currently not being held.
     */
    @Property("application")
    @Nullable String getApplication();

    @Property("alias")
    @Nullable String getAlias();

    @Property("location")
    @Nullable String getLocation();

    /**
     * Checks if mastership is currently being held by this user.
     *
     * @return if the master is currently being held by this user.
     */
    @Property("mastershipheldbyme")
    boolean isHolding();

    @POST("{@self}?action=request")
    @NotNull Query<Void> request();

    @POST("{@self}?action=release")
    @NotNull Query<Void> release();

    @Subscribable("{@self}}")
    @NotNull SubscribableQuery<MastershipEvent> onRequest();

}

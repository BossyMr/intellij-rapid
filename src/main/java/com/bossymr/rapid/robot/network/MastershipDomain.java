package com.bossymr.rapid.robot.network;

import com.bossymr.network.EntityModel;
import com.bossymr.network.NetworkCall;
import com.bossymr.network.SubscribableNetworkCall;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.POST;
import com.bossymr.network.annotations.Property;
import com.bossymr.network.annotations.Subscribable;
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
    @NotNull NetworkCall<Void> request();

    @POST("{@self}?action=release")
    @NotNull NetworkCall<Void> release();

    @Subscribable("{@self}}")
    @NotNull SubscribableNetworkCall<MastershipEvent> onRequest();

}

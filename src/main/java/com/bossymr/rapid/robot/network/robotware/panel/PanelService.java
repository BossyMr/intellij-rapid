package com.bossymr.rapid.robot.network.robotware.panel;

import com.bossymr.network.NetworkQuery;
import com.bossymr.network.SubscribableNetworkQuery;
import com.bossymr.network.annotations.Fetch;
import com.bossymr.network.annotations.Field;
import com.bossymr.network.annotations.Service;
import com.bossymr.network.annotations.Subscribable;
import com.bossymr.network.client.FetchMethod;
import org.jetbrains.annotations.NotNull;

@Service("/rw/panel")
public interface PanelService {

    @Fetch("/ctrlstate")
    @NotNull NetworkQuery<ControllerStateEntity> getControllerState();

    @Fetch(method = FetchMethod.POST, value = "/ctrlstate", arguments = "action=setctrlstate")
    @NotNull NetworkQuery<Void> setControllerState(
            @NotNull @Field("ctrl-state") ControllerStateEntity state
    );

    @Subscribable("/rw/panel/ctrlstate")
    @NotNull SubscribableNetworkQuery<ControllerStateEntity> onControllerState();

}

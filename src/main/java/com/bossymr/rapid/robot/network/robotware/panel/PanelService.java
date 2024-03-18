package com.bossymr.rapid.robot.network.robotware.panel;

import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.SubscribableNetworkQuery;
import com.bossymr.rapid.robot.api.annotations.Fetch;
import com.bossymr.rapid.robot.api.annotations.Field;
import com.bossymr.rapid.robot.api.annotations.Service;
import com.bossymr.rapid.robot.api.annotations.Subscribable;
import com.bossymr.rapid.robot.api.client.FetchMethod;
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

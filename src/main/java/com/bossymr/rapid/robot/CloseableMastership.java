package com.bossymr.rapid.robot;

import com.bossymr.network.NetworkAction;
import com.bossymr.network.NetworkManager;
import com.bossymr.network.client.NetworkRequest;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipDomain;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipService;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface CloseableMastership extends AutoCloseable {

    static @NotNull CloseableMastership withMastership(@NotNull NetworkManager manager, @NotNull MastershipType mastershipType) throws IOException, InterruptedException {
        NetworkAction action = new NetworkAction(manager) {
            @Override
            protected boolean onFailure(@NotNull NetworkRequest<?> request, @NotNull Throwable throwable) {
                return false;
            }
        };
        MastershipService mastershipService = manager.createService(MastershipService.class);
        MastershipDomain mastershipDomain = mastershipService.getDomain(mastershipType).get();
        Boolean isHolding = mastershipDomain.isHolding();
        if (isHolding != null && isHolding) {
            return action::close;
        } else {
            mastershipDomain.request().get();
            return () -> {
                mastershipDomain.release().get();
                action.close();
            };
        }
    }

    @Override
    void close() throws IOException, InterruptedException;
}

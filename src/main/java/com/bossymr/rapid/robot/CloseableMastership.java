package com.bossymr.rapid.robot;

import com.bossymr.network.client.NetworkAction;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipDomain;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipService;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface CloseableMastership extends AutoCloseable {

    static @NotNull CloseableMastership withMastership(@NotNull NetworkAction action, @NotNull MastershipType mastershipType) throws IOException, InterruptedException {
        MastershipService mastershipService = action.createService(MastershipService.class);
        MastershipDomain mastershipDomain = mastershipService.getDomain(mastershipType).get();
        Boolean isHolding = mastershipDomain.isHolding();
        if (isHolding != null && isHolding) {
            return () -> {};
        } else {
            mastershipDomain.request().get();
            return () -> mastershipDomain.release().get();
        }
    }

    @Override
    void close() throws IOException, InterruptedException;
}

package com.bossymr.rapid.robot;

import com.bossymr.network.client.NetworkManager;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipDomain;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipService;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import org.jetbrains.annotations.NotNull;

public interface CloseableMastership extends AutoCloseable {

    static @NotNull CloseableMastership withMastership(@NotNull NetworkManager manager, @NotNull MastershipType mastershipType) {
        MastershipService mastershipService = manager.createService(MastershipService.class);
        MastershipDomain mastershipDomain = mastershipService.getDomain(mastershipType);
        if (Boolean.FALSE.equals(mastershipDomain.isHolding())) {
            mastershipDomain.request();
            return mastershipDomain::release;
        } else {
            return () -> {};
        }
    }

    @Override
    void close();
}

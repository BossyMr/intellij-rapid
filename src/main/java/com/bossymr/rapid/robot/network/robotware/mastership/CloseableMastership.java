package com.bossymr.rapid.robot.network.robotware.mastership;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface CloseableMastership extends AutoCloseable {

    static @NotNull CloseableMastership request(@NotNull MastershipService service) throws IOException, InterruptedException {
        service.request().send();
        return () -> service.release().send();
    }

    static @NotNull CloseableMastership request(@NotNull MastershipDomain domain) throws IOException, InterruptedException {
        domain.request().send();
        return () -> domain.release().send();
    }

    @Override
    void close() throws InterruptedException, IOException;
}

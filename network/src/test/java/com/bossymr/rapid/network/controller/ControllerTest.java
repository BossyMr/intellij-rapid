package com.bossymr.rapid.network.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class ControllerTest {

    private Controller controller;

    @BeforeEach
    void setUp() {
        controller = Controller.connect(URI.create("http://localhost:80/"), "Default User", "robotics");
    }

    @Test
    void getIdentifier() {
        CompletableFuture<IdentityEntity> networkQuery = controller.getIdentity();
        IdentityEntity identity = networkQuery.join();
        Assertions.assertNotNull(identity.name());
        Assertions.assertNull(identity.identifier());
    }
}

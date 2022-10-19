package com.bossymr.rapid.robot.network.controller;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ControllerTest {

    private Controller controller;

    @Before
    public void setUp() throws IOException {
        controller = Controller.connect(URI.create("http://localhost:80/"), "Default User", "robotics");
    }

    @Test
    public void getIdentifier() {
        CompletableFuture<IdentityEntity> networkQuery = controller.getIdentity();
        IdentityEntity identity = networkQuery.join();
        assertNotNull(identity.name());
        assertNull(identity.identifier());
    }
}

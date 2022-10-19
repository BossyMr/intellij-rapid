package com.bossymr.rapid.robot.network.controller.event;

import com.bossymr.rapid.robot.network.controller.Controller;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EventLogTest {

    private Controller controller;

    @Before
    public void setUp() throws IOException {
        controller = Controller.connect(URI.create("http://localhost:80/"), "Default User", "robotics");
    }

    @Test
    public void getEvents() {
        CompletableFuture<List<EventLogCategory>> categoryNetworkQuery = controller.getEventLog().getCategories();
        List<EventLogCategory> categories = categoryNetworkQuery.join();
        assertTrue(categories.size() > 0);
        EventLogCategory category = categories.get(0);
        CompletableFuture<EventLogCategoryEntity> categoryEntity = category.getEntity();
        EventLogCategoryEntity entity = categoryEntity.join();
        assertNotNull(entity.name());
        CompletableFuture<List<EventLogMessageEntity>> messageNetworkQuery = category.getMessages();
        List<EventLogMessageEntity> messages = messageNetworkQuery.join();
        assertTrue(messages.size() > 0);
    }

}
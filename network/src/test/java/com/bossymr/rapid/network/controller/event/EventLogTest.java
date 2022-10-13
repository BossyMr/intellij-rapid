package com.bossymr.rapid.network.controller.event;

import com.bossymr.rapid.network.Credentials;
import com.bossymr.rapid.network.controller.Controller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class EventLogTest {

    private Controller controller;

    @BeforeEach
    void setUp() {
        Credentials credentials = new Credentials("Default User", "robotics".toCharArray());
        controller = Controller.connect(URI.create("http://localhost:80/"), credentials);
    }

    @Test
    void getEvents() {
        CompletableFuture<List<EventLogCategory>> categoryNetworkQuery = controller.getEventLog().getCategories();
        List<EventLogCategory> categories = categoryNetworkQuery.join();
        Assertions.assertTrue(categories.size() > 0);
        EventLogCategory category = categories.get(0);
        CompletableFuture<EventLogCategoryEntity> categoryEntity = category.getEntity();
        EventLogCategoryEntity entity = Assertions.assertDoesNotThrow(categoryEntity::join);
        Assertions.assertNotNull(entity.name());
        CompletableFuture<List<EventLogMessageEntity>> messageNetworkQuery = category.getMessages();
        List<EventLogMessageEntity> messages = messageNetworkQuery.join();
        Assertions.assertTrue(messages.size() > 0);
    }

}
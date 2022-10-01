package com.bossymr.rapid.network;

import com.bossymr.rapid.network.controller.Controller;
import com.bossymr.rapid.network.controller.IdentityEntity;
import com.bossymr.rapid.network.controller.event.EventLogCategory;
import com.bossymr.rapid.network.controller.event.EventLogMessageEntity;
import com.bossymr.rapid.network.controller.rapid.SymbolEntity;
import com.bossymr.rapid.network.controller.rapid.SymbolSearchQuery;
import com.bossymr.rapid.network.controller.rapid.SymbolType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

public class ControllerTest {

    private Controller controller;

    @BeforeEach
    void setUp() {
        Credentials credentials = new Credentials("Default User", "robotics".toCharArray());
        controller = Controller.connect(URI.create("http://localhost:80/"), credentials);
    }

    @Test
    void getIdentifier() {
        NetworkQuery<IdentityEntity> networkQuery = controller.getIdentity();
        IdentityEntity identity = networkQuery.sendAsync().join();
        Assertions.assertNotNull(identity.name());
        Assertions.assertNull(identity.identifier());
    }

    @Test
    void searchSymbols() {
        SymbolSearchQuery query = SymbolSearchQuery.newBuilder()
                .setSymbolType(SymbolType.ATOMIC)
                .build();
        NetworkQuery<List<SymbolEntity>> networkQuery = controller.getRapid().getSymbols(query);
        List<SymbolEntity> symbols = networkQuery.sendAsync().join();
        Assertions.assertTrue(symbols.size() > 0);
    }

    @Test
    void getEvents() {
        NetworkQuery<List<EventLogCategory>> categoryNetworkQuery = controller.getEventLog().getCategories();
        List<EventLogCategory> categories = categoryNetworkQuery.sendAsync().join();
        Assertions.assertTrue(categories.size() > 0);
        EventLogCategory category = categories.get(0);
        NetworkQuery<List<EventLogMessageEntity>> messageNetworkQuery = category.getMessages();
        List<EventLogMessageEntity> messages = messageNetworkQuery.sendAsync().join();
        Assertions.assertTrue(messages.size() > 0);
    }
}

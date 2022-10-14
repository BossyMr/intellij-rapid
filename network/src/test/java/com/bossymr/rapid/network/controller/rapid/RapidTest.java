package com.bossymr.rapid.network.controller.rapid;

import com.bossymr.rapid.network.controller.Controller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class RapidTest {

    private Controller controller;

    @BeforeEach
    void setUp() {
        controller = Controller.connect(URI.create("http://localhost:80/"), "Default User", "robotics");
    }

    @Test
    void searchSymbols() {
        SymbolSearchQuery query = SymbolSearchQuery.newBuilder()
                .setSymbolType(SymbolType.ANY)
                .build();
        CompletableFuture<List<SymbolEntity>> networkQuery = controller.getRapid().getSymbols(query);
        List<SymbolEntity> symbols = networkQuery.join();
        Assertions.assertEquals(1060, symbols.size());
    }

    @Test
    void getTasks(@TempDir Path directory) {
        CompletableFuture<List<Task>> taskQuery = controller.getRapid().getTasks();
        List<Task> tasks = taskQuery.join();
        assertTrue(tasks.size() > 0);
        Task task = tasks.get(0);
        assertDoesNotThrow(() -> task.getEntity().join());
        CompletableFuture<List<Module>> moduleQuery = task.getModules();
        List<Module> modules = moduleQuery.join();
        assertTrue(modules.size() > 0);
        Module module = modules.get(0);
        CompletableFuture<Void> saveQuery = module.save(directory.toFile().toPath());
        assertDoesNotThrow(saveQuery::join);
        File file = directory.toFile();
        String[] files = file.list();
        assertNotNull(files);
        assertTrue(files.length > 0);
    }
}
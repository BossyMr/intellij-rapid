package com.bossymr.rapid.robot.network.controller.rapid;

import com.bossymr.rapid.robot.network.controller.Controller;
import com.intellij.openapi.util.io.FileUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

public class RapidTest {

    private Controller controller;

    @Before
    public void setUp() {
        controller = Controller.connect(URI.create("http://localhost:80/"), "Default User", "robotics");
    }

    @Test
    public void searchSymbols() {
        SymbolSearchQuery query = SymbolSearchQuery.newBuilder()
                .setSymbolType(SymbolType.ANY)
                .build();
        CompletableFuture<List<SymbolEntity>> networkQuery = controller.getRapid().getSymbols(query);
        List<SymbolEntity> symbols = networkQuery.join();
        assertEquals(3192, symbols.size());
    }

    @Test
    public void getTasks() throws IOException {
        CompletableFuture<List<Task>> taskQuery = controller.getRapid().getTasks();
        List<Task> tasks = taskQuery.join();
        assertTrue(tasks.size() > 0);
        Task task = tasks.get(0);
        task.getEntity().join();
        CompletableFuture<List<Module>> moduleQuery = task.getModules();
        List<Module> modules = moduleQuery.join();
        assertTrue(modules.size() > 0);
        Module module = modules.get(0);
        File directory = FileUtil.createTempDirectory("RAPID", "", true);
        CompletableFuture<Void> saveQuery = module.save(directory.toPath());
        saveQuery.join();
        String[] files = directory.list();
        assertNotNull(files);
        assertTrue(files.length > 0);
    }
}
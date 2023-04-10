package com.bossymr.rapid.robot;

import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.network.NetworkTestUtil;
import com.intellij.testFramework.junit5.TestApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIf("com.bossymr.rapid.robot.network.NetworkTestUtil#doNetworkTest")
@TestApplication
public class RobotTest {

    private static RapidRobot robot;

    @BeforeEach
    void setUp() throws InterruptedException, IOException {
        RemoteRobotService remoteService = RemoteRobotService.getInstance();
        robot = remoteService.connect(NetworkTestUtil.DEFAULT_PATH, NetworkTestUtil.DEFAULT_CREDENTIALS);
    }

    @AfterEach
    void tearDown() throws InterruptedException, IOException {
        RemoteRobotService remoteService = RemoteRobotService.getInstance();
        remoteService.disconnect();
    }

    @Test
    public void symbol() throws InterruptedException, IOException {
        Set<VirtualSymbol> symbols = robot.getSymbols();
        VirtualSymbol symbol = robot.getSymbol("num");
        assertTrue(symbols.contains(symbol));
    }

    @Test
    public void fetchSymbol() throws InterruptedException, IOException {
        String symbolName = "TPErase";
        Set<String> names = robot.getSymbols().stream()
                .map(VirtualSymbol::getName)
                .collect(Collectors.toSet());
        // Symbol is not persisted
        assertFalse(names.contains(symbolName));
        // Symbol is now persisted
        VirtualSymbol symbol = robot.getSymbol(symbolName);
        assertNotNull(symbol);
        assertTrue(robot.getSymbols().contains(symbol));
        // Symbol is still persisted
        assertEquals(symbol, robot.getSymbol(symbolName));
    }
}
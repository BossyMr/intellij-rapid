package com.bossymr.rapid.robot;

import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.impl.RobotImpl;
import com.bossymr.rapid.robot.network.Controller;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;

public class RobotTest extends BasePlatformTestCase {

    public void testConnect() throws IOException {
        Robot robot = new RobotImpl(Controller.connect(URI.create("http://localhost:80/"), Controller.DEFAULT_CREDENTIALS));
        assertTrue(robot.isConnected());
        robot.disconnect();
        assertFalse(robot.isConnected());
    }

    public void testState() {
        try {
            new RobotImpl(new RobotState());
            fail();
        } catch (IllegalArgumentException ignored) {}
    }

    public void testSymbol() throws IOException {
        Robot robot = new RobotImpl(Controller.connect(URI.create("http://localhost:80/"), Controller.DEFAULT_CREDENTIALS));
        assertTrue(robot.isConnected());
        Set<VirtualSymbol> symbols = robot.getSymbols();
        assertNotEmpty(symbols);
        RobotService instance = RobotService.getInstance();
        assertNotNull(instance.getRobotState());
        assertNotEmpty(instance.getRobotState().symbols);
        Set<String> names = symbols.stream()
                .map(RapidSymbol::getName)
                .collect(Collectors.toSet());
        assertTrue(names.contains("num"));
        assertNotNull(robot.getSymbol("num"));
        assertNull(robot.getSymbol("")); // Symbol is empty.
        assertNull(robot.getSymbol("unknown")); // Symbol does not exist.
        assertContainsElements(instance.getRobotState().cache, "", "unknown"); // Symbols were not found
        robot.disconnect();
        assertFalse(names.contains("TPErase"));
        assertNull(robot.getSymbol("TPErase")); // Symbol is not provided.
        robot.reconnect();
        assertNotNull(robot.getSymbol("TPErase")); // Symbol was found.
        robot.disconnect();
        assertNotNull(robot.getSymbol("TPErase")); // Symbol was persisted.
        robot.reconnect();
    }
}

package com.bossymr.rapid.robot;

import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.network.NetworkTestUtil;
import com.intellij.testFramework.LightIdeaTestCase;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@EnabledIf("com.bossymr.rapid.robot.network.NetworkTestUtil#doNetworkTest")
public class RobotTest extends LightIdeaTestCase {

    private static Robot robot;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        RemoteService remoteService = RemoteService.getInstance();
        robot = remoteService.connect(NetworkTestUtil.DEFAULT_PATH, NetworkTestUtil.DEFAULT_CREDENTIALS);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        RemoteService remoteService = RemoteService.getInstance();
        remoteService.disconnect();
    }

    public void testSymbol() throws IOException, InterruptedException {
        Set<VirtualSymbol> symbols = robot.getSymbols();
        VirtualSymbol symbol = robot.getSymbol("num");
        assertTrue(symbols.contains(symbol));
    }

    public void testFetchSymbol() throws IOException, InterruptedException {
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
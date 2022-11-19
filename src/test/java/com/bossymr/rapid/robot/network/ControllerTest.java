package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.RobotState.SymbolState;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

public class ControllerTest {

    private static Controller controller;

    @BeforeClass
    public static void beforeClass() {
        controller = Controller.connect(URI.create("http://localhost:80/"), Controller.DEFAULT_CREDENTIALS);
    }

    @Test
    public void getIdentity() throws IOException {
        controller.getName();
    }

    @Test
    public void getEmptySymbol() throws IOException {
        Assert.assertNull(controller.getSymbol(""));
    }

    @Test
    public void getUnknownSymbol() throws IOException {
        Assert.assertNull(controller.getSymbol("unknown"));
    }

    @Test
    public void getSymbol() throws IOException {
        Assert.assertNotNull(controller.getSymbol("num"));
    }

    @Test
    public void getSymbols() throws IOException {
        Set<SymbolState> states = controller.getSymbols();
        System.out.println(states.size());
        Assert.assertTrue(states.size() > 0);
    }
}

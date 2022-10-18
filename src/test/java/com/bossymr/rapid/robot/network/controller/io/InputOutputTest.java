package com.bossymr.rapid.robot.network.controller.io;

import com.bossymr.rapid.robot.network.controller.Controller;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class InputOutputTest {

    private Controller controller;

    @Before
    public void setUp() {
        controller = Controller.connect(URI.create("http://localhost:80/"), "Default User", "robotics");
    }

    @Test
    public void getNetworks() {
        List<Network> networks = controller.getInputOutput().getNetworks().join();
        assertEquals(5, networks.size());
        Network network = networks.get(0);
        network.getEntity().join();
        List<Device> devices = network.getDevices().join();
        assertEquals(1, devices.size());
        Device device = devices.get(0);
        device.getEntity().join();
        List<Signal> signals = device.getSignals().join();
        assertEquals(86, signals.size());
        Signal signal = signals.get(0);
        signal.getEntity().join();
    }
}
package com.bossymr.rapid.network.controller.io;

import com.bossymr.rapid.network.controller.Controller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InputOutputTest {

    private Controller controller;

    @BeforeEach
    void setUp() {
        controller = Controller.connect(URI.create("http://localhost:80/"), "Default User", "robotics");
    }

    @Test
    void getNetworks() {
        List<Network> networks = Assertions.assertDoesNotThrow(() -> controller.getInputOutput().getNetworks().get());
        assertEquals(5, networks.size());
        Network network = networks.get(0);
        assertDoesNotThrow(() -> network.getEntity().get());
        List<Device> devices = assertDoesNotThrow(() -> network.getDevices().get());
        assertEquals(1, devices.size());
        Device device = devices.get(0);
        assertDoesNotThrow(() -> device.getEntity().get());
        List<Signal> signals = assertDoesNotThrow(() -> device.getSignals().get());
        assertEquals(86, signals.size());
        Signal signal = signals.get(0);
        assertDoesNotThrow(() -> signal.getEntity().get());
    }
}
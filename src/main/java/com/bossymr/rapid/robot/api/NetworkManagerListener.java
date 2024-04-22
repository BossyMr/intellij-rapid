package com.bossymr.rapid.robot.api;

import java.io.IOException;

public interface NetworkManagerListener {
    void onClose() throws IOException, InterruptedException;
}

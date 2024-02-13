package com.bossymr.rapid.robot;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class MastershipException extends IOException {

    public MastershipException(@NotNull MastershipType mastershipType, @Nullable String application) {
        super(RapidBundle.message("notification.title.robot.mastership.error", mastershipType, application != null ? application : "<unknown>"));
    }
}

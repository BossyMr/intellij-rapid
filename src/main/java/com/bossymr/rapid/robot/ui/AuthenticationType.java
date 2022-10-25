package com.bossymr.rapid.robot.ui;

import com.bossymr.rapid.RapidBundle;
import org.jetbrains.annotations.NotNull;

public enum AuthenticationType {
    PASSWORD(RapidBundle.message("robot.connect.authentication.type.password")),
    DEFAULT(RapidBundle.message("robot.connect.authentication.type.default"));

    private final String displayName;

    AuthenticationType(@NotNull String displayName) {
        this.displayName = displayName;
    }

    public @NotNull String getDisplayName() {
        return displayName;
    }
}

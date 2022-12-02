package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.impl.RobotUtil;
import com.intellij.model.Pointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class VirtualPointer implements Pointer<VirtualSymbol> {

    private final String name;

    public VirtualPointer(@NotNull String name) {
        this.name = name;
    }

    @Override
    public @Nullable VirtualSymbol dereference() {
        RobotService instance = RobotService.getInstance();
        Robot robot = instance.getRobot();
        if (robot == null) return null;
        try {
            return robot.getSymbol(name);
        } catch (IOException e) {
            RobotUtil.showNotification();
            return null;
        }
    }
}

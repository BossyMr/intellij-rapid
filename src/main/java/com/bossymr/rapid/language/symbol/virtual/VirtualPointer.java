package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidPointer;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

/**
 * A {@code VirtualPointer} attempts to restore a {@link VirtualSymbol} on the connected robot.
 *
 * @param <T> the type of the underlying element.
 */
public class VirtualPointer<T extends VirtualSymbol> implements RapidPointer<T> {

    private final @NotNull String name;
    private final @NotNull Class<? extends T> type;

    /**
     * Creates a new {@code VirtualPointer} which will attempt to restore the specified symbol.
     *
     * @param symbol the symbol.
     * @param type the type of the symbol.
     */
    public VirtualPointer(@NotNull T symbol, @NotNull Class<? extends T> type) {
        this.name = symbol.getCanonicalName();
        this.type = type;
    }

    @Override
    public @Nullable T dereference() {
        RobotService service = RobotService.getInstance();
        RapidRobot robot = service.getRobot();
        if (robot == null) {
            return null;
        }
        try {
            VirtualSymbol symbol = robot.getSymbol(name);
            if (type.isInstance(symbol)) {
                return type.cast(symbol);
            }
            return null;
        } catch (IOException e) {
            return null;
        } catch (InterruptedException e) {
            throw new ProcessCanceledException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualPointer<?> that = (VirtualPointer<?>) o;
        return Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return "VirtualPointer{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}

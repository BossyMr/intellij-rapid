package com.bossymr.rapid.language.psi.light;

import com.bossymr.rapid.language.psi.RapidComponent;
import com.bossymr.rapid.language.psi.RapidRecord;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class LightRecord extends LightSymbol implements RapidRecord {

    private final String name;
    private final List<RapidComponent> components;

    public LightRecord(@NotNull String name, @NotNull List<RapidComponent> components) {
        this.name = name;
        this.components = components;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull List<RapidComponent> getComponents() {
        return components;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LightRecord that = (LightRecord) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getComponents(), that.getComponents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getComponents());
    }

    @Override
    public String toString() {
        return "LightRecord:" + getName();
    }
}

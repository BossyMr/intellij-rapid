package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record VirtualComponent(
        @NotNull VirtualRecord record,
        @NotNull String name,
        @NotNull RapidType type
) implements RapidComponent, VirtualSymbol {

    @Override
    public @NotNull RapidType getType() {
        return type();
    }

    @Override
    public @NotNull RapidRecord getRecord() {
        return record();
    }

    @Override
    public @NotNull String getName() {
        return name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualComponent that = (VirtualComponent) o;
        return getName().equals(that.getName()) && getType().equals(that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getType());
    }

    @Override
    public String toString() {
        return "VirtualComponent{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}

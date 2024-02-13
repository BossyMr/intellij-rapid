package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class VirtualComponent implements RapidComponent, VirtualSymbol {

    private final @NotNull VirtualRecord record;
    private final @NotNull String name;
    private final @NotNull RapidType type;

    public VirtualComponent(@NotNull VirtualRecord record, @NotNull String name, @NotNull RapidType type) {
        this.record = record;
        this.name = name;
        this.type = type;
    }

    @Override
    public @NotNull RapidType getType() {
        return type;
    }

    @Override
    public @NotNull RapidRecord getRecord() {
        return record;
    }

    @Override
    public @NotNull String getCanonicalName() {
        return RapidComponent.super.getCanonicalName();
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull VirtualPointer<VirtualComponent> createPointer() {
        return new VirtualPointer<>(this, getClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualComponent that = (VirtualComponent) o;
        return Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return "VirtualComponent{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}

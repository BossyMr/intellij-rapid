package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.symbol.Visibility;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VirtualRecord implements RapidRecord, VirtualStructure {

    private final @NotNull String name;
    private final @NotNull List<VirtualComponent> components;

    public VirtualRecord(@NotNull String name, @NotNull List<VirtualComponent> components) {
        this.name = name;
        this.components = components;
    }

    @Override
    public @NotNull Visibility getVisibility() {
        return Visibility.GLOBAL;
    }

    @Override
    public @NotNull RapidType createType() {
        return switch (name.toLowerCase()) {
            case "pos" -> RapidPrimitiveType.POSITION;
            case "orient" -> RapidPrimitiveType.ORIENTATION;
            case "pose" -> RapidPrimitiveType.POSE;
            default -> RapidRecord.super.createType();
        };
    }

    @Override
    public @NotNull List<VirtualComponent> getComponents() {
        return components;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    public static @NotNull Builder newBuilder(@NotNull String name) {
        return new Builder(name);
    }

    @Override
    public @NotNull VirtualPointer<VirtualRecord> createPointer() {
        return new VirtualPointer<>(this, getClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualRecord that = (VirtualRecord) o;
        return Objects.equals(name, that.name) && Objects.equals(components, that.components);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, components);
    }

    @Override
    public String toString() {
        return "VirtualRecord{" +
                "name='" + name + '\'' +
                ", components=" + components +
                '}';
    }

    public static class Builder {

        private final @NotNull List<Pair<String, RapidType>> components = new ArrayList<>();
        private @NotNull String name;

        public Builder(@NotNull String name) {
            this.name = name;
        }

        public @NotNull Builder setName(@NotNull String name) {
            this.name = name;
            return this;
        }

        public @NotNull Builder withComponent(@NotNull String name, @NotNull RapidType dataType) {
            // A RapidComponent needs a reference to the record, but the record is constructed yet.
            components.add(Pair.create(name, dataType));
            return this;
        }

        public @NotNull VirtualRecord build() {
            VirtualRecord record = new VirtualRecord(name, new ArrayList<>());
            for (Pair<String, RapidType> component : components) {
                record.getComponents().add(new VirtualComponent(record, component.getFirst(), component.getSecond()));
            }
            return record;
        }
    }
}

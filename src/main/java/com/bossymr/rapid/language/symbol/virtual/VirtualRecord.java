package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.Visibility;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record VirtualRecord(
        @NotNull Visibility visibility,
        @NotNull String name,
        @NotNull List<RapidComponent> components
) implements RapidRecord, VirtualSymbol {

    public VirtualRecord(@NotNull String name, @NotNull List<RapidComponent> components) {
        this(Visibility.GLOBAL, name, components);
    }

    @Override
    public @NotNull Visibility getVisibility() {
        return visibility();
    }

    @Override
    public @NotNull List<RapidComponent> getComponents() {
        return components();
    }

    @Override
    public @NotNull String getName() {
        return name();
    }

    public static @NotNull Builder newBuilder(@NotNull String name) {
        return new Builder(name);
    }

    public static class Builder {

        private final @NotNull List<TemporaryComponent> components = new ArrayList<>();
        private @NotNull Visibility visibility;
        private @NotNull String name;

        public Builder(@NotNull String name) {
            this.visibility = Visibility.GLOBAL;
            this.name = name;
        }

        public @NotNull Builder setVisibility(@NotNull Visibility visibility) {
            this.visibility = visibility;
            return this;
        }

        public @NotNull Builder setName(@NotNull String name) {
            this.name = name;
            return this;
        }

        public @NotNull Builder withComponent(@NotNull String name, @NotNull RapidType dataType) {
            components.add(new TemporaryComponent(name, dataType));
            return this;
        }

        public @NotNull VirtualRecord build() {
            VirtualRecord record = new VirtualRecord(visibility, name, new ArrayList<>());
            for (TemporaryComponent component : components) {
                record.getComponents().add(new VirtualComponent(record, component.name(), component.dataType()));
            }
            return record;
        }

        public @NotNull RapidType asType() {
            return new RapidType(build());
        }

        private record TemporaryComponent(@NotNull String name, @NotNull RapidType dataType) {}
    }
}

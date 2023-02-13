package com.bossymr.rapid.robot;

import com.bossymr.network.client.NetworkEngine;
import com.bossymr.network.model.Model;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Tag("robot")
public final class RobotState {

    @Attribute("name")
    public @NotNull String name = "";

    @Attribute("path")
    public @NotNull String path = "";

    public @NotNull Set<SymbolState> symbolStates = new HashSet<>();

    /*
     * All symbols are not provided by the robot, as some symbols have an empty "name" property.
     * As a result, if a robot is currently connected, all symbols are checked individually.
     * If a symbol actually exists, it is added as a symbol state, if it does not, it is cached
     * to avoid checking it again.
     */
    public @NotNull Set<String> cache = new HashSet<>();

    public @NotNull Set<com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolState> getSymbols(@Nullable NetworkEngine networkEngine) {
        Set<com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolState> symbolStates = new HashSet<>();
        for (SymbolState symbolState : this.symbolStates) {
            Model model = symbolState.toModel();
            com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolState state = NetworkEngine.createEntity(networkEngine, com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolState.class, model);
            if (state != null) {
                symbolStates.add(state);
            }
        }
        return symbolStates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RobotState that = (RobotState) o;
        return Objects.equals(name, that.name) && Objects.equals(path, that.path) && symbolStates.equals(that.symbolStates) && cache.equals(that.cache);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path, symbolStates, cache);
    }

    @Override
    public String toString() {
        return "RobotState{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", symbols=" + symbolStates +
                ", cache=" + cache +
                '}';
    }

    public static final class SymbolState implements Comparable<SymbolState> {

        @Attribute("title")
        public String title;

        @Attribute("type")
        public String type;

        public Map<String, String> fields;

        public Map<String, String> links;

        public @NotNull Model toModel() {
            Map<String, URI> paths = links.entrySet().stream()
                    .map(entry -> Map.entry(entry.getKey(), URI.create(entry.getValue())))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return new Model(title.toLowerCase(), type, fields, paths);
        }

        @Override
        public int compareTo(@NotNull RobotState.SymbolState o) {
            return title.compareTo(o.title);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SymbolState that = (SymbolState) o;
            return Objects.equals(title, that.title) && Objects.equals(type, that.type) && Objects.equals(fields, that.fields);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, type, fields);
        }

        @Override
        public String toString() {
            return "SymbolState{" +
                    "title='" + title + '\'' +
                    ", type='" + type + '\'' +
                    ", properties=" + fields +
                    '}';
        }
    }

}

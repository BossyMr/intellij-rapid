package com.bossymr.rapid.robot;

import com.bossymr.rapid.robot.network.SymbolState;
import com.bossymr.rapid.robot.network.client.NetworkClient;
import com.bossymr.rapid.robot.network.client.impl.EntityUtil;
import com.bossymr.rapid.robot.network.client.model.Model;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Tag("robot")
public final class PersistentRobotState {

    @Attribute("name")
    public String name;

    @Attribute("path")
    public String path;

    public @NotNull Set<StorageSymbolState> symbols = new HashSet<>();
    /*
     * All symbols are not provided by the robot, as some symbols have an empty "name" property.
     * As a result, if a robot is currently connected, all symbols are checked individually.
     * If a symbol actually exists, it is added as a symbol state, if it does not, it is cached
     * to avoid checking it again.
     */
    public @NotNull Set<String> cache = new HashSet<>();

    public static @NotNull SymbolState getSymbolState(@NotNull StorageSymbolState symbolState) {
        Map<String, Class<?>> returnTypes = EntityUtil.getReturnTypes(SymbolState.class);
        Model model = getModel(symbolState);
        assert returnTypes.containsKey(model.getType());
        return NetworkClient.newSimpleEntity(model, SymbolState.class);
    }

    private static @NotNull Model getModel(@NotNull StorageSymbolState symbolState) {
        Map<String, URI> paths = symbolState.links.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), URI.create(entry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new Model(symbolState.title, symbolState.type, symbolState.fields, paths);
    }

    public @NotNull Set<SymbolState> getSymbolStates() {
        Map<String, Class<?>> returnTypes = EntityUtil.getReturnTypes(SymbolState.class);
        Set<SymbolState> symbolStates = new HashSet<>();
        for (StorageSymbolState storageSymbolState : symbols) {
            Model model = getModel(storageSymbolState);
            if (returnTypes.containsKey(model.getType())) {
                symbolStates.add((SymbolState) NetworkClient.newSimpleEntity(model, returnTypes.get(model.getType())));
            }
        }
        return symbolStates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersistentRobotState that = (PersistentRobotState) o;
        return Objects.equals(name, that.name) && Objects.equals(path, that.path) && symbols.equals(that.symbols) && cache.equals(that.cache);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path, symbols, cache);
    }

    @Override
    public String toString() {
        return "RobotState{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", symbols=" + symbols +
                ", cache=" + cache +
                '}';
    }

    public static final class StorageSymbolState implements Comparable<StorageSymbolState> {

        @Attribute("title")
        public String title;

        @Attribute("type")
        public String type;

        public Map<String, String> fields;

        public Map<String, String> links;

        @Override
        public int compareTo(@NotNull PersistentRobotState.StorageSymbolState o) {
            return title.compareTo(o.title);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StorageSymbolState that = (StorageSymbolState) o;
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

package com.bossymr.rapid.robot;

import com.bossymr.network.client.NetworkEngine;
import com.bossymr.network.model.Model;
import com.bossymr.rapid.robot.network.robotware.io.InputOutputSignalType;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.Symbol;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Tag("robot")
public final class RobotState {

    @Attribute("name")
    public @NotNull String name = "";

    @Attribute("path")
    public @NotNull String path = "";

    public @NotNull Set<SymbolState> symbols = new HashSet<>();

    /*
     * All symbols are not provided by the robot, as some symbols have an empty "name" property.
     * As a result, if a robot is currently connected, all symbols are checked individually.
     * If a symbol actually exists, it is added as a symbol state, if it does not, it is cached
     * to avoid checking it again.
     */
    public @NotNull Set<String> cache = new HashSet<>();

    public @NotNull Set<Symbol> getSymbols(@Nullable NetworkEngine networkEngine) {
        Set<Symbol> symbols = new HashSet<>();
        for (SymbolState symbolState : this.symbols) {
            Model model = symbolState.toModel();
            Symbol state = NetworkEngine.createEntity(networkEngine, Symbol.class, model);
            if (state != null) {
                symbols.add(state);
            }
        }
        return symbols;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RobotState that = (RobotState) o;
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

    public static final class NetworkState implements Comparable<NetworkState> {

        @Attribute("name")
        public String name;

        public List<DeviceState> devices;

        @Override
        public int compareTo(@NotNull NetworkState o) {
            return name.compareTo(o.name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NetworkState that = (NetworkState) o;
            return Objects.equals(name, that.name) && Objects.equals(devices, that.devices);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, devices);
        }

        @Override
        public String toString() {
            return "NetworkState{" +
                    "name='" + name + '\'' +
                    ", devices=" + devices +
                    '}';
        }
    }

    public static final class DeviceState implements Comparable<DeviceState> {

        @Attribute("name")
        public String name;

        public List<SignalState> signals;

        @Override
        public int compareTo(@NotNull DeviceState o) {
            return name.compareTo(o.name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeviceState that = (DeviceState) o;
            return Objects.equals(name, that.name) && Objects.equals(signals, that.signals);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, signals);
        }

        @Override
        public String toString() {
            return "DeviceState{" +
                    "name='" + name + '\'' +
                    ", signals=" + signals +
                    '}';
        }
    }

    public static final class SignalState implements Comparable<SignalState> {

        @Attribute("name")
        public String name;

        @Attribute("type")
        public InputOutputSignalType type;

        @Override
        public int compareTo(@NotNull SignalState o) {
            return name.compareTo(o.name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SignalState that = (SignalState) o;
            return Objects.equals(name, that.name) && type == that.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type);
        }

        @Override
        public String toString() {
            return "SignalState{" +
                    "name='" + name + '\'' +
                    ", type=" + type +
                    '}';
        }
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

package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.virtual.*;
import com.bossymr.rapid.robot.RobotState;
import com.bossymr.rapid.robot.RobotState.SymbolState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class RobotSymbolFactory {

    private final Map<String, Map<String, SymbolState>> states;
    private final Map<String, VirtualSymbol> symbols;

    public RobotSymbolFactory(@NotNull Collection<SymbolState> symbolStates) {
        this.states = new HashMap<>();
        for (SymbolState symbol : symbolStates) {
            String address = symbol.title.substring(0, symbol.title.lastIndexOf('/'));
            states.computeIfAbsent(address, (value) -> new HashMap<>());
            states.get(address).put(symbol.title.substring(symbol.title.lastIndexOf('/') + 1), symbol);
        }
        this.symbols = new HashMap<>();
    }

    public RobotSymbolFactory(@NotNull RobotState robotState) {
        this(robotState.symbols);
    }

    public Map<String, VirtualSymbol> getSymbols() {
        for (String name : states.get("RAPID").keySet()) {
            getSymbol(name);
        }
        return symbols;
    }

    private @Nullable RapidSymbol getSymbol(@NotNull String name) {
        if (symbols.containsKey(name)) return symbols.get(name);
        SymbolState state = states.get("RAPID").get(name);
        if (state == null) {
            return null;
        }
        return switch (state.type) {
            case "atm" -> getAtomic(state);
            case "rec" -> getRecord(state);
            case "ali" -> getAlias(state);
            case "con" -> getField(state, RapidField.Attribute.CONSTANT);
            case "var" -> getField(state, RapidField.Attribute.VARIABLE);
            case "per" -> getField(state, RapidField.Attribute.PERSISTENT);
            case "fun" -> getRoutine(state, RapidRoutine.Attribute.FUNCTION);
            case "prc" -> getRoutine(state, RapidRoutine.Attribute.PROCEDURE);
            case "trp" -> getRoutine(state, RapidRoutine.Attribute.TRAP);
            default -> throw new IllegalStateException("Unexpected value: " + state.type);
        };
    }

    private <T extends VirtualSymbol> @NotNull T getSymbol(@NotNull T symbol) {
        symbols.put(symbol.getName(), symbol);
        return symbol;
    }

    private @Nullable RapidStructure getStructure(@NotNull String name) {
        RapidSymbol symbol = getSymbol(name);
        return symbol instanceof RapidStructure structure ? structure : null;
    }

    private @NotNull RapidAtomic getAtomic(@NotNull SymbolState state) {
        RapidType dataType = state.dataType != null && state.dataType.length() > 0 ? new RapidType(getStructure(state.dataType)) : null;
        return getSymbol(new VirtualAtomic(Visibility.GLOBAL, state.name, dataType));
    }

    private @NotNull RapidRecord getRecord(@NotNull SymbolState state) {
        Collection<SymbolState> states = this.states.get(state.title).values();
        List<RapidComponent> components = new ArrayList<>();
        for (int i = 0; i < state.length; i++) {
            components.add(null);
        }
        for (SymbolState symbolState : states) {
            components.set(symbolState.index, getComponent(symbolState));
        }
        VirtualRecord symbol = new VirtualRecord(state.name, Collections.unmodifiableList(components));
        return getSymbol(symbol);
    }

    private @NotNull RapidAlias getAlias(@NotNull SymbolState state) {
        RapidType dataType = new RapidType(getStructure(state.dataType));
        return getSymbol(new VirtualAlias(Visibility.GLOBAL, state.name, dataType));
    }

    private @NotNull RapidComponent getComponent(@NotNull SymbolState state) {
        RapidType dataType = new RapidType(getStructure(state.dataType));
        return new VirtualComponent(state.name, dataType);
    }

    private @NotNull RapidField getField(@NotNull SymbolState state, @NotNull RapidField.Attribute attribute) {
        RapidType dataType = new RapidType(getStructure(state.dataType));
        return getSymbol(new VirtualField(Visibility.GLOBAL, attribute, state.name, dataType));
    }

    private @NotNull RapidParameter getParameter(@NotNull SymbolState state) {
        RapidType dataType = new RapidType(getStructure(state.dataType));
        RapidParameter.Attribute attribute = switch (state.mode) {
            case "in" -> RapidParameter.Attribute.INPUT;
            case "var" -> RapidParameter.Attribute.VARIABLE;
            case "pers" -> RapidParameter.Attribute.PERSISTENT;
            case "ref" -> RapidParameter.Attribute.REFERENCE;
            case "inout" -> RapidParameter.Attribute.INOUT;
            default -> throw new IllegalStateException("Unexpected value: " + state.mode);
        };
        return new VirtualParameter(attribute, state.name, dataType);
    }

    private @NotNull RapidRoutine getRoutine(@NotNull SymbolState state, @NotNull RapidRoutine.Attribute attribute) {
        List<RapidParameterGroup> groups = new ArrayList<>();
        Map<String, SymbolState> parameters = this.states.get(state.title);
        Collection<SymbolState> states = parameters != null ? parameters.values() : List.of();
        assert states.size() == state.length;
        for (int i = 0; i < state.length; i++) {
            groups.add(null);
        }
        for (SymbolState symbolState : states) {
            if (groups.get(symbolState.index) != null) {
                groups.get(symbolState.index).getParameters().add(getParameter(symbolState));
            } else {
                groups.set(symbolState.index, new VirtualParameterGroup(!symbolState.isRequired, new ArrayList<>()));
                groups.get(symbolState.index).getParameters().add(getParameter(symbolState));
            }
        }
        RapidType dataType = state.dataType != null && state.dataType.length() > 0 ? new RapidType(getStructure(state.dataType)) : null;
        VirtualRoutine routine = new VirtualRoutine(Visibility.GLOBAL, attribute, state.name, dataType, new ArrayList<>());
        return getSymbol(routine);
    }
}

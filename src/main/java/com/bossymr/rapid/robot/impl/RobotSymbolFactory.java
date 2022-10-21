package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.light.*;
import com.bossymr.rapid.robot.state.RobotState;
import com.bossymr.rapid.robot.state.SymbolState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class RobotSymbolFactory {

    private final Project project;
    private final Map<String, Map<String, SymbolState>> states;
    private final Map<String, RapidSymbol> symbols;

    public RobotSymbolFactory(@NotNull Project project, @NotNull RobotState robotState) {
        this.project = project;
        this.states = new HashMap<>();
        for (SymbolState symbol : robotState.symbols) {
            String address = symbol.path.substring(0, symbol.path.lastIndexOf('/'));
            states.computeIfAbsent(address, (value) -> new HashMap<>());
            states.get(address).put(symbol.path.substring(symbol.path.lastIndexOf('/') + 1), symbol);
        }
        this.symbols = new HashMap<>();
    }

    public Map<String, RapidSymbol> getSymbols() {
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
            case ATOMIC -> getAtomic(state);
            case RECORD -> getRecord(state);
            case ALIAS -> getAlias(state);
            case CONSTANT -> getField(state, RapidField.Attribute.CONSTANT);
            case VARIABLE -> getField(state, RapidField.Attribute.VARIABLE);
            case PERSISTENT -> getField(state, RapidField.Attribute.PERSISTENT);
            case FUNCTION -> getRoutine(state, RapidRoutine.Attribute.FUNCTION);
            case PROCEDURE -> getRoutine(state, RapidRoutine.Attribute.PROCEDURE);
            case TRAP -> getRoutine(state, RapidRoutine.Attribute.TRAP);
            default -> throw new IllegalStateException();
        };
    }

    private <T extends RapidSymbol> @NotNull T getSymbol(@NotNull T symbol) {
        symbols.put(symbol.getName(), symbol);
        return symbol;
    }

    private @Nullable RapidStructure getStructure(@NotNull String name) {
        RapidSymbol symbol = getSymbol(name);
        return symbol instanceof RapidStructure structure ? structure : null;
    }

    private @NotNull RapidAtomic getAtomic(@NotNull SymbolState state) {
        RapidType dataType = state.dataType != null && state.dataType.length() > 0 ? new RapidType(getStructure(state.dataType)) : null;
        return getSymbol(new LightAtomic(project, state.name, dataType));
    }

    private @NotNull RapidRecord getRecord(@NotNull SymbolState state) {
        Collection<SymbolState> states = this.states.get(state.path).values();
        List<RapidComponent> components = new ArrayList<>();
        for (int i = 0; i < state.length; i++) {
            components.add(null);
        }
        for (SymbolState symbolState : states) {
            components.set(symbolState.index, getComponent(symbolState));
        }
        LightRecord symbol = new LightRecord(project, state.name, Collections.unmodifiableList(components));
        return getSymbol(symbol);
    }

    private @NotNull RapidAlias getAlias(@NotNull SymbolState state) {
        RapidType dataType = new RapidType(getStructure(state.dataType));
        return getSymbol(new LightAlias(project, state.name, dataType));
    }

    private @NotNull RapidComponent getComponent(@NotNull SymbolState state) {
        RapidType dataType = new RapidType(getStructure(state.dataType));
        return new LightComponent(project, state.name, dataType);
    }

    private @NotNull RapidField getField(@NotNull SymbolState state, @NotNull RapidField.Attribute attribute) {
        RapidType dataType = new RapidType(getStructure(state.dataType));
        return getSymbol(new LightField(project, attribute, dataType, state.name));
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
        return new LightParameter(project, attribute, dataType, state.name);
    }

    private @NotNull RapidRoutine getRoutine(@NotNull SymbolState state, @NotNull RapidRoutine.Attribute attribute) {
        List<RapidParameterGroup> groups = new ArrayList<>();
        Map<String, SymbolState> parameters = this.states.get(state.path);
        Collection<SymbolState> states = parameters != null ? parameters.values() : List.of();
        assert states.size() == state.length;
        for (int i = 0; i < state.length; i++) {
            groups.add(null);
        }
        for (SymbolState symbolState : states) {
            if (groups.get(symbolState.index) != null) {
                groups.get(symbolState.index).getParameters().add(getParameter(symbolState));
            } else {
                groups.set(symbolState.index, new LightParameterGroup(project, !symbolState.isRequired, new ArrayList<>()));
                groups.get(symbolState.index).getParameters().add(getParameter(symbolState));
            }
        }
        RapidType dataType = state.dataType != null && state.dataType.length() > 0 ? new RapidType(getStructure(state.dataType)) : null;
        LightRoutine routine = new LightRoutine(project, attribute, state.name, dataType, new ArrayList<>());
        return getSymbol(routine);
    }
}

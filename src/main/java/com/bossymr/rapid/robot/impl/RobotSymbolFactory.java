package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.virtual.*;
import com.bossymr.rapid.robot.PersistentRobotState;
import com.bossymr.rapid.robot.network.SymbolState;
import com.bossymr.rapid.robot.network.SymbolState.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class RobotSymbolFactory {

    private final Map<String, Map<String, SymbolState>> states;
    private final Map<String, VirtualSymbol> symbols;

    private final Set<String> processed = new HashSet<>();

    public RobotSymbolFactory(@NotNull Collection<SymbolState> symbolStates) {
        this.states = new HashMap<>();
        for (SymbolState symbol : symbolStates) {
            String address = symbol.getTitle().substring(0, symbol.getTitle().lastIndexOf('/'));
            states.computeIfAbsent(address, (value) -> new HashMap<>());
            states.get(address).put(getName(symbol), symbol);
        }
        this.symbols = new HashMap<>();
    }

    public RobotSymbolFactory(@NotNull PersistentRobotState robotState) {
        this(robotState.getSymbolStates());
    }

    private @NotNull String getName(@NotNull SymbolState symbol) {
        return symbol.getTitle().substring(symbol.getTitle().lastIndexOf('/') + 1);
    }

    public Map<String, VirtualSymbol> getSymbols() {
        for (String name : states.get("RAPID").keySet()) {
            getSymbol(name);
        }
        states.remove("RAPID");
        states.entrySet().removeIf(entry -> processed.contains(entry.getKey()));
        assert states.isEmpty() : states;
        return symbols;
    }

    private @Nullable RapidSymbol getSymbol(@NotNull String name) {
        if (symbols.containsKey(name)) return symbols.get(name);
        SymbolState state = states.get("RAPID").get(name);
        if (state == null) {
            return null;
        }
        return switch (state.getSymbolType()) {
            case ATOMIC -> getAtomic((AtomicSymbolState) state);
            case RECORD -> getRecord((RecordSymbolState) state);
            case ALIAS -> getAlias((AliasSymbolState) state);
            case CONSTANT -> getField((FieldSymbolState) state, RapidField.Attribute.CONSTANT);
            case VARIABLE -> getField((FieldSymbolState) state, RapidField.Attribute.VARIABLE);
            case PERSISTENT -> getField((FieldSymbolState) state, RapidField.Attribute.PERSISTENT);
            case FUNCTION -> getRoutine((RoutineSymbolState) state, RapidRoutine.Attribute.FUNCTION);
            case PROCEDURE -> getRoutine((RoutineSymbolState) state, RapidRoutine.Attribute.PROCEDURE);
            case TRAP -> getRoutine((RoutineSymbolState) state, RapidRoutine.Attribute.TRAP);
            default -> throw new IllegalStateException("Unexpected value: " + state.getSymbolType());
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

    private @NotNull RapidAtomic getAtomic(@NotNull AtomicSymbolState symbol) {
        RapidType dataType = null;
        if (!(symbol.getDataType() == null || symbol.getDataType().isEmpty())) {
            dataType = new RapidType(getStructure(symbol.getDataType()));
        }
        return getSymbol(new VirtualAtomic(Visibility.GLOBAL, getName(symbol), dataType));
    }

    private @NotNull RapidRecord getRecord(@NotNull RecordSymbolState symbol) {
        Collection<SymbolState> states = this.states.get(symbol.getTitle()).values();
        List<RapidComponent> components = new ArrayList<>();
        assert states.size() == symbol.getSize();
        processed.add(symbol.getTitle());
        for (int i = 0; i < symbol.getSize(); i++) {
            components.add(null);
        }
        for (SymbolState symbolState : states) {
            assert symbolState instanceof ComponentSymbolState;
            ComponentSymbolState componentSymbolState = (ComponentSymbolState) symbolState;
            components.set(componentSymbolState.getIndex() - 1, getComponent(componentSymbolState));
        }
        assert !components.contains(null);
        return getSymbol(new VirtualRecord(getName(symbol), Collections.unmodifiableList(components)));
    }

    private @NotNull RapidAlias getAlias(@NotNull AliasSymbolState state) {
        RapidType dataType = new RapidType(getStructure(Objects.requireNonNull(state.getDataType())));
        Visibility visibility = state.isLocal() ? Visibility.LOCAL : Visibility.GLOBAL;
        return getSymbol(new VirtualAlias(visibility, getName(state), dataType));
    }

    private @NotNull RapidComponent getComponent(@NotNull ComponentSymbolState state) {
        RapidType dataType = new RapidType(getStructure(Objects.requireNonNull(state.getDataType())));
        return new VirtualComponent(getName(state), dataType);
    }

    private @NotNull RapidField getField(@NotNull FieldSymbolState state, @NotNull RapidField.Attribute attribute) {
        RapidType dataType = new RapidType(getStructure(Objects.requireNonNull(state.getDataType())));
        return getSymbol(new VirtualField(getFieldVisibility(state), attribute, getName(state), dataType));
    }

    private @NotNull Visibility getFieldVisibility(@NotNull FieldSymbolState state) {
        if (state instanceof VariableSymbolState variableSymbolState) {
            if (variableSymbolState.isTask()) {
                return Visibility.TASK;
            }
        }
        if (state instanceof PersistentSymbolState persistentSymbolState) {
            if (persistentSymbolState.isTask()) {
                return Visibility.TASK;
            }
        }
        if (state.isLocal()) {
            return Visibility.LOCAL;
        }
        return Visibility.GLOBAL;
    }

    private @NotNull RapidParameter getParameter(@NotNull ParameterSymbolState state) {
        RapidType dataType = new RapidType(getStructure(Objects.requireNonNull(state.getDataType())));
        RapidParameter.Attribute attribute = switch (state.getMode()) {
            case "in" -> RapidParameter.Attribute.INPUT;
            case "var" -> RapidParameter.Attribute.VARIABLE;
            case "pers" -> RapidParameter.Attribute.PERSISTENT;
            case "ref" -> RapidParameter.Attribute.REFERENCE;
            case "inout" -> RapidParameter.Attribute.INOUT;
            default -> throw new IllegalStateException("Unexpected mode: " + state.getMode());
        };
        return new VirtualParameter(attribute, getName(state), dataType);
    }

    private @NotNull RapidRoutine getRoutine(@NotNull RoutineSymbolState state, @NotNull RapidRoutine.Attribute attribute) {
        List<RapidParameterGroup> groups = new ArrayList<>();
        Map<String, SymbolState> parameters = this.states.get(state.getTitle());
        Collection<SymbolState> states = parameters != null ? parameters.values() : List.of();
        assert states.size() == state.getSize();
        processed.add(state.getTitle());
        for (int i = 0; i < state.getSize(); i++) {
            groups.add(null);
        }
        for (SymbolState symbolState : states) {
            assert symbolState instanceof ParameterSymbolState;
            ParameterSymbolState parameterSymbolState = (ParameterSymbolState) symbolState;
            if (groups.get(parameterSymbolState.getIndex() - 1) != null) {
                groups.get(parameterSymbolState.getIndex() - 1).getParameters().add(getParameter(parameterSymbolState));
            } else {
                groups.set(parameterSymbolState.getIndex() - 1, new VirtualParameterGroup(!parameterSymbolState.isRequired(), new ArrayList<>()));
                groups.get(parameterSymbolState.getIndex() - 1).getParameters().add(getParameter(parameterSymbolState));
            }
        }
        RapidType dataType = state instanceof FunctionSymbolState functionSymbolState && functionSymbolState.getDataType().length() > 0 ? new RapidType(getStructure(functionSymbolState.getDataType())) : null;
        groups.removeIf(Objects::isNull);
        assert !groups.contains(null);
        Visibility visibility = state.isLocal() ? Visibility.LOCAL : Visibility.GLOBAL;
        VirtualRoutine routine = new VirtualRoutine(visibility, attribute, getName(state), dataType, groups);
        return getSymbol(routine);
    }
}

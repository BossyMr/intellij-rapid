package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.virtual.*;
import com.bossymr.rapid.robot.RobotState;
import com.bossymr.rapid.robot.network.Symbol;
import com.bossymr.rapid.robot.network.Symbol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class RobotSymbolFactory {

    private final Map<String, Map<String, Symbol>> states;
    private final Map<String, VirtualSymbol> symbols;

    private final Set<String> processed = new HashSet<>();

    public RobotSymbolFactory(@NotNull Collection<Symbol> symbols) {
        this.states = new HashMap<>();
        for (Symbol symbol : symbols) {
            String address = symbol.getTitle().toLowerCase().substring(0, symbol.getTitle().lastIndexOf('/'));
            states.computeIfAbsent(address, (value) -> new HashMap<>());
            states.get(address).put(getName(symbol), symbol);
        }
        this.symbols = new HashMap<>();
    }

    public RobotSymbolFactory(@NotNull RobotState robotState) {
        this(robotState.getSymbols());
    }

    private @NotNull String getName(@NotNull Symbol symbol) {
        return symbol.getTitle().toLowerCase().substring(symbol.getTitle().lastIndexOf('/') + 1);
    }

    public @NotNull Map<String, VirtualSymbol> getSymbols() {
        if (states.isEmpty()) return new HashMap<>();
        for (String name : states.get("rapid").keySet()) {
            getSymbol(name);
        }
        states.remove("rapid");
        states.entrySet().removeIf(entry -> processed.contains(entry.getKey()));
        assert states.isEmpty() : states;
        return symbols;
    }

    private @Nullable RapidSymbol getSymbol(@NotNull String name) {
        if (symbols.containsKey(name)) return symbols.get(name);
        Symbol state = states.get("rapid").get(name);
        if (state == null) {
            return null;
        }
        return switch (state.getSymbolType()) {
            case ATOMIC -> getAtomic((AtomicSymbol) state);
            case RECORD -> getRecord((RecordSymbol) state);
            case ALIAS -> getAlias((AliasSymbol) state);
            case CONSTANT -> getField((FieldSymbol) state, RapidField.Attribute.CONSTANT);
            case VARIABLE -> getField((FieldSymbol) state, RapidField.Attribute.VARIABLE);
            case PERSISTENT -> getField((FieldSymbol) state, RapidField.Attribute.PERSISTENT);
            case FUNCTION -> getRoutine((RoutineSymbol) state, RapidRoutine.Attribute.FUNCTION);
            case PROCEDURE -> getRoutine((RoutineSymbol) state, RapidRoutine.Attribute.PROCEDURE);
            case TRAP -> getRoutine((RoutineSymbol) state, RapidRoutine.Attribute.TRAP);
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

    private @NotNull RapidAtomic getAtomic(@NotNull Symbol.AtomicSymbol symbol) {
        RapidType dataType = null;
        if (!(symbol.getDataType() == null || symbol.getDataType().isEmpty())) {
            dataType = new RapidType(getStructure(symbol.getDataType()));
        }
        return getSymbol(new VirtualAtomic(Visibility.GLOBAL, getName(symbol), dataType));
    }

    private @NotNull RapidRecord getRecord(@NotNull Symbol.RecordSymbol symbol) {
        Collection<Symbol> states = this.states.get(symbol.getTitle()).values();
        List<RapidComponent> components = new ArrayList<>();
        assert states.size() == symbol.getSize();
        processed.add(symbol.getTitle());
        for (int i = 0; i < symbol.getSize(); i++) {
            components.add(null);
        }
        for (Symbol symbolState : states) {
            assert symbolState instanceof ComponentSymbol;
            ComponentSymbol componentSymbolState = (ComponentSymbol) symbolState;
            components.set(componentSymbolState.getIndex() - 1, getComponent(componentSymbolState));
        }
        assert !components.contains(null);
        return getSymbol(new VirtualRecord(getName(symbol), Collections.unmodifiableList(components)));
    }

    private @NotNull RapidAlias getAlias(@NotNull Symbol.AliasSymbol state) {
        RapidType dataType = new RapidType(getStructure(Objects.requireNonNull(state.getDataType())));
        return getSymbol(new VirtualAlias(Visibility.GLOBAL, getName(state), dataType));
    }

    private @NotNull RapidComponent getComponent(@NotNull Symbol.ComponentSymbol state) {
        RapidType dataType = new RapidType(getStructure(Objects.requireNonNull(state.getDataType())));
        return new VirtualComponent(getName(state), dataType);
    }

    private @NotNull RapidField getField(@NotNull Symbol.FieldSymbol state, @NotNull RapidField.Attribute attribute) {
        RapidType dataType = new RapidType(getStructure(Objects.requireNonNull(state.getDataType())));
        boolean readOnly = false;
        if (state instanceof Symbol.PersistentSymbol persistentSymbol) {
            readOnly = persistentSymbol.isReadOnly();
        }
        if (state instanceof Symbol.VariableSymbol variableSymbol) {
            readOnly = variableSymbol.isReadOnly();
        }
        return getSymbol(new VirtualField(Visibility.GLOBAL, attribute, getName(state), dataType, readOnly));
    }

    private @NotNull VirtualParameter getParameter(@NotNull RapidParameterGroup parameterGroup, @NotNull Symbol.ParameterSymbol state) {
        RapidType dataType = new RapidType(getStructure(Objects.requireNonNull(state.getDataType())));
        RapidParameter.Attribute attribute = switch (state.getMode()) {
            case "in" -> RapidParameter.Attribute.INPUT;
            case "var" -> RapidParameter.Attribute.VARIABLE;
            case "pers" -> RapidParameter.Attribute.PERSISTENT;
            case "ref" -> RapidParameter.Attribute.REFERENCE;
            case "inout" -> RapidParameter.Attribute.INOUT;
            default -> throw new IllegalStateException("Unexpected mode: " + state.getMode());
        };
        return new VirtualParameter(parameterGroup, attribute, getName(state), dataType);
    }

    private @NotNull RapidRoutine getRoutine(@NotNull Symbol.RoutineSymbol state, @NotNull RapidRoutine.Attribute attribute) {
        List<VirtualParameterGroup> groups = new ArrayList<>();
        Map<String, Symbol> parameters = this.states.get(state.getTitle());
        Collection<Symbol> states = parameters != null ? parameters.values() : List.of();
        assert states.size() == state.getSize() : state;
        processed.add(state.getTitle());
        for (int i = 0; i < state.getSize(); i++) {
            groups.add(null);
        }
        for (Symbol symbol : states) {
            assert symbol instanceof ParameterSymbol;
            ParameterSymbol parameterSymbolState = (ParameterSymbol) symbol;
            int index = parameterSymbolState.getIndex() - 1;
            if (groups.get(index) != null) {
                groups.get(index).getParameters().add(getParameter(groups.get(index), parameterSymbolState));
            } else {
                groups.set(index, new VirtualParameterGroup(!parameterSymbolState.isRequired(), new ArrayList<>()));
                groups.get(index).getParameters().add(getParameter(groups.get(index), parameterSymbolState));
            }
        }
        RapidType dataType = state instanceof FunctionSymbol functionSymbolState && functionSymbolState.getDataType().length() > 0 ? new RapidType(getStructure(functionSymbolState.getDataType())) : null;
        groups.removeIf(Objects::isNull);
        assert !groups.contains(null);
        VirtualRoutine routine = new VirtualRoutine(Visibility.GLOBAL, attribute, getName(state), dataType, groups);
        return getSymbol(routine);
    }
}

package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.virtual.*;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A converter for converting {@link SymbolState} objects into {@link RapidSymbol} objects.
 */
public final class RapidSymbolConverter {

    private final Map<String, Map<String, SymbolState>> states;
    private final Map<String, VirtualSymbol> symbols;

    private final Set<String> processed = new HashSet<>();

    private RapidSymbolConverter(@NotNull Collection<SymbolState> symbolStates) {
        this.states = new HashMap<>();
        for (SymbolState symbolState : symbolStates) {
            String address = symbolState.getTitle().toLowerCase().substring(0, symbolState.getTitle().lastIndexOf('/'));
            states.computeIfAbsent(address, (value) -> new HashMap<>());
            states.get(address).put(getName(symbolState), symbolState);
        }
        this.symbols = new HashMap<>();
    }

    public static @NotNull Map<String, VirtualSymbol> getSymbols(@NotNull Collection<SymbolState> symbolStates) {
        return new RapidSymbolConverter(symbolStates).getSymbols();
    }

    private @NotNull String getName(@NotNull SymbolState symbolState) {
        return symbolState.getTitle().toLowerCase().substring(symbolState.getTitle().lastIndexOf('/') + 1);
    }

    private @NotNull Map<String, VirtualSymbol> getSymbols() {
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
        SymbolState state = states.get("rapid").get(name);
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
        assert states.size() == symbol.getComponentCount();
        processed.add(symbol.getTitle());
        for (int i = 0; i < symbol.getComponentCount(); i++) {
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
        return getSymbol(new VirtualAlias(Visibility.GLOBAL, getName(state), dataType));
    }

    private @NotNull RapidComponent getComponent(@NotNull ComponentSymbolState state) {
        RapidType dataType = new RapidType(getStructure(Objects.requireNonNull(state.getDataType())));
        return new VirtualComponent(getName(state), dataType);
    }

    private @NotNull RapidField getField(@NotNull FieldSymbolState state, @NotNull RapidField.Attribute attribute) {
        RapidType dataType = new RapidType(getStructure(Objects.requireNonNull(state.getDataType())));
        boolean readOnly = false;
        if (state instanceof PersistentSymbolState persistentSymbol) {
            readOnly = persistentSymbol.isReadOnly();
        }
        if (state instanceof VariableSymbolState variableSymbol) {
            readOnly = variableSymbol.isReadOnly();
        }
        return getSymbol(new VirtualField(Visibility.GLOBAL, attribute, getName(state), dataType, readOnly));
    }

    private @NotNull VirtualParameter getParameter(@NotNull RapidParameterGroup parameterGroup, @NotNull ParameterSymbolState state) {
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

    private @NotNull RapidRoutine getRoutine(@NotNull RoutineSymbolState state, @NotNull RapidRoutine.Attribute attribute) {
        List<VirtualParameterGroup> groups = new ArrayList<>();
        Map<String, SymbolState> parameters = this.states.get(state.getTitle());
        Collection<SymbolState> states = parameters != null ? parameters.values() : List.of();
        assert states.size() == state.getParameterCount() : state;
        processed.add(state.getTitle());
        for (int i = 0; i < state.getParameterCount(); i++) {
            groups.add(null);
        }
        for (SymbolState symbolState : states) {
            assert symbolState instanceof ParameterSymbolState;
            ParameterSymbolState parameterSymbolState = (ParameterSymbolState) symbolState;
            int index = parameterSymbolState.getIndex() - 1;
            if (groups.get(index) != null) {
                groups.get(index).getParameters().add(getParameter(groups.get(index), parameterSymbolState));
            } else {
                groups.set(index, new VirtualParameterGroup(!parameterSymbolState.isRequired(), new ArrayList<>()));
                groups.get(index).getParameters().add(getParameter(groups.get(index), parameterSymbolState));
            }
        }
        RapidType dataType = state instanceof FunctionSymbolState functionSymbolState && functionSymbolState.getDataType().length() > 0 ? new RapidType(getStructure(functionSymbolState.getDataType())) : null;
        groups.removeIf(Objects::isNull);
        assert !groups.contains(null);
        VirtualRoutine routine = new VirtualRoutine(Visibility.GLOBAL, attribute, getName(state), dataType, groups);
        return getSymbol(routine);
    }
}

package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.virtual.*;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A converter for converting {@link SymbolModel} objects into {@link RapidSymbol} objects.
 */
public final class SymbolConverter {

    private final Map<String, Map<String, SymbolModel>> states;
    private final Map<String, VirtualSymbol> symbols;

    private SymbolConverter(@NotNull Collection<SymbolModel> symbolModels) {
        this.states = new HashMap<>();
        for (SymbolModel symbolModel : symbolModels) {
            String address = symbolModel.getTitle().substring(0, symbolModel.getTitle().lastIndexOf('/'));
            states.computeIfAbsent(address, (value) -> new HashMap<>());
            states.get(address).put(getName(symbolModel), symbolModel);
        }
        this.symbols = new HashMap<>();
    }

    public static @NotNull Map<String, VirtualSymbol> getSymbols(@NotNull Collection<SymbolModel> symbolModels) {
        return new SymbolConverter(symbolModels).getSymbols();
    }

    public static @NotNull VirtualSymbol getSymbol(@NotNull SymbolModel symbolModel) {
        Map<String, VirtualSymbol> symbols = new SymbolConverter(List.of(symbolModel)).getSymbols();
        return List.copyOf(symbols.values()).get(0);
    }

    private @NotNull String getName(@NotNull SymbolModel symbolModel) {
        return symbolModel.getTitle().substring(symbolModel.getTitle().lastIndexOf('/') + 1);
    }

    private @NotNull Map<String, VirtualSymbol> getSymbols() {
        if (states.isEmpty()) return new HashMap<>();
        for (String name : states.get("RAPID").keySet()) {
            getSymbol(name);
        }
        return symbols;
    }

    private @Nullable RapidSymbol getSymbol(@NotNull String name) {
        if (symbols.containsKey(name)) return symbols.get(name);
        SymbolModel state = states.get("RAPID").get(name);
        if (state == null) {
            return null;
        }
        return switch (state.getSymbolType()) {
            case ATOMIC -> getAtomic((AtomicModel) state);
            case RECORD -> getRecord((RecordModel) state);
            case ALIAS -> getAlias((AliasModel) state);
            case CONSTANT -> getField((FieldModel) state, RapidField.Attribute.CONSTANT);
            case VARIABLE -> getField((FieldModel) state, RapidField.Attribute.VARIABLE);
            case PERSISTENT -> getField((FieldModel) state, RapidField.Attribute.PERSISTENT);
            case FUNCTION -> getRoutine((RoutineModel) state, RapidRoutine.Attribute.FUNCTION);
            case PROCEDURE -> getRoutine((RoutineModel) state, RapidRoutine.Attribute.PROCEDURE);
            case TRAP -> getRoutine((RoutineModel) state, RapidRoutine.Attribute.TRAP);
            default -> null;
        };
    }

    private <T extends VirtualSymbol> @NotNull T getSymbol(@NotNull T symbol) {
        symbols.put(symbol.getName().toLowerCase(), symbol);
        return symbol;
    }

    private @Nullable RapidStructure getStructure(@NotNull String name) {
        RapidSymbol symbol = getSymbol(name);
        return symbol instanceof RapidStructure structure ? structure : null;
    }

    private @NotNull RapidAtomic getAtomic(@NotNull AtomicModel symbol) {
        RapidType dataType = null;
        if (!(symbol.getDataType() == null || symbol.getDataType().isEmpty())) {
            dataType = new RapidType(getStructure(symbol.getDataType()));
        }
        return getSymbol(new VirtualAtomic(Visibility.GLOBAL, getName(symbol), dataType));
    }

    private @NotNull RapidRecord getRecord(@NotNull RecordModel symbol) {
        Map<String, SymbolModel> map = this.states.get(symbol.getTitle());
        Collection<SymbolModel> states = map.values();
        List<RapidComponent> components = new ArrayList<>();
        assert states.size() == symbol.getComponentCount();
        for (int i = 0; i < symbol.getComponentCount(); i++) {
            components.add(null);
        }
        VirtualRecord record = new VirtualRecord(getName(symbol), components);
        for (SymbolModel symbolModel : states) {
            assert symbolModel instanceof ComponentModel;
            ComponentModel componentSymbolState = (ComponentModel) symbolModel;
            components.set(componentSymbolState.getIndex() - 1, getComponent(record, componentSymbolState));
        }
        assert !components.contains(null);
        return getSymbol(record);
    }

    private @NotNull RapidAlias getAlias(@NotNull AliasModel state) {
        RapidType dataType = new RapidType(getStructure(Objects.requireNonNull(state.getDataType())));
        return getSymbol(new VirtualAlias(Visibility.GLOBAL, getName(state), dataType));
    }

    private @NotNull RapidComponent getComponent(@NotNull VirtualRecord record, @NotNull ComponentModel state) {
        RapidType dataType = new RapidType(getStructure(Objects.requireNonNull(state.getDataType())));
        return new VirtualComponent(record, getName(state), dataType);
    }

    private @NotNull RapidField getField(@NotNull FieldModel state, @NotNull RapidField.Attribute attribute) {
        RapidType dataType = new RapidType(getStructure(Objects.requireNonNull(state.getDataType())));
        boolean readOnly = false;
        if (state instanceof PersistentModel persistentSymbol) {
            readOnly = persistentSymbol.isReadOnly();
        }
        if (state instanceof VariableModel variableSymbol) {
            readOnly = variableSymbol.isReadOnly();
        }
        return getSymbol(new VirtualField(Visibility.GLOBAL, attribute, getName(state), dataType, readOnly));
    }

    private @NotNull VirtualParameter getParameter(@NotNull VirtualParameterGroup parameterGroup, @NotNull ParameterModel state) {
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

    private @NotNull RapidRoutine getRoutine(@NotNull RoutineModel state, @NotNull RapidRoutine.Attribute attribute) {
        List<VirtualParameterGroup> groups = new ArrayList<>();
        Map<String, SymbolModel> parameters = this.states.get(state.getTitle());
        Collection<SymbolModel> states = parameters != null ? parameters.values() : List.of();
        assert states.size() == state.getParameterCount() : state;
        for (int i = 0; i < state.getParameterCount(); i++) {
            groups.add(null);
        }
        RapidType dataType = state instanceof FunctionModel functionSymbolState && functionSymbolState.getDataType().length() > 0 ? new RapidType(getStructure(functionSymbolState.getDataType())) : null;
        VirtualRoutine routine = new VirtualRoutine(Visibility.GLOBAL, attribute, getName(state), dataType, groups);
        for (SymbolModel symbolModel : states) {
            assert symbolModel instanceof ParameterModel;
            ParameterModel parameterSymbolState = (ParameterModel) symbolModel;
            int index = parameterSymbolState.getIndex() - 1;
            if (groups.get(index) != null) {
                groups.get(index).getParameters().add(getParameter(groups.get(index), parameterSymbolState));
            } else {
                groups.set(index, new VirtualParameterGroup(routine, !parameterSymbolState.isRequired(), new ArrayList<>()));
                groups.get(index).getParameters().add(getParameter(groups.get(index), parameterSymbolState));
            }
        }
        groups.removeIf(Objects::isNull);
        assert !groups.contains(null);
        return getSymbol(routine);
    }
}

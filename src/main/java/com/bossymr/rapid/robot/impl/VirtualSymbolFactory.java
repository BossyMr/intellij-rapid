package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.language.flow.data.HardcodedContract;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.virtual.*;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.bossymr.rapid.language.type.RapidUnknownType;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A converter for converting {@link SymbolModel} objects into {@link RapidSymbol} objects.
 */
public final class VirtualSymbolFactory {

    private final Map<String, Map<String, SymbolModel>> states;
    private final Map<String, VirtualSymbol> symbols;

    private VirtualSymbolFactory(@NotNull Collection<SymbolModel> symbolModels) {
        this.states = new HashMap<>();
        for (SymbolModel symbolModel : symbolModels) {
            String address = symbolModel.getTitle().substring(0, symbolModel.getTitle().lastIndexOf('/')).toLowerCase();
            states.computeIfAbsent(address, (value) -> new HashMap<>());
            states.get(address).put(getName(symbolModel), symbolModel);
        }
        this.symbols = new HashMap<>();
        for (RapidPrimitiveType value : RapidPrimitiveType.values()) {
            VirtualStructure structure = value.getStructure();
            symbols.put(structure.getName(), structure);
        }
        for (HardcodedContract value : HardcodedContract.values()) {
            VirtualRoutine routine = value.getRoutine();
            symbols.put(routine.getName(), routine);
        }
    }

    public static @NotNull Map<String, VirtualSymbol> getSymbols(@NotNull Collection<SymbolModel> symbolModels) {
        return new VirtualSymbolFactory(symbolModels).getSymbols();
    }

    public static @NotNull VirtualSymbol getSymbol(@NotNull SymbolModel model) {
        Map<String, VirtualSymbol> symbols = new VirtualSymbolFactory(List.of(model)).getSymbols();
        VirtualSymbol symbol = symbols.get(getName(model));
        if (symbol == null) {
            throw new IllegalArgumentException("Could not convert model: " + model + " into a symbol");
        }
        return symbol;
    }

    private static @NotNull String getName(@NotNull SymbolModel symbolModel) {
        return symbolModel.getTitle().substring(symbolModel.getTitle().lastIndexOf('/') + 1).toLowerCase();
    }

    private @NotNull Map<String, VirtualSymbol> getSymbols() {
        if (states.isEmpty()) return new HashMap<>();
        Map<String, SymbolModel> objects = states.get("rapid");
        if(objects != null) {
            for (String name : objects.keySet()) {
                getSymbol(name);
            }
        }
        return symbols;
    }

    private @Nullable RapidSymbol getSymbol(@NotNull String name) {
        if (symbols.containsKey(name)) return symbols.get(name);
        SymbolModel state = states.get("rapid").get(name);
        if (state == null) {
            return null;
        }
        return switch (state.getSymbolType()) {
            case ATOMIC -> getAtomic((AtomicModel) state);
            case RECORD -> getRecord((RecordModel) state);
            case ALIAS -> getAlias((AliasModel) state);
            case CONSTANT -> getField((FieldModel) state, FieldType.CONSTANT);
            case VARIABLE -> getField((FieldModel) state, FieldType.VARIABLE);
            case PERSISTENT -> getField((FieldModel) state, FieldType.PERSISTENT);
            case FUNCTION -> getRoutine((RoutineModel) state, RoutineType.FUNCTION);
            case PROCEDURE -> getRoutine((RoutineModel) state, RoutineType.PROCEDURE);
            case TRAP -> getRoutine((RoutineModel) state, RoutineType.TRAP);
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
            RapidStructure structure = getStructure(symbol.getDataType());
            if(structure != null) {
                dataType = structure.createType();
            }
        }
        return getSymbol(new VirtualAtomic(getName(symbol), dataType));
    }

    private @NotNull RapidRecord getRecord(@NotNull RecordModel symbol) {
        Map<String, SymbolModel> map = this.states.get(symbol.getTitle().toLowerCase());
        Collection<SymbolModel> states = map.values();
        List<VirtualComponent> components = new ArrayList<>();
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
        String name = Objects.requireNonNull(state.getDataType());
        RapidStructure structure = Objects.requireNonNull(getStructure(name));
        return getSymbol(new VirtualAlias(getName(state), structure.createType()));
    }

    private @NotNull VirtualComponent getComponent(@NotNull VirtualRecord record, @NotNull ComponentModel state) {
        String name = Objects.requireNonNull(state.getDataType());
        RapidStructure structure = Objects.requireNonNull(getStructure(name));
        return new VirtualComponent(record, getName(state), structure.createType());
    }

    private @NotNull RapidField getField(@NotNull FieldModel state, @NotNull FieldType fieldType) {
        RapidStructure structure = getStructure(Objects.requireNonNull(state.getDataType()));
        RapidType dataType = structure != null ? structure.createType() : new RapidUnknownType(state.getName());
        boolean readOnly = false;
        if (state instanceof PersistentModel persistentSymbol) {
            readOnly = persistentSymbol.isReadOnly();
        }
        if (state instanceof VariableModel variableSymbol) {
            readOnly = variableSymbol.isReadOnly();
        }
        return getSymbol(new VirtualField(getName(state), fieldType, dataType, !(readOnly)));
    }

    private @NotNull VirtualParameter getParameter(@NotNull VirtualParameterGroup parameterGroup, @NotNull ParameterModel state) {
        RapidStructure structure = getStructure(Objects.requireNonNull(state.getDataType()));
        RapidType dataType = structure != null ? structure.createType() : new RapidUnknownType(state.getName());
        ParameterType parameterType = switch (state.getMode()) {
            case "in" -> ParameterType.INPUT;
            case "var" -> ParameterType.VARIABLE;
            case "pers" -> ParameterType.PERSISTENT;
            case "ref" -> ParameterType.REFERENCE;
            case "inout" -> ParameterType.INOUT;
            default -> throw new IllegalStateException("Unexpected mode: " + state.getMode());
        };
        return new VirtualParameter(parameterGroup, parameterType, getName(state), dataType);
    }

    private @NotNull RapidRoutine getRoutine(@NotNull RoutineModel state, @NotNull RoutineType routineType) {
        List<VirtualParameterGroup> groups = new ArrayList<>();
        Map<String, SymbolModel> parameters = this.states.get(state.getTitle().toLowerCase());
        Collection<SymbolModel> states = parameters != null ? parameters.values() : List.of();
        if(states.size() != state.getParameterCount()) {
            throw new IllegalStateException("Expected: " + state.getParameterCount() + " elements (" + state + "), got: " + states);
        }
        for (int i = 0; i < state.getParameterCount(); i++) {
            groups.add(null);
        }
        RapidType dataType;
        if (state instanceof FunctionModel functionSymbolState && !(functionSymbolState.getDataType()).isEmpty()) {
            RapidStructure structure = getStructure(functionSymbolState.getDataType());
            dataType = structure != null ? structure.createType() : null;
        } else {
            dataType = null;
        }
        VirtualRoutine routine = new VirtualRoutine(routineType, getName(state), dataType, groups);
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

package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity(subtype = {
        SymbolState.AtomicSymbolState.class,
        SymbolState.RecordSymbolState.class,
        SymbolState.AliasSymbolState.class,
        SymbolState.ComponentSymbolState.class,
        SymbolState.ConstantSymbolState.class,
        SymbolState.VariableSymbolState.class,
        SymbolState.PersistentSymbolState.class,
        SymbolState.ParameterSymbolState.class,
        SymbolState.FunctionSymbolState.class,
        SymbolState.ProcedureSymbolState.class
})
public interface SymbolState extends EntityModel {

    @Property("symburl")
    @Nullable String getCanonicalName();

    @Property("name")
    @Nullable String getName();

    @Property("symtyp")
    @NotNull SymbolType getSymbolType();

    @Property("named")
    boolean isNamed();

    interface VisibleSymbolState extends SymbolState {

        @Property("local")
        boolean isLocal();
    }

    interface FieldSymbolState extends SymbolState, VisibleSymbolState {

        @Property("typurl")
        @Nullable String getCanonicalType();

        @Property("dattyp")
        @Nullable String getDataType();

        @Property("ndim")
        int getSize();
    }

    interface RoutineSymbolState extends SymbolState, VisibleSymbolState {

        @Property("npar")
        int getSize();
    }

    @Entity({"rap-sympropatomic", "rap-sympropatomic-li"})
    interface AtomicSymbolState extends SymbolState {

        @Property("size")
        int getSize();

        @Property("valtyp")
        @Nullable String getValueType();

        @Property("typurl")
        @Nullable String getCanonicalType();

        @Property("dattyp")
        @Nullable String getDataType();

    }

    @Entity({"rap-symproprecord", "rap-symproprecord-li"})
    interface RecordSymbolState extends SymbolState, VisibleSymbolState {

        @Property("ncom")
        int getSize();

        @Property("valtyp")
        @NotNull String getValueType();

    }

    @Entity({"rap-sympropalias", "rap-sympropalias-li"})
    interface AliasSymbolState extends SymbolState, VisibleSymbolState {

        @Property("linked")
        boolean isLinked();

        @Property("typurl")
        @Nullable String getCanonicalType();

        @Property("dattyp")
        @Nullable String getDataType();
    }

    @Entity({"rap-sympropreccomp", "rap-sympropreccomp-li"})
    interface ComponentSymbolState extends SymbolState {

        @Property("comnum")
        int getIndex();

        @Property("typurl")
        @Nullable String getCanonicalType();

        @Property("dattyp")
        @Nullable String getDataType();
    }

    @Entity({"rap-sympropconstant", "rap-sympropconstant-li"})
    interface ConstantSymbolState extends SymbolState, VisibleSymbolState, FieldSymbolState {

        @Property("linked")
        boolean isLinked();

    }

    @Entity({"rap-sympropvar", "rap-sympropvar-li"})
    interface VariableSymbolState extends SymbolState, VisibleSymbolState, FieldSymbolState {

        @Property("dim")
        @Nullable String getDimensions();

        @Property("rdonly")
        boolean isRead();

        @Property("taskvar")
        boolean isTask();

    }

    @Entity({"rap-symproppers", "rap-symproppers-li"})
    interface PersistentSymbolState extends SymbolState, VisibleSymbolState, FieldSymbolState {

        @Property("dim")
        @Nullable String getDimensions();

        @Property("rdonly")
        boolean isRead();

        @Property("taskpers")
        boolean isTask();

    }

    @Entity({"rap-sympropparam", "rap-sympropparam-li"})
    interface ParameterSymbolState extends SymbolState {

        @Property("altnum")
        int getAlternativeIndex();

        @Property("dattyp")
        @Nullable String getDataType();

        @Property("mode")
        @NotNull String getMode();

        @Property("ndim")
        int getSize();

        @Property("parnum")
        int getIndex();

        @Property("required")
        boolean isRequired();

        @Property("typurl")
        @NotNull String getCanonicalType();

    }

    @Entity({"rap-sympropfunction", "rap-sympropfunction-li"})
    interface FunctionSymbolState extends SymbolState, VisibleSymbolState, RoutineSymbolState {

        @Property("linked")
        boolean isLinked();

        @Property("typurl")
        @NotNull String getCanonicalType();

        @Property("dattyp")
        @NotNull String getDataType();

    }

    @Entity({"rap-sympropproc", "rap-sympropproc-li"})
    interface ProcedureSymbolState extends SymbolState, VisibleSymbolState, RoutineSymbolState {}

    @Entity({"rap-symproptask", "rap-symproptask-li"})
    interface TaskSymbolState extends SymbolState {}
}

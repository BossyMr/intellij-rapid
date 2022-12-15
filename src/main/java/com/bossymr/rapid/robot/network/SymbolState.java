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
    @Nullable SymbolType getSymbolType();

    @Property("named")
    boolean isNamed();

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
    interface RecordSymbolState extends SymbolState {

        @Property("local")
        boolean isLocal();

        @Property("ncom")
        int getSize();

        @Property("valtyp")
        @NotNull String getValueType();

    }

    @Entity({"rap-sympropalias", "rap-sympropalias-li"})
    interface AliasSymbolState extends SymbolState {

        @Property("linked")
        boolean isLinked();

        @Property("local")
        boolean isLocal();

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
    interface ConstantSymbolState extends SymbolState {

        @Property("linked")
        boolean isLinked();

        @Property("local")
        boolean isLocal();

        @Property("typurl")
        @Nullable String getCanonicalType();

        @Property("dattyp")
        @Nullable String getDataType();

        @Property("ndim")
        int getSize();
    }

    @Entity({"rap-sympropvar", "rap-sympropvar-li"})
    interface VariableSymbolState extends SymbolState {

        @Property("dattyp")
        @Nullable String getDataType();

        @Property("typurl")
        @Nullable String getCanonicalType();

        @Property("ndim")
        int getSize();

        @Property("dim")
        @Nullable String getDimensions();

        @Property("local")
        boolean isLocal();

        @Property("rdonly")
        boolean isRead();

        @Property("taskvar")
        boolean isTask();

    }

    @Entity({"rap-symproppers", "rap-symproppers-li"})
    interface PersistentSymbolState extends SymbolState {

        @Property("dattyp")
        @Nullable String getDataType();

        @Property("ndim")
        int getSize();

        @Property("dim")
        @Nullable String getDimensions();

        @Property("local")
        boolean isLocal();

        @Property("rdonly")
        boolean isRead();

        @Property("taskvar")
        boolean isTask();

        @Property("typurl")
        @Nullable String getCanonicalType();

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
    interface FunctionSymbolState extends SymbolState {

        @Property("linked")
        boolean isLinked();

        @Property("local")
        boolean isLocal();

        @Property("typurl")
        @NotNull String getCanonicalType();

        @Property("dattyp")
        @NotNull String getDataType();

        @Property("npar")
        int getSize();
    }

    @Entity({"rap-sympropproc", "rap-sympropproc-li"})
    interface ProcedureSymbolState extends SymbolState {

        @Property("local")
        boolean isLocal();

        @Property("npar")
        int getSize();
    }

    @Entity({"rap-symproptask", "rap-symproptask-li"})
    interface TaskSymbolState extends SymbolState {}
}

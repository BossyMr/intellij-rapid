package com.bossymr.rapid.robot.network;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity(subtype = {
        Symbol.AtomicSymbol.class,
        Symbol.RecordSymbol.class,
        Symbol.AliasSymbol.class,
        Symbol.ComponentSymbol.class,
        Symbol.ConstantSymbol.class,
        Symbol.VariableSymbol.class,
        Symbol.PersistentSymbol.class,
        Symbol.ParameterSymbol.class,
        Symbol.FunctionSymbol.class,
        Symbol.ProcedureSymbol.class
})
public interface Symbol extends EntityModel {

    @Property("symburl")
    @Nullable String getCanonicalName();

    @Property("name")
    @Nullable String getName();

    @Property("symtyp")
    @NotNull SymbolType getSymbolType();

    @Property("named")
    boolean isNamed();

    interface VisibleSymbol extends Symbol {

        @Property("local")
        boolean isLocal();
    }

    interface FieldSymbol extends Symbol, VisibleSymbol {

        @Property("typurl")
        @Nullable String getCanonicalType();

        @Property("dattyp")
        @Nullable String getDataType();

        @Property("ndim")
        int getSize();
    }

    interface RoutineSymbol extends Symbol, VisibleSymbol {

        @Property("npar")
        int getSize();
    }

    @Entity({"rap-sympropatomic", "rap-sympropatomic-li"})
    interface AtomicSymbol extends Symbol {

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
    interface RecordSymbol extends Symbol, VisibleSymbol {

        @Property("ncom")
        int getSize();

        @Property("valtyp")
        @NotNull String getValueType();

    }

    @Entity({"rap-sympropalias", "rap-sympropalias-li"})
    interface AliasSymbol extends Symbol, VisibleSymbol {

        @Property("linked")
        boolean isLinked();

        @Property("typurl")
        @Nullable String getCanonicalType();

        @Property("dattyp")
        @Nullable String getDataType();
    }

    @Entity({"rap-sympropreccomp", "rap-sympropreccomp-li"})
    interface ComponentSymbol extends Symbol {

        @Property("comnum")
        int getIndex();

        @Property("typurl")
        @Nullable String getCanonicalType();

        @Property("dattyp")
        @Nullable String getDataType();
    }

    @Entity({"rap-sympropconstant", "rap-sympropconstant-li"})
    interface ConstantSymbol extends Symbol, VisibleSymbol, FieldSymbol {

        @Property("linked")
        boolean isLinked();

    }

    @Entity({"rap-sympropvar", "rap-sympropvar-li"})
    interface VariableSymbol extends Symbol, VisibleSymbol, FieldSymbol {

        @Property("dim")
        @Nullable String getDimensions();

        @Property("rdonly")
        boolean isReadOnly();

        @Property("taskvar")
        boolean isTask();

    }

    @Entity({"rap-symproppers", "rap-symproppers-li"})
    interface PersistentSymbol extends Symbol, VisibleSymbol, FieldSymbol {

        @Property("dim")
        @Nullable String getDimensions();

        @Property("rdonly")
        boolean isReadOnly();

        @Property("taskpers")
        boolean isTask();

    }

    @Entity({"rap-sympropparam", "rap-sympropparam-li"})
    interface ParameterSymbol extends Symbol {

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
    interface FunctionSymbol extends Symbol, VisibleSymbol, RoutineSymbol {

        @Property("linked")
        boolean isLinked();

        @Property("typurl")
        @NotNull String getCanonicalType();

        @Property("dattyp")
        @NotNull String getDataType();

    }

    @Entity({"rap-sympropproc", "rap-sympropproc-li"})
    interface ProcedureSymbol extends Symbol, VisibleSymbol, RoutineSymbol {}

    @Entity({"rap-symproptask", "rap-symproptask-li"})
    interface TaskSymbol extends Symbol {}
}

package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import com.bossymr.network.annotations.Title;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code Symbol} represents a symbol.
 */
@Entity(subtype = {
        AtomicModel.class, AliasModel.class, RecordModel.class, ComponentModel.class,
        ConstantModel.class, VariableModel.class, PersistentModel.class, ParameterModel.class, FunctionModel.class,
        ProcedureModel.class, TrapModel.class, ModuleModel.class})
public interface SymbolModel {

    @Title
    @NotNull String getTitle();

    /**
     * Returns the canonical name of the symbol. For example, the canonical name of a field might be
     * {@code RAPID/T_ROB1/MyModule/MyField}.
     *
     * @return the canonical name of the symbol.
     */
    @Property("symburl")
    @NotNull String getCanonicalName();

    /**
     * Returns the name of the symbol.
     *
     * @return the name of the symbol.
     */
    @Property("name")
    @NotNull String getName();

    /**
     * Returns the type of the symbol.
     *
     * @return the type of the symbol.
     */
    @Property("symtyp")
    @NotNull SymbolType getSymbolType();

    /**
     * Checks if this symbol is named, or if the name of the symbol is {@code <ID>}.
     *
     * @return if this symbol is named.
     */
    @Property("named")
    boolean isNamed();

}

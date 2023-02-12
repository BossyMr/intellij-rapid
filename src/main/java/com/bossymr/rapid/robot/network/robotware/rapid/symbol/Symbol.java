package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code Symbol} represents a symbol.
 */
@Entity(subtype = {
        AtomicSymbol.class, AliasSymbol.class, RecordSymbol.class, ComponentSymbol.class,
        ConstantSymbol.class, VariableSymbol.class, PersistentSymbol.class, ParameterSymbol.class, FunctionSymbol.class,
        ProcedureSymbol.class, TrapSymbol.class, ModuleSymbol.class})
public interface Symbol extends EntityModel {

    /**
     * Returns the canonical name of the symbol. For example, the canonical name of a field might be
     * {@code RAPID/T_ROB1/MyModule/MyField}.
     *
     * @return the canonical name of the symbol.
     */
    @Property("symburl")
    @Nullable String getCanonicalName();

    /**
     * Returns the name of the symbol.
     *
     * @return the name of the symbol.
     */
    @Property("name")
    @Nullable String getName();

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

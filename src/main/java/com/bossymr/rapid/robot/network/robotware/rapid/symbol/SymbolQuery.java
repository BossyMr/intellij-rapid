package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Deserializable;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * A {@code SymbolQuery} is used to build a query for a symbol search.
 */
public class SymbolQuery extends HashMap<String, String> {

    /**
     * Specifies the search method.
     *
     * @param method the search method.
     * @return this builder.
     */
    public @NotNull SymbolQuery setMethod(@NotNull SymbolSearchMethod method) {
        put("view", getValue(method));
        return this;
    }

    /**
     * Specifies whether the search should stop at the next scope or block level, or continue recursively until the root
     * or leaf element is reached.
     *
     * @param recursive whether to search recursively.
     * @return this builder.
     */
    public @NotNull SymbolQuery setRecursive(boolean recursive) {
        put("recursive", String.valueOf(recursive));
        return this;
    }

    /**
     * Specifies the URL of the block to search in and should not start with '/'. The global block is 'RAPID'.
     *
     * @param block the URL of the block.
     * @return this builder.
     */
    public @NotNull SymbolQuery setBlock(@NotNull String block) {
        put("blockurl", block);
        return this;
    }

    /**
     * Specifies the stack frame to search in, the current stack frame is always '1'.
     *
     * @param stack the stack frame.
     * @return this builder.
     */
    public @NotNull SymbolQuery setStack(int stack) {
        put("stack", String.valueOf(stack));
        return this;
    }

    /**
     * Specifies the line (row) of the block to search in.
     *
     * @param row the row of the block.
     * @return this builder.
     */
    public @NotNull SymbolQuery setRow(int row) {
        put("posl", String.valueOf(row));
        return this;
    }

    /**
     * Specifies the column of the block to search in.
     *
     * @param column the column of the block.
     * @return this builder.
     */
    public @NotNull SymbolQuery setColumn(int column) {
        put("posc", String.valueOf(column));
        return this;
    }

    /**
     * Specifies whether only used symbols should be found.
     *
     * @param onlyUsed whether to only find symbols which are in use.
     * @return this builder.
     */
    public @NotNull SymbolQuery onlyUsed(boolean onlyUsed) {
        put("onlyused", String.valueOf(onlyUsed));
        return this;
    }

    /**
     * Specifies whether to skip shared symbols.
     *
     * @param skipShared whether to skip shared symbols.
     * @return this builder.
     */
    public @NotNull SymbolQuery skipShared(boolean skipShared) {
        put("skipshared", String.valueOf(skipShared));
        return this;
    }

    /**
     * Specifies the name of the symbol to find.
     *
     * @param name the name of the symbol to find.
     * @return this builder.
     */
    public @NotNull SymbolQuery setName(@NotNull @Language("RegExp") String name) {
        put("regexp", name);
        return this;
    }

    /**
     * Specifies the data type of the symbol to find.
     *
     * @param dataType the data type of the symbol
     * @return this builder.
     */
    public @NotNull SymbolQuery setDataType(@NotNull String dataType) {
        put("dattyp", dataType);
        return this;
    }

    /**
     * Specifies which type of symbol to find.
     *
     * @param symbolType the type of symbol
     * @return this builder.
     */
    public @NotNull SymbolQuery setSymbolType(@NotNull SymbolType symbolType) {
        put("symtyp", getValue(symbolType));
        return this;
    }

    private <E extends Enum<E>> @NotNull String getValue(@NotNull E constant) {
        try {
            Field field = constant.getDeclaringClass().getField(constant.name());
            Deserializable deserializable = field.getAnnotation(Deserializable.class);
            if (deserializable == null) {
                throw new IllegalStateException("Field '" + constant.name() + "' of '" + constant.getClass().getName() + "' is not deserializable");
            }
            return deserializable.value();
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

}

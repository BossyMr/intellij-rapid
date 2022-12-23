package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Deserializable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class SymbolQueryBuilder {

    private final Map<String, String> arguments = new HashMap<>();

    public @NotNull SymbolQueryBuilder setRecursive(boolean recursive) {
        arguments.put("recursive", String.valueOf(recursive));
        return this;
    }

    public @NotNull SymbolQueryBuilder setQuery(@NotNull String regex) {
        arguments.put("regexp", regex);
        return this;
    }

    public @NotNull SymbolQueryBuilder setSymbolType(@NotNull SymbolType symbolType) {
        try {
            Field field = symbolType.getDeclaringClass().getField(symbolType.name());
            Deserializable deserializable = field.getAnnotation(Deserializable.class);
            assert deserializable != null;
            arguments.put("symtyp", deserializable.value());
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Contract(pure = true)
    public @NotNull Map<String, String> build() {
        return arguments;
    }

}

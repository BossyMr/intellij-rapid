package com.bossymr.rapid.network.controller.rapid;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SymbolSearchQuery {

    private final Map<String, String> arguments;

    private SymbolSearchQuery(@NotNull Map<String, String> arguments) {
        this.arguments = Collections.unmodifiableMap(arguments);
    }

    public static @NotNull Builder newBuilder() {
        return new Builder();
    }

    public @NotNull Map<String, String> getArguments() {
        return arguments;
    }

    public static class Builder {

        private final Map<String, String> arguments = new HashMap<>();

        public @NotNull Builder setSymbolViewType(@NotNull SymbolSearchViewType searchViewType) {
            arguments.put("view", switch (searchViewType) {
                case BLOCK -> "block";
                case SCOPE -> "scope";
                case STACK -> "stack";
            });
            return this;
        }

        public @NotNull Builder setSymbolType(@NotNull SymbolType symbolType) {
            arguments.put("symtyp", switch (symbolType) {
                case ATOMIC -> "atm";
                case RECORD -> "rec";
                case ALIAS -> "ali";
                case RECORD_COMPONENT -> "rcp";
                case CONSTANT -> "con";
                case VARIABLE -> "var";
                case PERSISTENT -> "per";
                case PARAMETER -> "par";
                case LABEL -> "lab";
                case FOR_STATEMENT -> "for";
                case FUNCTION -> "fun";
                case PROCEDURE -> "prc";
                case TRAP -> "trp";
                case MODULE -> "mod";
                case TASK -> "tsk";
                case UNDEFINED -> throw new IllegalArgumentException();
                case ANY -> "any";
            });
            return this;
        }

        public @NotNull SymbolSearchQuery build() {
            return new SymbolSearchQuery(arguments);
        }
    }
}

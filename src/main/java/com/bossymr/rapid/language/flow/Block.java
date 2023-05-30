package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.psi.StatementListType;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.RoutineType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public sealed interface Block {

    @NotNull String moduleName();

    @NotNull String name();

    @Nullable RapidType returnType();

    @NotNull List<Scope> scopes();

    @NotNull Map<StatementListType, Scope> entry();

    @NotNull Map<Integer, Variable> variables();

    @Nullable List<ArgumentGroup> arguments();

    default int getNextIndex() {
        List<ArgumentGroup> arguments = arguments();
        return variables().size() + (arguments != null ? arguments.size() : 0);
    }

    record FunctionBlock(
            @NotNull String moduleName,
            @NotNull String name,
            @Nullable RapidType returnType,
            @NotNull List<Scope> scopes,
            @NotNull Map<StatementListType, Scope> entry,
            @NotNull Map<Integer, Variable> variables,
            @Nullable List<ArgumentGroup> arguments,
            @NotNull RoutineType routineType
    ) implements Block {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FunctionBlock that = (FunctionBlock) o;
            return Objects.equals(moduleName, that.moduleName) && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(moduleName, name);
        }
    }

    record FieldBlock(
            @NotNull String moduleName,
            @NotNull String name,
            @NotNull RapidType returnType,
            @NotNull List<Scope> scopes,
            @NotNull Map<StatementListType, Scope> entry,
            @NotNull Map<Integer, Variable> variables,
            @NotNull FieldType fieldType
    ) implements Block {

        @Override
        public @Nullable List<ArgumentGroup> arguments() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FieldBlock that = (FieldBlock) o;
            return Objects.equals(moduleName, that.moduleName) && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(moduleName, name);
        }
    }
}

package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.conditon.Value;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.RoutineType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public sealed interface Block {

    @NotNull String moduleName();

    @NotNull String name();

    @Nullable RapidType returnType();

    @NotNull List<Scope> scopes();

    @NotNull Map<ScopeType, Scope> entry();

    @NotNull Map<Integer, Variable> variables();

    @Nullable List<ArgumentGroup> arguments();

    default @Nullable Argument findArgument(@NotNull String name) {
        List<ArgumentGroup> arguments = arguments();
        if (arguments == null) {
            return null;
        }
        for (ArgumentGroup group : arguments) {
            for (Argument argument : group.arguments()) {
                if (argument.name().equalsIgnoreCase(name)) {
                    return argument;
                }
            }
        }
        return null;
    }

    default @Nullable Variable findVariable(@NotNull String name) {
        Collection<Variable> arguments = variables().values();
        for (Variable argument : arguments) {
            if (name.equalsIgnoreCase(argument.name())) {
                return argument;
            }
        }
        return null;
    }

    default @NotNull Scope newScope() {
        Scope scope = new Scope.IntermediateScope(scopes().size(), new ArrayList<>());
        scopes().add(scope);
        return scope;
    }

    default @NotNull Scope newErrorScope(@Nullable List<Value> exceptions) {
        Scope scope = new Scope.ErrorScope(scopes().size(), exceptions, new ArrayList<>());
        scopes().add(scope);
        return scope;
    }

    default @NotNull Scope newEntryScope(@NotNull ScopeType scopeType) {
        Scope scope = new Scope.EntryScope(scopeType, scopes().size(), new ArrayList<>());
        scopes().add(scope);
        return scope;
    }

    default @NotNull Variable newVariable(@NotNull String name, @Nullable Object value, @NotNull RapidType type, @Nullable FieldType fieldType) {
        Variable variable = new Variable(getNextVariableIndex(), value, fieldType, type, name);
        variables().put(variable.index(), variable);
        return variable;
    }

    default @NotNull Variable newVariable(@Nullable Object value, @NotNull RapidType type) {
        Variable variable = new Variable(getNextVariableIndex(), value, null, type, null);
        variables().put(variable.index(), variable);
        return variable;
    }

    default @NotNull Argument newArgument(@NotNull String name, @NotNull RapidType type, @NotNull ParameterType parameterType) {
        return new Argument(getNextVariableIndex(), parameterType, type, name);
    }

    private int getNextVariableIndex() {
        List<ArgumentGroup> arguments = arguments();
        return 1 + variables().size() + (arguments != null ? arguments.size() : 0);
    }

    record FunctionBlock(
            @NotNull String moduleName,
            @NotNull String name,
            @Nullable RapidType returnType,
            @NotNull List<Scope> scopes,
            @NotNull Map<ScopeType, Scope> entry,
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
            @NotNull Map<ScopeType, Scope> entry,
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

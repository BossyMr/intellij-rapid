package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.psi.RapidStatement;
import com.bossymr.rapid.language.psi.StatementListType;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.Visibility;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class VirtualRoutine implements RapidRoutine, VirtualSymbol {

    private final @NotNull RoutineType routineType;
    private final @NotNull String moduleName, name;
    private final @Nullable RapidType type;
    private final @Nullable List<VirtualParameterGroup> parameters;

    public VirtualRoutine(@NotNull RoutineType routineType, @NotNull String name, @Nullable RapidType type, @Nullable List<VirtualParameterGroup> parameters) {
        this("", name, routineType, type, parameters);
    }

    public VirtualRoutine(@NotNull String moduleName, @NotNull String name, @NotNull RoutineType routineType, @Nullable RapidType returnType, @Nullable List<VirtualParameterGroup> parameters) {
        this.routineType = routineType;
        this.moduleName = moduleName;
        this.name = name;
        this.type = returnType;
        this.parameters = parameters;
    }

    public @NotNull String getModuleName() {
        return moduleName;
    }

    @Override
    public @NotNull RoutineType getRoutineType() {
        return routineType;
    }

    @Override
    public @Nullable RapidType getType() {
        return type;
    }

    @Override
    public @Nullable List<VirtualParameterGroup> getParameters() {
        return parameters;
    }

    @Override
    public @NotNull List<VirtualField> getFields() {
        return List.of();
    }

    @Override
    public @NotNull List<RapidStatement> getStatements() {
        return List.of();
    }

    @Override
    public @Nullable List<RapidStatement> getStatements(@NotNull StatementListType statementListType) {
        if (statementListType == StatementListType.STATEMENT_LIST) {
            return getStatements();
        }
        return null;
    }

    @Override
    public @NotNull Visibility getVisibility() {
        return Visibility.GLOBAL;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull VirtualPointer<VirtualRoutine> createPointer() {
        return new VirtualPointer<>(this, getClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualRoutine that = (VirtualRoutine) o;
        return routineType == that.routineType && Objects.equals(moduleName, that.moduleName) && Objects.equals(name, that.name) && Objects.equals(type, that.type) && Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routineType, moduleName, name, type, parameters);
    }

    @Override
    public String toString() {
        return "VirtualRoutine{" +
                "routineType=" + routineType +
                ", name='" + name + '\'' +
                ", identity='" + hashCode() + '\'' +
                ", type=" + type +
                '}';
    }
}

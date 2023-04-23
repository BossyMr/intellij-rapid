package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.psi.RapidStatement;
import com.bossymr.rapid.language.psi.StatementListType;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.Visibility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VirtualRoutine implements RapidRoutine, VirtualSymbol {

    private final @NotNull RoutineType routineType;
    private final @NotNull String name;
    private final @Nullable RapidType type;
    private final @Nullable List<VirtualParameterGroup> parameters;

    public VirtualRoutine(@NotNull RoutineType routineType, @NotNull String name, @Nullable RapidType type, @Nullable List<VirtualParameterGroup> parameters) {
        this.routineType = routineType;
        this.name = name;
        this.type = type;
        this.parameters = parameters;
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
    public String toString() {
        return "VirtualRoutine{" +
                "routineType=" + routineType +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}

package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.psi.RapidStatement;
import com.bossymr.rapid.language.psi.RapidStatementList;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.Visibility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record VirtualRoutine(
        @NotNull Visibility visibility,
        @NotNull Attribute attribute,
        @NotNull String name,
        @Nullable RapidType type,
        @Nullable List<VirtualParameterGroup> parameters
) implements RapidRoutine, VirtualSymbol {

    @Override
    public @NotNull Attribute getAttribute() {
        return attribute();
    }

    @Override
    public @Nullable RapidType getType() {
        return type();
    }

    @Override
    public @Nullable List<VirtualParameterGroup> getParameters() {
        return parameters();
    }

    @Override
    public @NotNull List<VirtualField> getFields() {
        return new ArrayList<>();
    }

    @Override
    public @NotNull List<RapidStatement> getStatements() {
        return new ArrayList<>();
    }

    @Override
    public @Nullable List<RapidStatement> getStatements(@NotNull RapidStatementList.Attribute attribute) {
        return null;
    }

    @Override
    public @NotNull Visibility getVisibility() {
        return visibility();
    }

    @Override
    public @NotNull String getName() {
        return name();
    }
}

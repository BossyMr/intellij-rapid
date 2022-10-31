package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.psi.RapidStatement;
import com.bossymr.rapid.language.psi.RapidStatementList;
import com.bossymr.rapid.language.symbol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class VirtualRoutine implements RapidRoutine, VirtualSymbol {

    private final Visibility visibility;
    private final Attribute attribute;
    private final String name;
    private final RapidType type;
    private final List<RapidParameterGroup> parameters;

    public VirtualRoutine(@NotNull Visibility visibility, @NotNull Attribute attribute, @NotNull String name, @Nullable RapidType type, @Nullable List<RapidParameterGroup> parameters) {
        this.visibility = visibility;
        this.attribute = attribute;
        this.name = name;
        this.type = type;
        this.parameters = parameters;
    }

    @Override
    public @NotNull Visibility getVisibility() {
        return visibility;
    }

    @Override
    public @NotNull Attribute getAttribute() {
        return attribute;
    }

    @Override
    public @Nullable RapidType getType() {
        return type;
    }

    @Override
    public @Nullable List<RapidParameterGroup> getParameters() {
        return parameters;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull List<RapidField> getFields() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull List<RapidStatement> getStatements() {
        return Collections.emptyList();
    }

    @Override
    public @Nullable List<RapidStatement> getStatements(RapidStatementList.@NotNull Attribute attribute) {
        return Collections.emptyList();
    }
}

package com.bossymr.rapid.language.psi.light;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidStatementListImpl;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class LightRoutine extends LightSymbol implements RapidRoutine {

    private final Attribute attribute;
    private final String name;
    private final RapidType type;
    private final List<RapidParameterGroup> parameters;

    public LightRoutine(@NotNull PsiManager manager, @NotNull Attribute attribute, @NotNull String name, @Nullable RapidType type, @NotNull List<RapidParameterGroup> parameters) {
        super(manager);
        this.attribute = attribute;
        this.name = name;
        this.type = type;
        this.parameters = parameters;
    }

    @Override
    public boolean isLocal() {
        return false;
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
    public @Nullable RapidTypeElement getTypeElement() {
        return null;
    }

    @Override
    public @NotNull List<RapidParameterGroup> getParameters() {
        return parameters;
    }

    @Override
    public @Nullable RapidParameterList getParameterList() {
        return null;
    }

    @Override
    public @NotNull List<RapidField> getFields() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull RapidStatementList getStatementList() {
        return new RapidStatementListImpl();
    }

    @Override
    public @Nullable RapidStatementList getBackwardStatementList() {
        return null;
    }

    @Override
    public @Nullable RapidStatementList getErrorStatementList() {
        return null;
    }

    @Override
    public @Nullable RapidStatementList getUndoStatementList() {
        return null;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "RapidRoutine";
    }
}

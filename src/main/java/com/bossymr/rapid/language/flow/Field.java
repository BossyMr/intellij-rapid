package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.symbol.RapidVariable;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Field {

    int getIndex();

    @NotNull RapidType getType();

    @Nullable String getName();

    @Nullable RapidVariable getVariable();

    @Nullable List<Expression> getArraySize();

}

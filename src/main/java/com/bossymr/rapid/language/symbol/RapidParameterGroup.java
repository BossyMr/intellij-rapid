package com.bossymr.rapid.language.symbol;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface RapidParameterGroup {

    @NotNull RapidRoutine getRoutine();

    boolean isOptional();

    @NotNull List<? extends RapidParameter> getParameters();
}

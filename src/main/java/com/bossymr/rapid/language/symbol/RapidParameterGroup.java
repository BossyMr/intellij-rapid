package com.bossymr.rapid.language.symbol;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface RapidParameterGroup {

    boolean isOptional();

    @NotNull List<RapidParameter> getParameters();
}

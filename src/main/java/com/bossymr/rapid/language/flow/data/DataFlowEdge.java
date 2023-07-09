package com.bossymr.rapid.language.flow.data;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record DataFlowEdge(boolean plausible, @NotNull DataFlowBlock block, @NotNull Set<DataFlowState> states) {}

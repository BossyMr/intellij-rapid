package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.BasicBlock;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public record DataFlowBlock(@NotNull BasicBlock basicBlock,
                            @NotNull Set<DataFlowBlock> predecessors,
                            @NotNull Set<DataFlowBlock> successors,
                            @NotNull List<DataFlowState> states) {}

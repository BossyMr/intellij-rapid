package com.bossymr.rapid.language.flow.debug;

import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public record DataFlowUsage(@NotNull DataFlowUsageType usageType, @NotNull Set<DataFlowBlock> usages) {

    public @NotNull DataFlowUsage copy() {
        return new DataFlowUsage(usageType, new HashSet<>(usages));
    }

    public enum DataFlowUsageType {
        SUCCESS, ERROR, EXIT
    }

}

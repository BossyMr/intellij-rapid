package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.conditon.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface DataFlowFunction {

    @NotNull DataFlowFunctionOutput getOutput(@NotNull DataFlowBlock block, @NotNull Map<Integer, Value> arguments);

}

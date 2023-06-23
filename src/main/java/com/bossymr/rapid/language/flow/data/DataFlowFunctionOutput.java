package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.constraint.Constraint;
import org.jetbrains.annotations.Nullable;

public sealed interface DataFlowFunctionOutput {

    record Fail() implements DataFlowFunctionOutput {}

    record Succeed(@Nullable Constraint constraint) implements DataFlowFunctionOutput {}

}

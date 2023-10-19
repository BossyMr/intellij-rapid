package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.instruction.CallInstruction;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * A {@code DataFlowFunction} represents a callable function.
 */
public interface DataFlowFunction {

    @NotNull Block.FunctionBlock getBlock();

    @NotNull Set<Result> getOutput(@NotNull DataFlowState state, @NotNull CallInstruction instruction);

    sealed interface Result {

        @NotNull DataFlowState state();

        @Nullable ReferenceExpression variable();

        record Success(@NotNull DataFlowState state, @Nullable ReferenceExpression variable) implements Result {}

        record Error(@NotNull DataFlowState state, @Nullable ReferenceExpression variable) implements Result {}

        /**
         * This function terminated the program.
         */
        record Exit(@NotNull DataFlowState state) implements Result {
            @Override
            public @Nullable ReferenceExpression variable() {
                return null;
            }
        }
    }

}

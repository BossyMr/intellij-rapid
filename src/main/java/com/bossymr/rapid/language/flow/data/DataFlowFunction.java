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

    /**
     * Returns the block of this function.
     *
     * @return the block of this function.
     */
    @NotNull Block.FunctionBlock getBlock();

    /**
     * Create all possible outputs of this function, if called by the specified instruction in the specified state. The
     * state stored in each returned result is a successor of the specified state. Additionally, each parameter for this
     * function is replaced by the corresponding snapshot in the specified state.
     *
     * @param state the state where this function is called.
     * @param instruction the instruction which calls this function.
     * @return all possible outputs of this function.
     */
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

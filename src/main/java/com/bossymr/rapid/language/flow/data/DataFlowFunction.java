package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.value.SnapshotExpression;
import com.bossymr.rapid.language.flow.value.ReferenceValue;
import com.bossymr.rapid.language.flow.value.VariableValue;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A {@code DataFlowFunction} represents a callable function.
 */
public interface DataFlowFunction {

    /**
     * Returns the block which represents this function, which can be used to find the return type and arguments of this
     * function. If this function represents a virtual function, the specified block will not have any entry blocks.
     *
     * @return the block which represents this function.
     */
    @NotNull Block.FunctionBlock getBlock();

    /**
     * Calculates the result of calling this function with the specified arguments.
     *
     * @param callingBlock the block which called this function.
     * @param arguments the arguments with which this function is called with.
     * @return the result of calling this function.
     */
    @NotNull Set<Result> getOutput(@NotNull DataFlowBlock callingBlock, @NotNull Map<Argument, Constraint> arguments);

    /**
     * A {@code Result} object represents the possible output of calling a {@link DataFlowFunction}.
     */
    sealed interface Result {

        /**
         * The function returned successfully.
         *
         * @param states the state of the variables in the function when it returned.
         * @param returnValue the value which was returned, or {@code null} if a variable was not returned.
         */
        record Success(@NotNull List<DataFlowState> states, @Nullable ReferenceValue returnValue) implements Result {

            public static @NotNull Success create(@NotNull Block.FunctionBlock block, @NotNull Map<Argument, Constraint> constraints, @Nullable RapidType returnType, @Nullable Constraint returnConstraint) {
                DataFlowState state = DataFlowState.createUnknownState(block);
                for (Argument argument : constraints.keySet()) {
                    Optional<SnapshotExpression> snapshot = state.createSnapshot(new VariableValue(argument));
                    if (snapshot.isPresent() && snapshot.orElseThrow() instanceof VariableSnapshot variableSnapshot) {
                        state.add(variableSnapshot, constraints.get(argument));
                    }
                }
                ReferenceValue returnValue = null;
                if (returnType != null && returnConstraint != null) {
                    returnValue = new VariableSnapshot(returnType);
                    Optional<SnapshotExpression> snapshot = state.createSnapshot(returnValue);
                    if (snapshot.isPresent() && snapshot.orElseThrow() instanceof VariableSnapshot variableSnapshot) {
                        state.add(variableSnapshot, returnConstraint);
                    }
                }
                return new Success(List.of(state), returnValue);
            }
        }

        /**
         * The function returned unsuccessfully.
         *
         * @param states the state of the variables in this function when it failed.
         * @param exceptionValue the exception which was thrown, or {@code null} if a specific exception was not
         * thrown.
         */
        record Error(@NotNull List<DataFlowState> states, @Nullable ReferenceValue exceptionValue) implements Result {}

        /**
         * This function terminated the program.
         */
        record Exit() implements Result {}
    }

}

package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.value.ReferenceValue;
import com.bossymr.rapid.language.flow.value.VariableReference;
import com.bossymr.rapid.language.flow.value.VariableSnapshot;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * @param arguments the arguments with which this function is called with.
     * @return the result of calling this function.
     */
    @NotNull Set<Result> getOutput(@NotNull Map<Argument, Constraint> arguments);

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
        record Success(@NotNull Set<DataFlowState> states, @Nullable ReferenceValue returnValue) implements Result {

            public Success {
                states = states.stream()
                        .map(state -> new DataFlowState(state.conditions(), state.snapshots()))
                        .collect(Collectors.toSet());
            }

            public static @NotNull Success create(@NotNull Map<Argument, Constraint> constraints, @Nullable RapidType returnType, @Nullable Constraint constraint) {
                Set<DataFlowState> states = new HashSet<>();
                states.add(new DataFlowState(new HashSet<>(), new HashMap<>()));
                for (Argument argument : constraints.keySet()) {
                    Set<Set<Condition>> situations = constraints.get(argument).toConditions(new VariableReference(argument));
                    states = getStates(states, situations);
                }
                ReferenceValue returnValue = null;
                if (constraint != null) {
                    Objects.requireNonNull(returnType);
                    returnValue = new VariableSnapshot(returnType);
                    Set<Set<Condition>> situations = constraint.toConditions(returnValue);
                    states = getStates(states, situations);
                }
                return new Success(states, returnValue);
            }

            private static @NotNull Set<DataFlowState> getStates(@NotNull Set<DataFlowState> states, @NotNull Set<Set<Condition>> situations) {
                return states.stream()
                        .flatMap(state -> {
                            Stream.Builder<DataFlowState> builder = Stream.builder();
                            for (Set<Condition> situation : situations) {
                                DataFlowState copy = new DataFlowState(state.conditions(), state.snapshots());
                                for (Condition condition : situation) {
                                    copy.setCondition(condition);
                                }
                                builder.add(copy);
                            }
                            return builder.build();
                        }).collect(Collectors.toSet());
            }
        }

        /**
         * The function returned unsuccessfully.
         *
         * @param states the state of the variables in this function when it failed.
         * @param exceptionValue the exception which was thrown, or {@code null} if a specific exception was not
         * thrown.
         */
        record Error(@NotNull Set<DataFlowState> states, @Nullable ReferenceValue exceptionValue) implements Result {}

        /**
         * This function terminated the program.
         */
        record Exit() implements Result {}
    }

}

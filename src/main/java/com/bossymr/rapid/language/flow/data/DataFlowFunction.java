package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.value.VariableReference;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface DataFlowFunction {

    @NotNull Block.FunctionBlock getBlock();

    @NotNull Result getOutput(@NotNull Map<Argument, Constraint> arguments);

    sealed interface Result {

        @NotNull Constraint getConstraint(@NotNull DataFlowBlock block, @NotNull Map<Argument, Condition> arguments);

        record WithConstraint(@NotNull Constraint constraint) implements Result {
            @Override
            public @NotNull Constraint getConstraint(@NotNull DataFlowBlock block, @NotNull Map<Argument, Condition> arguments) {
                return constraint;
            }
        }

        record WithCondition(@NotNull DataFlowState state, @NotNull Condition condition) implements Result {
            @Override
            public @NotNull Constraint getConstraint(@NotNull DataFlowBlock block, @NotNull Map<Argument, Condition> arguments) {
                DataFlowState copy = new DataFlowState(state.conditions(), state.snapshots());
                copy.conditions().add(condition);
                for (Argument argument : arguments.keySet()) {
                    copy.conditions().add(arguments.get(argument));
                    copy.snapshots().put(new VariableReference(argument.type(), argument), arguments.get(argument).getVariable());
                }
                return copy.getConstraint(condition);
            }
        }
    }

}

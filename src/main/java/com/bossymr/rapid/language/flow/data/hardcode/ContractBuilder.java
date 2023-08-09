package com.bossymr.rapid.language.flow.data.hardcode;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.ArgumentGroup;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.constraint.Optionality;
import com.bossymr.rapid.language.flow.data.AbstractDataFlowFunction;
import com.bossymr.rapid.language.flow.data.DataFlowFunction;
import com.bossymr.rapid.language.flow.data.DataFlowFunction.Result;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.value.ReferenceValue;
import com.bossymr.rapid.language.flow.value.VariableValue;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.virtual.VirtualParameter;
import com.bossymr.rapid.language.symbol.virtual.VirtualParameterGroup;
import com.bossymr.rapid.language.symbol.virtual.VirtualRoutine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

public class ContractBuilder {

    private final @NotNull Block.FunctionBlock functionBlock;
    private final @NotNull Map<Map<Argument, Constraint>, Set<Result>> results = new HashMap<>();

    public ContractBuilder(@NotNull String name, @NotNull RapidType type) {
        VirtualRoutine virtualRoutine = new VirtualRoutine(RoutineType.FUNCTION, name, type, new ArrayList<>());
        functionBlock = new Block.FunctionBlock(virtualRoutine, "");
    }

    public ContractBuilder(@NotNull String name) {
        VirtualRoutine virtualRoutine = new VirtualRoutine(RoutineType.PROCEDURE, name, null, new ArrayList<>());
        functionBlock = new Block.FunctionBlock(virtualRoutine, "");
    }

    public @NotNull ArgumentGroupBuilder withArgumentGroup(boolean isOptional) {
        return new ArgumentGroupBuilder(isOptional);
    }

    public @NotNull FunctionContractBuilder withResult() {
        return new FunctionContractBuilder();
    }

    public @NotNull DataFlowFunction build() {
        return new AbstractDataFlowFunction() {
            @Override
            protected @NotNull Map<Map<Argument, Constraint>, Set<Result>> getResults() {
                return results;
            }

            @Override
            public @NotNull Block.FunctionBlock getBlock() {
                return functionBlock;
            }
        };
    }

    public class FunctionContractBuilder {

        private final @NotNull Map<Argument, Constraint> constraints = new HashMap<>();
        private final @NotNull DataFlowState state = DataFlowState.createState(functionBlock);
        private final @Nullable ReferenceValue output;

        public FunctionContractBuilder() {
            for (ArgumentGroup argumentGroup : functionBlock.getArgumentGroups()) {
                for (Argument argument : argumentGroup.arguments()) {
                    Optionality optionality = argumentGroup.isOptional() ? Optionality.UNKNOWN : Optionality.PRESENT;
                    constraints.put(argument, Constraint.any(argument.type(), optionality));
                }
            }
            if (functionBlock.getReturnType() != null) {
                output = new VariableSnapshot(functionBlock.getReturnType());
            } else {
                output = null;
            }
        }

        public @NotNull FunctionContractBuilder whereArgument(@NotNull String name, @NotNull Constraint constraint) {
            Argument argument = findArgument(name);
            ArgumentGroup argumentGroup = functionBlock.getArgumentGroups().stream()
                    .filter(group -> group.arguments().contains(argument))
                    .findFirst().orElseThrow();
            constraints.put(argument, constraint);
            if (constraint.getOptionality() == Optionality.PRESENT) {
                if (argumentGroup.arguments().stream().filter(value -> !(value.equals(argument))).anyMatch(value -> constraints.get(value).getOptionality() == Optionality.PRESENT)) {
                    throw new IllegalArgumentException();
                }
                for (Argument value : argumentGroup.arguments()) {
                    if (value == argument) {
                        continue;
                    }
                    constraints.put(value, constraints.get(value).and(Constraint.any(value.type(), Optionality.MISSING)));
                }
            }
            return this;
        }

        private @NotNull Argument findArgument(@NotNull String name) {
            return functionBlock.getArgumentGroups().stream()
                    .flatMap(argumentGroup -> argumentGroup.arguments().stream())
                    .filter(value -> value.name().equalsIgnoreCase(name))
                    .findFirst().orElseThrow();
        }

        public @NotNull FunctionContractBuilder withCondition(@NotNull BiConsumer<DataFlowState, VariableMap> consumer) {
            consumer.accept(state, new VariableMap());
            return this;
        }

        public @NotNull ContractBuilder withSuccess() {
            if (functionBlock.getReturnType() != null) {
                throw new IllegalStateException();
            }
            results.computeIfAbsent(constraints, value -> new HashSet<>(1));
            results.get(constraints).add(new Result.Success(List.of(), null));
            return ContractBuilder.this;
        }

        public @NotNull ContractBuilder withSuccess(@NotNull Constraint constraint) {
            if (functionBlock.getReturnType() == null) {
                throw new IllegalStateException();
            }
            ReferenceValue snapshot = Objects.requireNonNull(output);
            for (Argument argument : constraints.keySet()) {
                state.assign(new VariableValue(argument), constraints.get(argument));
            }
            state.assign(snapshot, constraint);
            results.computeIfAbsent(constraints, value -> new HashSet<>(1));
            results.get(constraints).add(new Result.Success(List.of(state), snapshot));
            return ContractBuilder.this;
        }

        public class VariableMap {

            public @NotNull ReferenceValue getArgument(@NotNull String name) {
                return new VariableValue(findArgument(name));
            }

            public @NotNull ReferenceValue getOutput() {
                return Objects.requireNonNull(output);
            }

        }
    }

    public class ArgumentGroupBuilder {

        private final @NotNull VirtualParameterGroup parameterGroup;

        public ArgumentGroupBuilder(boolean isOptional) {
            parameterGroup = new VirtualParameterGroup((VirtualRoutine) functionBlock.getElement(), isOptional, new ArrayList<>(2));
        }

        public @NotNull ArgumentGroupBuilder withArgument(@NotNull ParameterType parameterType, @NotNull String name, @NotNull RapidType type) {
            parameterGroup.getParameters().add(new VirtualParameter(parameterGroup, parameterType, name, type));
            return this;
        }

        public @NotNull ContractBuilder build() {
            VirtualRoutine element = (VirtualRoutine) functionBlock.getElement();
            List<VirtualParameterGroup> parameters = element.getParameters();
            Objects.requireNonNull(parameters);
            parameters.add(parameterGroup);
            int size = functionBlock.getArgumentGroups().stream()
                    .mapToInt(argumentGroup -> argumentGroup.arguments().size())
                    .sum();
            List<VirtualParameter> parameterList = parameterGroup.getParameters();
            List<Argument> arguments = new ArrayList<>(parameterList.size());
            for (int i = 0; i < parameterList.size(); i++) {
                VirtualParameter parameter = parameterList.get(i);
                Argument argument = new Argument(size + i, parameter.getParameterType(), parameter.getType(), parameter.getName());
                arguments.add(argument);
            }
            ArgumentGroup argumentGroup = new ArgumentGroup(parameterGroup.isOptional(), List.copyOf(arguments));
            functionBlock.getArgumentGroups().add(argumentGroup);
            return ContractBuilder.this;
        }
    }
}

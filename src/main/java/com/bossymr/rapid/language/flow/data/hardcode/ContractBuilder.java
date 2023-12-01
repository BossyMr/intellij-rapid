package com.bossymr.rapid.language.flow.data.hardcode;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.ArgumentGroup;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.data.AbstractDataFlowFunction;
import com.bossymr.rapid.language.flow.data.DataFlowFunction;
import com.bossymr.rapid.language.flow.data.DataFlowFunction.Result;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.flow.value.VariableExpression;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.virtual.VirtualParameter;
import com.bossymr.rapid.language.symbol.virtual.VirtualParameterGroup;
import com.bossymr.rapid.language.symbol.virtual.VirtualRoutine;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

public class ContractBuilder {

    private final @NotNull Block.FunctionBlock functionBlock;
    private final @NotNull Set<Result> results = new HashSet<>();

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
            public @NotNull Block.FunctionBlock getBlock() {
                return functionBlock;
            }

            @Override
            protected @NotNull Set<Result> getResults() {
                return results;
            }
        };
    }

    public class FunctionContractBuilder {

        private final @NotNull DataFlowState state = DataFlowState.createState(functionBlock);
        private final @Nullable ReferenceExpression output;

        public FunctionContractBuilder() {
            if (functionBlock.getReturnType() != null) {
                output = new VariableSnapshot(functionBlock.getReturnType());
            } else {
                output = null;
            }
        }

        private @NotNull Argument findArgument(@NotNull String name) {
            return functionBlock.getArgumentGroups().stream()
                    .flatMap(argumentGroup -> argumentGroup.arguments().stream())
                    .filter(value -> value.getName().equalsIgnoreCase(name))
                    .findFirst().orElseThrow();
        }

        public @NotNull FunctionContractBuilder withCondition(@NotNull BiConsumer<DataFlowState, VariableMap> consumer) {
            consumer.accept(state, new VariableMap());
            return this;
        }

        public @NotNull ContractBuilder withSuccess() {
            results.add(new Result.Success(state, output));
            return ContractBuilder.this;
        }

        public class VariableMap {

            public @NotNull ReferenceExpression getArgument(@NotNull String name) {
                return new VariableExpression(findArgument(name));
            }

            public @NotNull ReferenceExpression getOutput() {
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
            ArgumentGroup argumentGroup = getArgumentGroup(size);
            functionBlock.getArgumentGroups().add(argumentGroup);
            return ContractBuilder.this;
        }

        @NotNull
        private ArgumentGroup getArgumentGroup(int size) {
            List<VirtualParameter> parameterList = parameterGroup.getParameters();
            List<Argument> arguments = new ArrayList<>(parameterList.size());
            for (int i = 0; i < parameterList.size(); i++) {
                VirtualParameter parameter = parameterList.get(i);
                Argument argument = new Argument(size + i, parameter.getParameterType(), parameter.getType(), parameter.getName());
                arguments.add(argument);
            }
            return new ArgumentGroup(parameterGroup.isOptional(), List.copyOf(arguments));
        }
    }
}

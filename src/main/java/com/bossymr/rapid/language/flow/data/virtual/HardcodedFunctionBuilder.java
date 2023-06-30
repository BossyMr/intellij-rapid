package com.bossymr.rapid.language.flow.data.virtual;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.ArgumentGroup;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.Variable;
import com.bossymr.rapid.language.flow.condition.Condition;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.constraint.Optionality;
import com.bossymr.rapid.language.flow.data.DataFlowFunction;
import com.bossymr.rapid.language.flow.data.DataFlowState;
import com.bossymr.rapid.language.flow.value.ReferenceValue;
import com.bossymr.rapid.language.flow.value.VariableReference;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.RoutineType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class HardcodedFunctionBuilder {

    private final @NotNull Block.FunctionBlock functionBlock;
    private final @NotNull Map<Map<Argument, Constraint>, DataFlowFunction.Result> results = new HashMap<>();

    public HardcodedFunctionBuilder(@NotNull Block.FunctionBlock functionBlock) {
        this.functionBlock = functionBlock;
    }

    public static @NotNull HardcodedFunctionBuilder create(@NotNull String name, @NotNull RapidType returnType) {
        Block.FunctionBlock functionBlock = new Block.FunctionBlock(null, name, returnType, RoutineType.FUNCTION, true);
        return new HardcodedFunctionBuilder(functionBlock);
    }

    public static @NotNull HardcodedFunctionBuilder create(@NotNull String name) {
        Block.FunctionBlock functionBlock = new Block.FunctionBlock(null, name, null, RoutineType.PROCEDURE, true);
        return new HardcodedFunctionBuilder(functionBlock);
    }

    public @NotNull ArgumentGroupBuilder withArgumentGroup(boolean isOptional) {
        return new ArgumentGroupBuilder(isOptional);
    }

    public @NotNull HardcodedFunctionBuilder withResult(@NotNull Map<Integer, Constraint> arguments, @NotNull BiFunction<ReferenceValue, List<VariableReference>, Condition> result) {
        Map<Argument, Constraint> transformed = arguments.entrySet().stream()
                .map(entry -> Map.entry(functionBlock.findArgument(entry.getKey()), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        List<VariableReference> argumentsReferences = functionBlock.getArgumentGroups().stream()
                .flatMap(group -> group.arguments().stream())
                .map(argument -> new VariableReference(argument.type(), argument))
                .toList();
        assert functionBlock.getReturnType() != null;
        Variable variable = new Variable(argumentsReferences.size(), null, null, functionBlock.getReturnType(), null);
        Condition condition = result.apply(new VariableReference(functionBlock.getReturnType(), variable), argumentsReferences);
        DataFlowState dataFlowState = new DataFlowState(new ArrayList<>(List.of(condition)), new HashMap<>(Map.of()));
        results.put(transformed, new DataFlowFunction.Result.WithCondition(dataFlowState, condition));
        return this;
    }

    @Contract(pure = true)
    public @NotNull DataFlowFunction build() {
        return new DataFlowFunction() {
            @Override
            public @NotNull Block.FunctionBlock getBlock() {
                return functionBlock;
            }

            @Override
            public @NotNull Result getOutput(@NotNull Map<Argument, Constraint> arguments) {
                List<Result> combinations = getCombinations(arguments);
                return Result.combine(combinations);
            }
        };
    }

    @NotNull
    private List<DataFlowFunction.Result> getCombinations(Map<Argument, Constraint> arguments) {
        return results.keySet().stream()
                .filter(map -> {
                    for (Argument argument : map.keySet()) {
                        if (!arguments.containsKey(argument)) {
                            Optionality optionality = map.get(argument).getOptionality();
                            if (optionality == Optionality.PRESENT) {
                                return false;
                            }
                            continue;
                        }
                        if (!map.get(argument).contains(arguments.get(argument))) {
                            return false;
                        }
                    }
                    return true;
                }).map(results::get).toList();
    }

    public class ArgumentGroupBuilder {

        private final @NotNull ArgumentGroup argumentGroup;

        public ArgumentGroupBuilder(boolean isOptional) {
            this.argumentGroup = new ArgumentGroup(isOptional, new ArrayList<>());
        }

        public @NotNull ArgumentGroupBuilder withArgument(@NotNull String name, @NotNull RapidType type, @NotNull ParameterType parameterType) {
            Argument argument = new Argument(argumentGroup.arguments().size(), parameterType, type, name);
            argumentGroup.arguments().add(argument);
            return this;
        }

        public @NotNull HardcodedFunctionBuilder build() {
            functionBlock.getArgumentGroups().add(argumentGroup);
            return HardcodedFunctionBuilder.this;
        }
    }
}

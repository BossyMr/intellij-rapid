package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.BlockDescriptor;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.hardcode.HardcodedContract;
import com.bossymr.rapid.language.flow.debug.DataFlowUsage;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class DataFlowFunctionMap {

    /**
     * A map of blocks for each block descriptor.
     */
    private final @NotNull Map<BlockDescriptor, Block.FunctionBlock> descriptorMap;

    /**
     * A map of functions for each block descriptor.
     */
    private final @NotNull Map<BlockDescriptor, DataFlowFunction> functionMap;

    /**
     * A stack of blocks which should need to be reprocessed.
     */
    private final @NotNull Deque<DataFlowBlock> workList;

    private final @NotNull Map<DataFlowBlock, ResultEntry> softReferences = new HashMap<>();
    private final @NotNull Map<DataFlowBlock, DataFlowUsage> hardReferences = new HashMap<>();
    private final @NotNull Map<DataFlowFunction.Result, DataFlowBlock> exitPoints = new HashMap<>();


    public DataFlowFunctionMap(@NotNull Map<BlockDescriptor, Block.FunctionBlock> descriptorMap, @NotNull Deque<DataFlowBlock> workList) {
        this.descriptorMap = descriptorMap;
        this.functionMap = new HashMap<>();
        this.workList = workList;
        for (HardcodedContract value : HardcodedContract.values()) {
            DataFlowFunction function = value.getFunction();
            Block.FunctionBlock functionBlock = function.getBlock();
            BlockDescriptor blockKey = BlockDescriptor.getBlockKey(functionBlock);
            descriptorMap.put(blockKey, functionBlock);
            functionMap.put(blockKey, function);
        }
    }

    public @NotNull Map<DataFlowBlock, DataFlowUsage> getUsages() {
        return hardReferences;
    }

    private void registerExit(@NotNull DataFlowBlock returnBlock, @NotNull Map<Argument, Constraint> arguments, @NotNull DataFlowFunction.Result result) {
        Block block = returnBlock.getBasicBlock().getBlock();
        BlockDescriptor blockDescriptor = BlockDescriptor.getBlockKey(block);
        ResultEntry outputEntry = new ResultEntry(blockDescriptor, arguments);
        exitPoints.put(result, returnBlock);
        for (DataFlowBlock callerBlock : softReferences.keySet()) {
            ResultEntry resultEntry = softReferences.get(callerBlock);
            if (resultEntry.isSimilar(outputEntry)) {
                if (!(workList.contains(callerBlock))) {
                    workList.add(callerBlock);
                }
            }
        }
        if (hardReferences.containsKey(returnBlock)) {
            DataFlowUsage usage = hardReferences.get(returnBlock);
            for (DataFlowBlock callerBlock : usage.usages()) {
                if (!(workList.contains(callerBlock))) {
                    workList.add(callerBlock);
                }
            }
        }
    }

    private void registerUsage(@NotNull DataFlowBlock callerBlock, @NotNull Map<Argument, Constraint> arguments, @NotNull BlockDescriptor blockDescriptor, @NotNull DataFlowFunction.Result result) {
        if (exitPoints.containsKey(result)) {
            softReferences.remove(callerBlock);
            DataFlowBlock returnBlock = exitPoints.get(result);
            if (hardReferences.containsKey(returnBlock)) {
                DataFlowUsage usage = hardReferences.get(returnBlock);
                usage.usages().add(callerBlock);
            } else {
                DataFlowUsage usage = new DataFlowUsage(getUsageType(result), new HashSet<>());
                usage.usages().add(callerBlock);
                hardReferences.put(returnBlock, usage);
            }
        } else {
            softReferences.put(callerBlock, new ResultEntry(blockDescriptor, arguments));
        }
    }

    public @NotNull Optional<DataFlowFunction> get(@NotNull BlockDescriptor blockDescriptor) {
        if (functionMap.containsKey(blockDescriptor)) {
            return Optional.ofNullable(functionMap.get(blockDescriptor));
        }
        if (!(descriptorMap.containsKey(blockDescriptor))) {
            return Optional.empty();
        }
        Block.FunctionBlock functionBlock = descriptorMap.get(blockDescriptor);
        return Optional.of(new PhysicalDataFlowFunction(functionBlock));
    }

    public void set(@NotNull BlockDescriptor blockDescriptor, @NotNull DataFlowBlock returnBlock, @NotNull Map<Argument, Constraint> arguments, @NotNull DataFlowFunction.Result result) {
        if (functionMap.containsKey(blockDescriptor)) {
            /*
             * The function has already been created. It should be modified to provide the specified result.
             */
            if (!(functionMap.get(blockDescriptor) instanceof PhysicalDataFlowFunction function)) {
                throw new IllegalStateException();
            }
            function.addResult(returnBlock, arguments, result);
        } else {
            /*
             * The function has not been processed, and must be created.
             */
            PhysicalDataFlowFunction function = new PhysicalDataFlowFunction(descriptorMap.get(blockDescriptor));
            function.addResult(returnBlock, arguments, result);
            functionMap.put(blockDescriptor, function);
        }
        registerExit(returnBlock, arguments, result);
    }

    private @NotNull DataFlowUsage.DataFlowUsageType getUsageType(DataFlowFunction.@NotNull Result result) {
        if (result instanceof DataFlowFunction.Result.Success) {
            return DataFlowUsage.DataFlowUsageType.SUCCESS;
        } else if (result instanceof DataFlowFunction.Result.Error) {
            return DataFlowUsage.DataFlowUsageType.ERROR;
        } else if (result instanceof DataFlowFunction.Result.Exit) {
            return DataFlowUsage.DataFlowUsageType.EXIT;
        } else {
            throw new AssertionError();
        }
    }

    public record ResultEntry(@NotNull BlockDescriptor blockDescriptor, @NotNull Map<Argument, Constraint> arguments) {

        public boolean isSimilar(@NotNull ResultEntry entry) {
            if (!(blockDescriptor().equals(entry.blockDescriptor()))) {
                return false;
            }
            if (arguments().size() != entry.arguments().size()) {
                return false;
            }
            for (Argument argument : arguments.keySet()) {
                if (!(entry.arguments().containsKey(argument))) {
                    return false;
                }
                if (!(arguments().get(argument).intersects(entry.arguments().get(argument)))) {
                    return false;
                }
            }
            return true;
        }
    }

    private final class PhysicalDataFlowFunction extends AbstractDataFlowFunction {

        private final @NotNull Block.FunctionBlock functionBlock;
        private final @NotNull AtomicReference<Map<Map<Argument, Constraint>, Map<DataFlowBlock, Result>>> result = new AtomicReference<>();

        public PhysicalDataFlowFunction(@NotNull Block.FunctionBlock functionBlock) {
            this.functionBlock = functionBlock;
        }

        public void setResult(@NotNull Map<Map<Argument, Constraint>, Map<DataFlowBlock, Result>> result) {
            this.result.set(result);
        }

        public void addResult(@NotNull DataFlowBlock returnBlock, @NotNull Map<Argument, Constraint> constraints, @NotNull DataFlowFunction.Result value) {
            Map<Map<Argument, Constraint>, Map<DataFlowBlock, Result>> map = result.get();
            if (map == null) {
                Map<Map<Argument, Constraint>, Map<DataFlowBlock, Result>> data = new HashMap<>();
                data.put(constraints, new HashMap<>());
                data.get(constraints).put(returnBlock, value);
                setResult(data);
            } else {
                map.computeIfAbsent(constraints, (k) -> new HashMap<>());
                Map<DataFlowBlock, Result> entries = map.get(constraints);
                entries.put(returnBlock, value);
            }
        }

        @Override
        public @NotNull Block.FunctionBlock getBlock() {
            return functionBlock;
        }

        @Override
        protected @NotNull Map<Map<Argument, Constraint>, Set<Result>> getResults() {
            Map<Map<Argument, Constraint>, Set<Result>> map = new HashMap<>(result.get().size(), 1);
            result.get().forEach((arguments, results) -> {
                map.computeIfAbsent(arguments, value -> new HashSet<>(1));
                map.get(arguments).addAll(results.values());
            });
            return map;
        }

        @Override
        public @NotNull Set<Result> getOutput(@NotNull DataFlowBlock callerBlock, @NotNull Map<Argument, Constraint> arguments) {
            Map<Map<Argument, Constraint>, Map<DataFlowBlock, Result>> value = result.get();
            if (value != null) {
                Set<Result> results = super.getOutput(callerBlock, arguments);
                for (Result output : results) {
                    registerUsage(callerBlock, arguments, BlockDescriptor.getBlockKey(functionBlock), output);
                }
                return results;
            }
            Map<Argument, Constraint> constraints = new HashMap<>();
            for (Argument argument : arguments.keySet()) {
                constraints.put(argument, Constraint.any(argument.type()));
            }
            RapidType returnType = functionBlock.getReturnType();
            Constraint returnConstraint = returnType != null ? Constraint.any(returnType) : null;
            Result output = Result.Success.create(functionBlock, constraints, returnType, returnConstraint);
            registerUsage(callerBlock, arguments, BlockDescriptor.getBlockKey(functionBlock), output);
            return Set.of(output);
        }
    }
}

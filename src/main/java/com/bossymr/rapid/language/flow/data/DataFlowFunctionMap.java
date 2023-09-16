package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.BlockDescriptor;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.hardcode.HardcodedContract;
import com.bossymr.rapid.language.flow.debug.DataFlowUsage;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.type.RapidType;
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

    private final @NotNull Map<DataFlowBlock, Set<DataFlowFunction.Result>> usages = new HashMap<>();

    /**
     * A map of references between a caller block and its entry. If the return block for a caller has not yet been
     * processed, it is added as a soft reference. Once the block has been processed, all its soft references are
     * cleared and added to the work-list to be reprocessed. When reprocessed, it will find the correct return block and
     * instead add the reference as a hard reference.
     */
    private final @NotNull Map<DataFlowBlock, ResultEntry> softReferences = new HashMap<>();

    /**
     * A map of references between the return block and its usages.
     */
    private final @NotNull Map<DataFlowBlock, DataFlowUsage> hardReferences = new HashMap<>();

    /**
     * A map of references between the result of a function and the block where it is returned.
     */
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

    private void registerUsage(@NotNull DataFlowBlock callerBlock, @NotNull BlockDescriptor blockDescriptor, @NotNull DataFlowFunction.Result result) {
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
            softReferences.put(callerBlock, new ResultEntry(blockDescriptor));
        }
    }

    public @NotNull Set<DataFlowFunction.Result> getCalls(@NotNull DataFlowBlock block) {
        return usages.getOrDefault(block, Set.of());
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

    public void set(@NotNull BlockDescriptor blockDescriptor, @NotNull DataFlowBlock returnBlock, @NotNull DataFlowFunction.Result result) {
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
        private final @NotNull AtomicReference<Map<DataFlowBlock, Result>> result = new AtomicReference<>();

        public PhysicalDataFlowFunction(@NotNull Block.FunctionBlock functionBlock) {
            this.functionBlock = functionBlock;
        }

        public void setResult(@NotNull Map<DataFlowBlock, Result> result) {
            this.result.set(result);
        }

        public void addResult(@NotNull DataFlowBlock returnBlock, @NotNull DataFlowFunction.Result value) {
            Map<DataFlowBlock, Result> map = result.get();
            if (map == null) {
                Map<DataFlowBlock, Result> data = new HashMap<>();
                data.put(returnBlock, value);
                setResult(data);
            } else {
                map.put(returnBlock, value);
            }
        }


        @Override
        public @NotNull Block.FunctionBlock getBlock() {
            return functionBlock;
        }

        @Override
        protected @NotNull Set<Result> getResults() {
            Map<DataFlowBlock, Result> value = result.get();
            if (value != null) {
                return new HashSet<>(value.values());
            }
            return Set.of();
        }

        @Override
        public @NotNull Set<Result> getOutput(@NotNull DataFlowState state, @NotNull BranchingInstruction.CallInstruction instruction) {
            Map<DataFlowBlock, Result> value = result.get();
            if (value != null) {
                Set<Result> results = super.getOutput(state, instruction);
                Optional<DataFlowBlock> block = state.getBlock();
                if (block.isPresent()) {
                    usages.put(block.orElseThrow(), Set.copyOf(results));
                    for (Result output : results) {
                        registerUsage(block.orElseThrow(), BlockDescriptor.getBlockKey(functionBlock), output);
                    }
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
            Set<Result> result = Set.of(output);
            usages.put(callerBlock, result);
            registerUsage(callerBlock, arguments, BlockDescriptor.getBlockKey(functionBlock), output);
            return result;
        }
    }
}

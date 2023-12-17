package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.BlockDescriptor;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.snapshots.Snapshot;
import com.bossymr.rapid.language.flow.debug.DataFlowUsage;
import com.bossymr.rapid.language.flow.instruction.CallInstruction;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

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
     * A stack of states which should need to be reprocessed.
     */
    private final @NotNull Deque<DataFlowState> workList;

    /**
     * A map of references between a caller block and its entry. If the return block for a caller has not yet been
     * processed, it is added as a soft reference. Once the block has been processed, all its soft references are
     * cleared and added to the work-list to be reprocessed. When reprocessed, it will find the correct return block and
     * instead add the reference as a hard reference.
     */
    private final @NotNull Map<DataFlowState, BlockDescriptor> softReferences = new HashMap<>();

    /**
     * A map of references between the return block and its usages.
     */
    private final @NotNull Map<DataFlowState, DataFlowUsage> hardReferences = new HashMap<>();

    /**
     * A map of references between the result of a function and the block where it is returned.
     */
    private final @NotNull Map<DataFlowFunction.Result, DataFlowState> exitPoints = new HashMap<>();

    /**
     * A {@code Consumer} called for each declared usage to a function which has not been processed.
     */
    private final @NotNull BiConsumer<BlockDescriptor, DataFlowFunctionMap> consumer;

    public DataFlowFunctionMap(@NotNull Map<BlockDescriptor, Block.FunctionBlock> descriptorMap, @NotNull Deque<DataFlowState> workList, @NotNull BiConsumer<BlockDescriptor, DataFlowFunctionMap> consumer) {
        this.descriptorMap = descriptorMap;
        this.consumer = consumer;
        this.functionMap = new HashMap<>();
        this.workList = workList;
    }

    public @NotNull Deque<DataFlowState> getWorkList() {
        return workList;
    }

    public @NotNull Map<DataFlowState, DataFlowUsage> getUsages() {
        return hardReferences;
    }

    /**
     * Register a state where the routine halts' execution.
     *
     * @param returnState the state where the routine halt's execution.
     * @param result the result.
     */
    private void registerExit(@NotNull DataFlowState returnState, @NotNull DataFlowFunction.Result result) {
        Instruction instruction = returnState.getBlock().getInstruction();
        Block block = instruction.getBlock();
        BlockDescriptor calleeDescriptor = BlockDescriptor.getBlockKey(block);
        exitPoints.put(result, returnState);
        // Find calls which reference this state, but were processed before this state.
        for (DataFlowState callerState : softReferences.keySet()) {
            BlockDescriptor callerDescriptor = softReferences.get(callerState);
            if (!(callerDescriptor.equals(calleeDescriptor))) {
                continue;
            }
            if (!(workList.contains(callerState))) {
                workList.add(callerState);
            }
        }
        if (hardReferences.containsKey(returnState)) {
            DataFlowUsage usage = hardReferences.get(returnState);
            for (DataFlowState callerState : usage.usages()) {
                if (!(workList.contains(callerState))) {
                    workList.add(callerState);
                }
            }
            usage.usages().clear();
        } else {
            hardReferences.put(returnState, new DataFlowUsage(getUsageType(result), new HashSet<>()));
        }
    }

    private void registerUsage(@NotNull DataFlowState callerState, @NotNull DataFlowFunction.Result result) {
        DataFlowState resultState = exitPoints.get(result);
        softReferences.remove(callerState);
        if (hardReferences.containsKey(resultState)) {
            DataFlowUsage usage = hardReferences.get(resultState);
            usage.usages().add(callerState);
        } else {
            DataFlowUsage usage = new DataFlowUsage(getUsageType(result), new HashSet<>());
            usage.usages().add(callerState);
            hardReferences.put(resultState, usage);
        }
    }

    private void registerUsage(@NotNull DataFlowState callerState, @NotNull BlockDescriptor blockDescriptor) {
        softReferences.put(callerState, blockDescriptor);
        consumer.accept(blockDescriptor, this);
    }

    public @NotNull Optional<DataFlowFunction> get(@NotNull BlockDescriptor blockDescriptor) {
        if (!(descriptorMap.containsKey(blockDescriptor))) {
            if (blockDescriptor.moduleName().isEmpty()) {
                consumer.accept(blockDescriptor, this);
            }
        }
        if (functionMap.containsKey(blockDescriptor)) {
            return Optional.ofNullable(functionMap.get(blockDescriptor));
        }
        if (!(descriptorMap.containsKey(blockDescriptor))) {
            return Optional.empty();
        }
        Block.FunctionBlock functionBlock = descriptorMap.get(blockDescriptor);
        return Optional.of(new PhysicalDataFlowFunction(functionBlock));
    }

    public void set(@NotNull BlockDescriptor blockDescriptor, @NotNull DataFlowState returnState, @NotNull DataFlowFunction.Result result) {
        if (functionMap.containsKey(blockDescriptor)) {
            /*
             * The function has already been created. It should be modified to provide the specified result.
             */
            if (!(functionMap.get(blockDescriptor) instanceof PhysicalDataFlowFunction function)) {
                throw new IllegalStateException();
            }
            function.addResult(returnState, result);
        } else {
            /*
             * The function has not been processed, and must be created.
             */
            PhysicalDataFlowFunction function = new PhysicalDataFlowFunction(descriptorMap.get(blockDescriptor));
            function.addResult(returnState, result);
            functionMap.put(blockDescriptor, function);
        }
        registerExit(returnState, result);
    }

    public void unregisterState(@NotNull DataFlowState state) {
        if (hardReferences.containsKey(state)) {
            // This state is a return state
            exitPoints.entrySet().removeIf(value -> value.equals(state));
            DataFlowUsage usage = hardReferences.remove(state);
            Set<DataFlowState> usages = usage.usages();
            for (DataFlowState callerState : usages) {
                if (!(workList.contains(callerState))) {
                    workList.add(callerState);
                }
            }
        }
        Instruction instruction = state.getBlock().getInstruction();
        softReferences.remove(state);
        if (instruction instanceof CallInstruction) {
            for (DataFlowState calleeState : hardReferences.keySet()) {
                DataFlowUsage usage = hardReferences.get(calleeState);
                usage.usages().removeIf(callerState -> callerState.equals(state));
            }
        }
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

    private final class PhysicalDataFlowFunction extends AbstractDataFlowFunction {

        private final @NotNull Block.FunctionBlock functionBlock;
        private final @NotNull AtomicReference<Map<DataFlowState, Result>> result = new AtomicReference<>();

        public PhysicalDataFlowFunction(@NotNull Block.FunctionBlock functionBlock) {
            this.functionBlock = functionBlock;
        }

        public void setResult(@NotNull Map<DataFlowState, Result> result) {
            this.result.set(result);
        }

        public void addResult(@NotNull DataFlowState returnState, @NotNull DataFlowFunction.Result value) {
            Map<DataFlowState, Result> map = result.get();
            if (map == null) {
                Map<DataFlowState, Result> data = new HashMap<>();
                data.put(returnState, value);
                setResult(data);
            } else {
                map.put(returnState, value);
            }
        }


        @Override
        public @NotNull Block.FunctionBlock getBlock() {
            return functionBlock;
        }

        @Override
        protected @NotNull Set<Result> getResults() {
            Map<DataFlowState, Result> value = result.get();
            if (value != null) {
                return new HashSet<>(value.values());
            }
            return Set.of();
        }

        @Override
        public @NotNull Set<Result> getOutput(@NotNull DataFlowState state, @NotNull CallInstruction instruction) {
            Map<DataFlowState, Result> value = result.get();
            BlockDescriptor blockDescriptor = BlockDescriptor.getBlockKey(functionBlock);
            if (value != null) {
                Set<DataFlowFunction.Result> results = new HashSet<>();
                for (Result result : getResults()) {
                    Result output = getOutput(result, state, instruction);
                    if (output != null) {
                        registerUsage(state, result);
                        results.add(output);
                    }
                }
                return results;
            }
            RapidType returnType = functionBlock.getReturnType();
            DataFlowState successorState = DataFlowState.createSuccessorState(state.getBlock(), state);
            Snapshot returnValue = returnType != null ? Snapshot.createSnapshot(returnType) : null;
            for (ReferenceExpression variable : instruction.getArguments().values()) {
                successorState.createSnapshot(variable);
            }
            Result output = new Result.Success(successorState, returnValue);
            registerUsage(state, blockDescriptor);
            return Set.of(output);
        }
    }
}

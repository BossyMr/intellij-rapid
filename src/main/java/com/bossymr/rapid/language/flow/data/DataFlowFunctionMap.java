package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.BlockDescriptor;
import com.bossymr.rapid.language.flow.constraint.Constraint;
import com.bossymr.rapid.language.flow.data.hardcode.HardcodedContract;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class DataFlowFunctionMap {

    private final @NotNull Map<BlockDescriptor, Block.FunctionBlock> descriptorMap;
    private final @NotNull Map<BlockDescriptor, DataFlowFunction> functionMap;
    private final @NotNull Deque<WorkListEntry> workList;

    public DataFlowFunctionMap(@NotNull Map<BlockDescriptor, Block.FunctionBlock> descriptorMap, @NotNull Deque<WorkListEntry> workList) {
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

    public @NotNull DataFlowFunction get(@NotNull DataFlowBlock currentBlock, @NotNull BlockDescriptor blockDescriptor) {
        if (functionMap.containsKey(blockDescriptor)) {
            return functionMap.get(blockDescriptor);
        }
        WorkListEntry entry = new WorkListEntry(blockDescriptor, currentBlock);
        if (!(workList.contains(entry))) {
            workList.add(entry);
        }
        Block.FunctionBlock functionBlock = descriptorMap.get(blockDescriptor);
        if (functionBlock == null) {
            throw new IllegalStateException("Could not find function: " + blockDescriptor.moduleName() + ":" + blockDescriptor.name());
        }
        return new PhysicalDataFlowFunction(functionBlock);
    }

    public void set(@NotNull BlockDescriptor blockDescriptor, @NotNull DataFlowBlock returnBlock, @NotNull Map<Argument, Constraint> constraints, @NotNull DataFlowFunction.Result result) {
        if (functionMap.containsKey(blockDescriptor)) {
            if (!(functionMap.get(blockDescriptor) instanceof PhysicalDataFlowFunction function)) {
                throw new IllegalStateException();
            }
            function.addResult(returnBlock, constraints, result);
        } else {
            PhysicalDataFlowFunction function = new PhysicalDataFlowFunction(descriptorMap.get(blockDescriptor));
            function.addResult(returnBlock, constraints, result);
            functionMap.put(blockDescriptor, function);
        }
    }

    public record WorkListEntry(@NotNull BlockDescriptor descriptor, @NotNull DataFlowBlock block) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WorkListEntry that = (WorkListEntry) o;
            return Objects.equals(block, that.block);
        }

        @Override
        public int hashCode() {
            return Objects.hash(block);
        }
    }

    private static final class PhysicalDataFlowFunction extends AbstractDataFlowFunction {

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
        public @NotNull Set<Result> getOutput(@NotNull Map<Argument, Constraint> arguments) {
            Map<Map<Argument, Constraint>, Map<DataFlowBlock, Result>> value = result.get();
            if (value != null) {
                return super.getOutput(arguments);
            }
            Map<Argument, Constraint> constraints = new HashMap<>();
            for (Argument argument : arguments.keySet()) {
                constraints.put(argument, Constraint.any(argument.type()));
            }
            RapidType returnType = functionBlock.getReturnType();
            Constraint returnConstraint = returnType != null ? Constraint.any(returnType) : null;
            return Set.of(Result.Success.create(functionBlock, constraints, returnType, returnConstraint));
        }


    }
}

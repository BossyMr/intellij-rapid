package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.data.DataFlow;
import com.bossymr.rapid.language.flow.data.DataFlowAnalyzer;
import com.bossymr.rapid.language.flow.data.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.DataFlowFunctionMap;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@code ControlFlow} instance represents the control flow graph for a program.
 * <p>
 * The control flow graph is seperated in blocks, which can be retrieved with either {@link #getBlocks()} or
 * {@link #getBlock(String, String)}.
 */
public class ControlFlow {

    private final @NotNull Project project;
    private final @NotNull Map<BlockDescriptor, Block> map;
    private final @NotNull DataFlow dataFlow;

    public ControlFlow(@NotNull Project project, @NotNull Map<BlockDescriptor, Block> map) {
        this.map = Map.copyOf(map);
        this.project = project;
        this.dataFlow = createDataFlow();
    }

    private @NotNull DataFlow createDataFlow() {
        Stream<Block.FunctionBlock> stream = getBlocks().stream()
                .filter(block -> block instanceof Block.FunctionBlock)
                .map(block -> (Block.FunctionBlock) block);
        Map<BasicBlock, DataFlowBlock> dataFlow = new HashMap<>();
        Map<BlockDescriptor, Block.FunctionBlock> descriptorMap = stream.collect(Collectors.toMap(BlockDescriptor::getBlockKey, block -> block));
        Deque<DataFlowFunctionMap.WorkListEntry> workList = new ArrayDeque<>();
        DataFlowFunctionMap functionMap = new DataFlowFunctionMap(descriptorMap, workList);
        for (Block block : getBlocks()) {
            if (!(block instanceof Block.FunctionBlock functionBlock)) {
                continue;
            }
            Map<BasicBlock, DataFlowBlock> result = DataFlowAnalyzer.analyze(functionBlock, functionMap);
            dataFlow.putAll(result);
        }
        for (DataFlowFunctionMap.WorkListEntry entry : workList) {
            Block.FunctionBlock block = ((Block.FunctionBlock) entry.block().getBasicBlock().getBlock());
            DataFlowAnalyzer.reanalyze(block, functionMap, dataFlow, Set.of(entry.block()));
        }
        return new DataFlow(this, dataFlow);
    }

    public @NotNull Project getProject() {
        return project;
    }

    public @NotNull DataFlow getDataFlow() {
        return dataFlow;
    }

    public void accept(@NotNull ControlFlowVisitor visitor) {
        visitor.visitControlFlow(this);
    }

    /**
     * Returns all blocks in this control flow graph.
     *
     * @return a collection containing all blocks in this control flow graph.
     */
    public @NotNull Collection<Block> getBlocks() {
        return map.values();
    }

    /**
     * Returns the block in this control flow graph with the specified name, and which was declared in a module with the
     * specified name.
     *
     * @param moduleName the name of the module in which the block was declared.
     * @param name the name of the block.
     * @return the block, or {@code null} if a suitable block was not found.
     */
    public @Nullable Block getBlock(@NotNull String moduleName, @NotNull String name) {
        return map.get(new BlockDescriptor(moduleName, name));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ControlFlow that = (ControlFlow) o;
        return Objects.equals(project, that.project) && Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(project, map);
    }

    @Override
    public String toString() {
        return "ControlFlow{" +
                "blocks=" + getBlocks() +
                '}';
    }
}

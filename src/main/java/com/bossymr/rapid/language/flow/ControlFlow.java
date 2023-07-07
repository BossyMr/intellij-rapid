package com.bossymr.rapid.language.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@code ControlFlow} instance represents the control flow graph for a program.
 * <p>
 * The control flow graph is seperated in blocks, which can be retrieved with either {@link #getBlocks()} or
 * {@link #getBlock(String, String)}.
 */
public class ControlFlow {

    private final @NotNull Map<BlockDescriptor, Block> map;

    public ControlFlow() {
        this.map = new HashMap<>();
    }

    public <T> T accept(@NotNull ControlFlowVisitor<T> visitor) {
        return visitor.visitControlFlow(this);
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

    public void setBlock(@NotNull Block block) {
        map.put(BlockDescriptor.getBlockKey(block), block);
    }
}

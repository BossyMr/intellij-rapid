package com.bossymr.rapid.language.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * A {@code ControlFlow} instance represents the control flow graph for a program.
 * <p>
 * The control flow graph is seperated in blocks, which can be retrieved with either {@link #getBlocks()} or
 * {@link #getBlock(String, String)}.
 */
public class ControlFlow {

    private final @NotNull Map<BlockDescriptor, Block> map;

    public ControlFlow(@NotNull Map<BlockDescriptor, Block> map) {
        this.map = Map.copyOf(map);
    }

    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ControlFlow that = (ControlFlow) o;
        return Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    @Override
    public String toString() {
        return "ControlFlow{" +
                "blocks=" + getBlocks() +
                '}';
    }
}

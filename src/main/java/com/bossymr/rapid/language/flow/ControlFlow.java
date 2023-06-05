package com.bossymr.rapid.language.flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ControlFlow {

    /**
     * A map which contains all objects in the control flow graph.
     * <p>
     * The key to each object refers to the "moduleName:routineName" of the object.
     */
    private final @NotNull Map<String, Block> map;

    public ControlFlow() {
        this.map = new HashMap<>();
    }

    public @NotNull Collection<Block> getBlocks() {
        return map.values();
    }

    public void accept(@NotNull ControlFlowVisitor visitor) {
        visitor.visitControlFlow(this);
    }

    public void insertBlock(@NotNull Block block) {
        map.put(block.getModuleName() + ":" + block.getName(), block);
    }

    public @Nullable Block getBlock(@NotNull String moduleName, @NotNull String name) {
        return map.get(moduleName + ":" + name);
    }
}

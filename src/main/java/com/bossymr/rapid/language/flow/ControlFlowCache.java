package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class ControlFlowCache {
    private final Map<RapidRoutine, CacheEntry> cache = new WeakHashMap<>();

    @RequiresReadLock
    public @NotNull Set<ControlFlowBlock> getDataFlow() {
        Set<ControlFlowBlock> blocks = new HashSet<>();
        for (Map.Entry<RapidRoutine, CacheEntry> entry : cache.entrySet()) {
            if (entry.getKey() instanceof PhysicalRoutine routine && !(routine.isValid())) {
                continue;
            }
            blocks.add(entry.getValue().getDataFlow(entry.getKey()));
        }
        return blocks;
    }

    @RequiresReadLock
    public @NotNull Set<Block> getControlFLow() {
        Set<Block> blocks = new HashSet<>();
        for (Map.Entry<RapidRoutine, CacheEntry> entry : cache.entrySet()) {
            if (entry.getKey() instanceof PhysicalRoutine routine && !(routine.isValid())) {
                continue;
            }
            blocks.add(entry.getValue().getControlFlow(entry.getKey()));
        }
        return blocks;
    }

    @RequiresReadLock
    public @Nullable ControlFlowBlock getDataFlowIfAvailable(@NotNull RapidRoutine routine) {
        if (cache.containsKey(routine)) {
            CacheEntry entry = cache.get(routine);
            synchronized (entry) {
                return entry.getDataFlowIfAvailable();
            }
        }
        return null;
    }

    @RequiresReadLock
    public @NotNull ControlFlowBlock getDataFlow(@NotNull RapidRoutine routine) {
        return getDataFlow(Set.of(routine), routine);
    }

    @RequiresReadLock
    public @NotNull ControlFlowBlock getDataFlow(@NotNull Set<RapidRoutine> stack, @NotNull RapidRoutine routine) {
        CacheEntry entry = cache.computeIfAbsent(routine, ignored -> new CacheEntry());
        synchronized (entry) {
            return entry.getDataFlow(routine, stack);
        }
    }

    @RequiresReadLock
    public @NotNull Block getControlFlow(@NotNull RapidRoutine routine) {
        CacheEntry entry = cache.computeIfAbsent(routine, ignored -> new CacheEntry());
        synchronized (entry) {
            return entry.getControlFlow(routine);
        }
    }

    public void clear() {
        cache.clear();
    }

}

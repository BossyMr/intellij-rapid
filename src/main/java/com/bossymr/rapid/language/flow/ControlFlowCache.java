package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ControlFlowCache {
    private final Map<RapidRoutine, CacheEntry> cache = new WeakHashMap<>();

    @RequiresReadLock
    public synchronized @NotNull Set<ControlFlowBlock> getDataFlow() {
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
    public synchronized @NotNull Set<Block> getControlFLow() {
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
    public synchronized @Nullable ControlFlowBlock getDataFlowIfAvailable(@NotNull RapidRoutine routine) {
        if(cache.containsKey(routine)) {
            return cache.get(routine).getDataFlowIfAvailable();
        }
        return null;
    }

    @RequiresReadLock
    public synchronized @NotNull ControlFlowBlock getDataFlow(@NotNull RapidRoutine routine) {
        return getDataFlow(Set.of(routine), routine);
    }

    @RequiresReadLock
    public synchronized @NotNull ControlFlowBlock getDataFlow(@NotNull Set<RapidRoutine> stack, @NotNull RapidRoutine routine) {
        if(cache.containsKey(routine)) {
            return cache.get(routine).getDataFlow(routine, stack);
        }
        CacheEntry entry = new CacheEntry();
        cache.put(routine, entry);
        return entry.getDataFlow(routine, stack);
    }

    @RequiresReadLock
    public synchronized @NotNull Block getControlFlow(@NotNull RapidRoutine routine) {
        if(cache.containsKey(routine)) {
            return cache.get(routine).getControlFlow(routine);
        }
        CacheEntry entry = new CacheEntry();
        cache.put(routine, entry);
        return entry.getControlFlow(routine);
    }

    public void clear() {
        cache.clear();
    }

}

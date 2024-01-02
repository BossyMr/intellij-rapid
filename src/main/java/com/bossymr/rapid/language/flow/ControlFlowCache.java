package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.builder.ArgumentDescriptor;
import com.bossymr.rapid.language.flow.builder.ControlFlowBuilder;
import com.bossymr.rapid.language.flow.data.DataFlowAnalyzer;
import com.bossymr.rapid.language.flow.data.hardcode.HardcodedContract;
import com.bossymr.rapid.language.flow.instruction.*;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.physical.*;
import com.bossymr.rapid.language.symbol.virtual.VirtualRoutine;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.model.Pointer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class ControlFlowCache {
    private static final Logger logger = Logger.getInstance(ControlFlowService.class);

    private final Map<RapidRoutine, Entry> cache = new WeakHashMap<>();

    public synchronized @NotNull Set<ControlFlowBlock> getDataFlow() {
        Set<ControlFlowBlock> blocks = new HashSet<>();
        for (Entry value : cache.values()) {
            ControlFlowBlock block = value.getBlock();
            if (block != null) {
                blocks.add(block);
            }
        }
        return blocks;
    }

    @RequiresReadLock
    public synchronized @NotNull ControlFlowBlock getDataFlow(@NotNull Set<RapidRoutine> stack, @NotNull RapidRoutine routine) {
        ControlFlowBlock block = getControlFlow(routine);
        if (!(block.hasDataFlowGraph())) {
            computeDataFlow(stack, block);
        }
        return block;
    }

    @RequiresReadLock
    public synchronized @NotNull ControlFlowBlock getControlFlow(@NotNull RapidRoutine routine) {
        if (cache.containsKey(routine)) {
            ControlFlowBlock block = cache.get(routine).getBlock();
            if (block != null) {
                return block;
            }
        }
        Block controlFlow = computeControlFlow(routine);
        ControlFlowBlock block = new ControlFlowBlock(controlFlow);
        Map<PhysicalSymbol, Long> dependencies = routine instanceof PhysicalRoutine physicalRoutine ? getDependencies(physicalRoutine, controlFlow) : new HashMap<>();
        cache.put(routine, new Entry(block, dependencies));
        return block;
    }

    private @NotNull Map<PhysicalSymbol, Long> getDependencies(@NotNull PhysicalRoutine routine, @NotNull Block block) {
        Map<PhysicalSymbol, Long> dependencies = new HashMap<>();
        for (Instruction instruction : block.getInstructions()) {
            Set<PhysicalSymbol> symbols = getDependencies(instruction);
            symbols.add(routine);
            for (PhysicalSymbol symbol : symbols) {
                dependencies.put(symbol, symbol.getModificationCount());
            }
        }
        return dependencies;
    }

    private @NotNull Set<PhysicalSymbol> getDependencies(@NotNull Instruction instruction) {
        Set<PhysicalSymbol> symbols = new HashSet<>();
        if (instruction instanceof ReturnInstruction returnInstruction) {
            getDependencies(symbols, returnInstruction.getReturnValue());
        } else if (instruction instanceof ThrowInstruction throwInstruction) {
            getDependencies(symbols, throwInstruction.getExceptionValue());
        } else if (instruction instanceof AssignmentInstruction assignmentInstruction) {
            getDependencies(symbols, assignmentInstruction.getVariable());
            getDependencies(symbols, assignmentInstruction.getExpression());
        } else if (instruction instanceof ConditionalBranchingInstruction conditionalBranchingInstruction) {
            getDependencies(symbols, conditionalBranchingInstruction.getCondition());
        } else if (instruction instanceof ConnectInstruction connectInstruction) {
            getDependencies(symbols, connectInstruction.getVariable());
            getDependencies(symbols, connectInstruction.getExpression());
        } else if (instruction instanceof CallInstruction callInstruction) {
            getDependencies(symbols, callInstruction.getRoutineName());
            getDependencies(symbols, callInstruction.getReturnValue());
            Map<ArgumentDescriptor, ReferenceExpression> arguments = callInstruction.getArguments();
            for (ReferenceExpression expression : arguments.values()) {
                getDependencies(symbols, expression);
            }
            Expression routineName = callInstruction.getRoutineName();
            if (routineName.getElement() instanceof RapidReferenceExpression referenceExpression) {
                RapidSymbol symbol = referenceExpression.getSymbol();
                if (symbol instanceof PhysicalRoutine routine) {
                    symbols.add(routine);
                }
            }
        }
        return symbols;
    }

    private void getDependencies(@NotNull Set<PhysicalSymbol> symbols, @Nullable Expression expression) {
        if (expression == null) {
            return;
        }
        for (Expression component : expression.getComponents()) {
            RapidType type = component.getType();
            getDependencies(symbols, type);
        }
    }

    private void getDependencies(@NotNull Set<PhysicalSymbol> dependencies, @NotNull RapidType type) {
        RapidStructure structure = type.getStructure();
        if (!(type instanceof PhysicalSymbol physicalSymbol)) {
            return;
        }
        if (!(dependencies.add(physicalSymbol))) {
            return;
        }
        if (structure instanceof PhysicalRecord record) {
            List<PhysicalComponent> components = record.getComponents();
            for (PhysicalComponent component : components) {
                RapidType componentType = component.getType();
                if (componentType != null) {
                    getDependencies(dependencies, componentType);
                }
            }
        } else if (structure instanceof PhysicalAlias alias) {
            RapidType aliasType = alias.getType();
            if (aliasType != null) {
                getDependencies(dependencies, aliasType);
            }
        }
    }

    private @NotNull Block computeControlFlow(@NotNull RapidRoutine routine) {
        if (routine instanceof VirtualRoutine) {
            for (HardcodedContract value : HardcodedContract.values()) {
                if (value.getRoutine().equals(routine)) {
                    return value.getBlock();
                }
            }
        }
        Set<Block> controlFlow = new ControlFlowBuilder()
                .withModule(getModuleName(routine), moduleBuilder -> moduleBuilder.withRoutine(routine))
                .getControlFlow();
        return controlFlow.iterator().next();
    }

    private @NotNull String getModuleName(@NotNull RapidRoutine routine) {
        if (routine instanceof PhysicalRoutine physicalRoutine) {
            PhysicalModule module = PhysicalModule.getModule(physicalRoutine);
            if (module != null) {
                return Objects.requireNonNullElseGet(module.getName(), RapidSymbol::getDefaultText);
            }
            return RapidSymbol.getDefaultText();
        }
        return "";
    }

    private void computeDataFlow(@NotNull Set<RapidRoutine> stack, @NotNull ControlFlowBlock block) {
        long startTime = System.currentTimeMillis();
        Block controlFlow = block.getControlFlow();
        DataFlowAnalyzer.computeDataFlow(stack, block);
        long totalTime = System.currentTimeMillis() - startTime;
        logger.debug("Computed data flow for block: " + controlFlow.getModuleName() + ":" + controlFlow.getName() + (logger.isDebugEnabled() ? " (" + block.getSize() + " states)" : "") + " in: " + totalTime + " ms");
        ControlFlowListener.publish().onBlock(block);
    }

    public void clear() {
        cache.clear();
    }

    private static class Entry {

        private final @NotNull ControlFlowBlock block;
        private final @NotNull Map<Pointer<? extends PhysicalSymbol>, Long> dependencies;

        public Entry(@NotNull ControlFlowBlock block, @NotNull Map<PhysicalSymbol, Long> dependencies) {
            this.block = block;
            this.dependencies = new HashMap<>();
            dependencies.forEach((symbol, dependency) -> this.dependencies.put(symbol.createPointer(), dependency));
        }

        public @Nullable ControlFlowBlock getBlock() {
            for (Pointer<? extends RapidSymbol> pointer : dependencies.keySet()) {
                RapidSymbol symbol = pointer.dereference();
                if (symbol == null) {
                    return null;
                }
                if (symbol instanceof PhysicalSymbol physicalSymbol) {
                    if (physicalSymbol.getModificationCount() != dependencies.get(pointer)) {
                        return null;
                    }
                }
            }
            return block;
        }
    }

}

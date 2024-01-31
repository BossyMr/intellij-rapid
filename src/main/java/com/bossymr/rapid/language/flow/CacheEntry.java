package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.builder.ArgumentDescriptor;
import com.bossymr.rapid.language.flow.builder.ControlFlowBuilder;
import com.bossymr.rapid.language.flow.data.DataFlowAnalyzer;
import com.bossymr.rapid.language.flow.data.HardcodedContract;
import com.bossymr.rapid.language.flow.expression.Expression;
import com.bossymr.rapid.language.flow.instruction.*;
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

import java.lang.ref.SoftReference;
import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class CacheEntry {
    private static final Logger logger = Logger.getInstance(ControlFlowService.class);

    private @Nullable Result result;
    private @Nullable SoftReference<DataFlowAnalyzer> workList;

    @RequiresReadLock
    public @NotNull Block getControlFlow(@NotNull RapidRoutine routine) {
        return getControlFlowBlock(routine).getControlFlow();
    }

    @RequiresReadLock
    public @NotNull ControlFlowBlock getDataFlow(@NotNull RapidRoutine routine) {
        return getDataFlow(routine, Set.of(routine));
    }

    @RequiresReadLock
    public @NotNull ControlFlowBlock getDataFlow(@NotNull RapidRoutine routine, @NotNull Set<RapidRoutine> stack) {
        ControlFlowBlock block = getControlFlowBlock(routine);
        if(workList == null) {
            return block;
        }
        DataFlowAnalyzer analyzer = workList.get();
        if(analyzer == null) {
            analyzer = DataFlowAnalyzer.createDataFlowAnalyzer(block);
            workList = new SoftReference<>(analyzer);
        }
        computeDataFlow(analyzer, stack, block);
        workList = null;
        return block;
    }

    @RequiresReadLock
    public @Nullable ControlFlowBlock getDataFlowIfAvailable() {
        if(result == null) {
            return null;
        }
        if(workList != null) {
            return null;
        }
        ControlFlowBlock block = result.getBlock();
        if(block == null) {
            clear();
            return null;
        }
        return block;
    }

    public void clear() {
        result = null;
        workList = null;
    }

    private void computeDataFlow(@NotNull DataFlowAnalyzer analyzer, @NotNull Set<RapidRoutine> stack, @NotNull ControlFlowBlock block) {
        Block controlFlow = block.getControlFlow();
        long startTime = System.currentTimeMillis();
        analyzer.process(stack);
        long totalTime = System.currentTimeMillis() - startTime;
        if(logger.isDebugEnabled()) {
            logger.debug("Computed data flow for block: " + controlFlow.getModuleName() + ":" + controlFlow.getName() + " (" + block.getSize() + " states)" + " in: " + totalTime + " ms");
        }
        ControlFlowListener.publish().onBlock(block);
    }


    private @NotNull ControlFlowBlock getControlFlowBlock(@NotNull RapidRoutine routine) {
        if(result != null) {
            ControlFlowBlock block = result.getBlock();
            if(block != null) {
                return block;
            }
            clear();
        }
        ControlFlowBlock block = computeControlFlow(routine);
        result = new Result(block, routine);
        workList = new SoftReference<>(null);
        return block;
    }

    private @NotNull ControlFlowBlock computeControlFlow(@NotNull RapidRoutine routine) {
        if (routine instanceof VirtualRoutine) {
            for (HardcodedContract value : HardcodedContract.values()) {
                if (value.getRoutine().equals(routine)) {
                    return new ControlFlowBlock(value.getBlock(), value::getFunction);
                }
            }
        }
        Set<Block> controlFlow = new ControlFlowBuilder()
                .withModule(getModuleName(routine), moduleBuilder -> moduleBuilder.withRoutine(routine))
                .getControlFlow();
        return new ControlFlowBlock(controlFlow.iterator().next());
    }

    private @NotNull String getModuleName(@NotNull RapidRoutine routine) {
        if (routine instanceof PhysicalRoutine physicalRoutine) {
            PhysicalModule module = PhysicalModule.getModule(physicalRoutine);
            if (module == null) {
                return RapidSymbol.getDefaultText();
            }
            return Objects.requireNonNullElseGet(module.getName(), RapidSymbol::getDefaultText);
        }
        return "";
    }

    private record Result(@NotNull ControlFlowBlock block, @NotNull Map<Pointer<? extends PhysicalSymbol>, Long> dependencies) {

        private Result(@NotNull ControlFlowBlock block, @NotNull RapidRoutine routine) {
            this(block, new HashMap<>());
            if (routine instanceof PhysicalRoutine physicalRoutine) {
                Block controlFlow = block.getControlFlow();
                getDependencies(physicalRoutine, controlFlow).forEach((symbol, dependency) -> dependencies.put(symbol.createPointer(), dependency));
            }
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

        private static @NotNull Map<PhysicalSymbol, Long> getDependencies(@NotNull PhysicalRoutine routine, @NotNull Block block) {
            Map<PhysicalSymbol, Long> dependencies = new HashMap<>();
            for (Instruction instruction : block.getInstructions()) {
                Set<PhysicalSymbol> symbols = getDependencies(instruction);
                for (PhysicalSymbol symbol : symbols) {
                    dependencies.put(symbol, symbol.getModificationCount());
                }
            }
            dependencies.put(routine, routine.getModificationCount());
            return dependencies;
        }

        private static @NotNull Set<PhysicalSymbol> getDependencies(@NotNull Instruction instruction) {
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
                Map<ArgumentDescriptor, Expression> arguments = callInstruction.getArguments();
                for (Expression expression : arguments.values()) {
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

        private static void getDependencies(@NotNull Set<PhysicalSymbol> symbols, @Nullable Expression expression) {
            if (expression == null) {
                return;
            }
            for (Expression component : expression.getComponents()) {
                RapidType type = component.getType();
                getDependencies(symbols, type);
            }
        }

        private static void getDependencies(@NotNull Set<PhysicalSymbol> dependencies, @NotNull RapidType type) {
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
    }
}

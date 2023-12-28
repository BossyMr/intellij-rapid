package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.builder.ControlFlowBuilder;
import com.bossymr.rapid.language.flow.data.DataFlowAnalyzer;
import com.bossymr.rapid.language.flow.data.DataFlowFunction;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.hardcode.HardcodedContract;
import com.bossymr.rapid.language.flow.instruction.CallInstruction;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.symbol.virtual.VirtualRoutine;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.LazyInitializer;
import com.intellij.util.messages.Topic;
import com.microsoft.z3.Context;
import com.microsoft.z3.Z3Exception;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A service used to retrieve the control flow graph for a program.
 */
@Service(Service.Level.APP)
public final class ControlFlowService implements Disposable {

    private static final Logger logger = Logger.getInstance(ControlFlowService.class);
    @Topic.AppLevel
    public static Topic<ControlFlowListener> TOPIC = Topic.create("Control Flow", ControlFlowListener.class);
    /**
     * The context used to create queries for the z3 SMT-solver. Creating the context for
     * each individual query resulted in it becoming the bottleneck.
     */
    private final @NotNull LazyInitializer.LazyValue<AtomicReference<Context>> context = LazyInitializer.create(() -> new AtomicReference<>(new Context()));

    /**
     * A store containing the control flow information for hardcoded methods. This store is built lazily, that is,
     * the control flow graph for a method is not built until it is called.
     */
    private final @NotNull Map<VirtualRoutine, ControlFlowBlock> hardcoded = new HashMap<>();

    public static @NotNull ControlFlowService getInstance() {
        return ApplicationManager.getApplication().getService(ControlFlowService.class);
    }

    /**
     * Computes the control flow and data flow for the specified routine. Control flow and data flow graphs are
     * computed lazily, and will only compute the specified routine and any subsequent routines called in that routine.
     *
     * @param routine the element.
     * @return the control flow and data flow graphs for the routine.
     */
    public @NotNull ControlFlowBlock getControlFlowBlock(@NotNull RapidRoutine routine) {
        Set<RapidRoutine> stack = new HashSet<>();
        stack.add(routine);
        return getControlFlowBlock(stack, routine);
    }

    private @NotNull ControlFlowBlock getControlFlowBlock(@NotNull Set<RapidRoutine> stack, @NotNull RapidRoutine routine) {
        if (routine instanceof PhysicalRoutine physicalRoutine) {
            // Attempting to compute the data flow graph for the same routine with two different stacks will result in
            // an exception. However, it doesn't matter whether the stack is different, since it is only used to check
            // if a method calls itself - and if it does, it will always do so, and as a result it doesn't matter if the
            // call occurred in a different order.
            Set<RapidRoutine> indifferentStack = new HashSet<>(stack) {
                @Override
                public boolean equals(Object o) {
                    return o instanceof HashSet<?>;
                }
            };
            return CachedValuesManager.getProjectPsiDependentCache(physicalRoutine, (symbol) -> getDataFlow(indifferentStack, symbol));
        }
        if (routine instanceof VirtualRoutine virtualRoutine) {
            if (hardcoded.containsKey(virtualRoutine)) {
                return hardcoded.get(virtualRoutine);
            }
            ControlFlowBlock block = getDataFlow(stack, virtualRoutine);
            hardcoded.put(virtualRoutine, block);
            return block;
        }
        throw new AssertionError();
    }

    public @NotNull Set<ControlFlowBlock> getControlFlowBlock(@NotNull Project project) {
        Set<ControlFlowBlock> routines = new HashSet<>();
        PsiManager manager = PsiManager.getInstance(project);
        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(RapidFileType.getInstance(), GlobalSearchScope.projectScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            PsiFile psiFile = manager.findFile(virtualFile);
            if (psiFile instanceof RapidFile file) {
                for (PhysicalModule module : file.getModules()) {
                    for (PhysicalRoutine routine : module.getRoutines()) {
                        routines.add(getControlFlowBlock(routine));
                    }
                }
            }
        }
        routines.addAll(hardcoded.values());
        return routines;
    }

    private @NotNull ControlFlowBlock getDataFlow(@NotNull Set<RapidRoutine> stack, @NotNull RapidRoutine routine) {
        long startTime = System.currentTimeMillis();
        Block block;
        if (routine instanceof PhysicalRoutine physicalRoutine) {
            block = getPhysicalBlock(physicalRoutine);
        } else if (routine instanceof VirtualRoutine virtualRoutine) {
            block = getVirtualBlock(virtualRoutine);
        } else {
            throw new AssertionError();
        }
        Map<Instruction, DataFlowBlock> dataFlow = new HashMap<>();
        for (Instruction instruction : block.getInstructions()) {
            dataFlow.put(instruction, new DataFlowBlock(context.get(), instruction));
        }
        ControlFlowBlock controlFlowBlock = new ControlFlowBlock(block, dataFlow);
        Deque<DataFlowState> workList = new ArrayDeque<>(dataFlow.size());
        for (EntryInstruction instruction : block.getEntryInstructions()) {
            DataFlowBlock dataFlowBlock = controlFlowBlock.getDataFlow(instruction.getInstruction());
            DataFlowState dataFlowState = DataFlowState.createState(dataFlowBlock);
            workList.add(dataFlowState);
        }
        DataFlowAnalyzer analyzer = new DataFlowAnalyzer(stack, controlFlowBlock, workList);
        analyzer.process();
        long totalTime = System.currentTimeMillis() - startTime;
        String stateCount = logger.isDebugEnabled() ? " (" + getStateCount(controlFlowBlock) + " states)" : "";
        String message = "Computed data flow for block: " + routine + stateCount + " in: " + totalTime + " ms";
        System.out.println(message); // TODO: Temporary
        logger.debug(message);
        ControlFlowListener.publish().onBlock(controlFlowBlock);
        return controlFlowBlock;
    }

    private long getStateCount(@NotNull ControlFlowBlock block) {
        return block.getDataFlow().stream()
                    .map(DataFlowBlock::getStates)
                    .mapToLong(List::size)
                    .sum();
    }

    private @NotNull Block getPhysicalBlock(@NotNull RapidRoutine routine) {
        Set<Block> blocks = new ControlFlowBuilder()
                .withModule(getModuleName(routine), moduleBuilder -> moduleBuilder.withRoutine(routine))
                .getControlFlow();
        return blocks.iterator().next();
    }

    private @NotNull Block getVirtualBlock(@NotNull RapidRoutine routine) {
        for (HardcodedContract contract : HardcodedContract.values()) {
            if (contract.getRoutine().equals(routine)) {
                return contract.getBlock();
            }
        }
        return getPhysicalBlock(routine);
    }

    public @NotNull Set<DataFlowFunction.Result> getDataFlowFunction(@NotNull Set<RapidRoutine> stack, @NotNull DataFlowState callerState, @NotNull CallInstruction instruction) {
        Expression routineName = instruction.getRoutineName();
        if (!(routineName.getElement() instanceof RapidReferenceExpression referenceExpression)) {
            // It is not possible to predict which method is being called.
            return Set.of(DataFlowFunction.getDefaultOutput(null, callerState, instruction));
        }
        RapidSymbol symbol = referenceExpression.getSymbol();
        if (!(symbol instanceof RapidRoutine routine)) {
            return Set.of(DataFlowFunction.getDefaultOutput(null, callerState, instruction));
        }
        if (stack.contains(routine)) {
            return Set.of(DataFlowFunction.getDefaultOutput(null, callerState, instruction));
        }
        Set<RapidRoutine> copy = new HashSet<>(stack);
        copy.add(routine);
        ControlFlowBlock block = getControlFlowBlock(copy, routine);
        DataFlowFunction function = block.getFunction();
        return function.getOutput(callerState, instruction);
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

    @Override
    public void dispose() {
        AtomicReference<Context> reference = context.get();
        Context context = reference.get();
        if (context != null) {
            try {
                context.close();
            } catch (Z3Exception ignored) {}
        }
    }
}

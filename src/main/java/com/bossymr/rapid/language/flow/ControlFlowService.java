package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.builder.ControlFlowBuilder;
import com.bossymr.rapid.language.flow.data.DataFlowAnalyzer;
import com.bossymr.rapid.language.flow.data.DataFlowFunction;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.hardcode.HardcodedContract;
import com.bossymr.rapid.language.flow.instruction.CallInstruction;
import com.bossymr.rapid.language.flow.instruction.ExitInstruction;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.instruction.ThrowInstruction;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.symbol.ParameterType;
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
import org.jetbrains.annotations.Nullable;

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
    public @NotNull ControlFlowBlock getDataFlow(@NotNull RapidRoutine routine) {
        Set<RapidRoutine> stack = new HashSet<>();
        stack.add(routine);
        return getDataFlow(stack, routine);
    }

    public @NotNull Set<ControlFlowBlock> getDataFlow(@NotNull Project project) {
        Set<ControlFlowBlock> routines = new HashSet<>();
        PsiManager manager = PsiManager.getInstance(project);
        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(RapidFileType.getInstance(), GlobalSearchScope.projectScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            PsiFile psiFile = manager.findFile(virtualFile);
            if (psiFile instanceof RapidFile file) {
                for (PhysicalModule module : file.getModules()) {
                    for (PhysicalRoutine routine : module.getRoutines()) {
                        routines.add(getDataFlow(routine));
                    }
                }
            }
        }
        routines.addAll(hardcoded.values());
        return routines;
    }

    private @NotNull ControlFlowBlock getDataFlow(@NotNull Set<RapidRoutine> stack, @NotNull RapidRoutine routine) {
        if (routine instanceof PhysicalRoutine physicalRoutine) {
            ControlFlowBlock block = CachedValuesManager.getProjectPsiDependentCache(physicalRoutine, (symbol) -> new ControlFlowBlock(computeControlFlow(symbol)));
            if (block.getDataFlow().isEmpty()) {
                computeDataFlow(block, stack);
            }
            return block;
        }
        if (routine instanceof VirtualRoutine virtualRoutine) {
            if (hardcoded.containsKey(virtualRoutine)) {
                return hardcoded.get(virtualRoutine);
            }
            ControlFlowBlock block = new ControlFlowBlock(computeControlFlow(virtualRoutine));
            computeDataFlow(block, stack);
            hardcoded.put(virtualRoutine, block);
            return block;
        }
        throw new AssertionError();
    }

    private void computeDataFlow(@NotNull ControlFlowBlock block, @NotNull Set<RapidRoutine> stack) {
        long startTime = System.currentTimeMillis();
        Map<Instruction, DataFlowBlock> dataFlow = block.getDataFlow();
        Block controlFlow = block.getControlFlow();
        for (Instruction instruction : controlFlow.getInstructions()) {
            dataFlow.put(instruction, new DataFlowBlock(context.get(), instruction));
        }
        Deque<DataFlowState> workList = new ArrayDeque<>(dataFlow.size());
        for (EntryInstruction instruction : controlFlow.getEntryInstructions()) {
            DataFlowBlock dataFlowBlock = block.getDataFlow(instruction.getInstruction());
            DataFlowState dataFlowState = DataFlowState.createState(dataFlowBlock);
            workList.add(dataFlowState);
        }
        DataFlowAnalyzer analyzer = new DataFlowAnalyzer(stack, block, workList);
        analyzer.process();
        long totalTime = System.currentTimeMillis() - startTime;
        String routineName = controlFlow.getModuleName() + ":" + controlFlow.getName();
        String states = logger.isDebugEnabled() ? " (" + getBlockSize(block) + " states)" : "";
        logger.debug("Computed data flow for block: " + routineName + states + " in: " + totalTime + " ms");
        ControlFlowListener.publish().onBlock(block);
    }

    private @NotNull Block computeControlFlow(@NotNull RapidRoutine routine) {
        Block block;
        if (routine instanceof PhysicalRoutine physicalRoutine) {
            block = computePhysicalControlFlow(physicalRoutine);
        } else if (routine instanceof VirtualRoutine virtualRoutine) {
            block = computeVirtualControlFlow(virtualRoutine);
        } else {
            throw new AssertionError();
        }
        return block;
    }

    private long getBlockSize(@NotNull ControlFlowBlock block) {
        return block.getDataFlow().values().stream()
                    .map(DataFlowBlock::getStates)
                    .mapToLong(List::size)
                    .sum();
    }

    private @NotNull Block computePhysicalControlFlow(@NotNull RapidRoutine routine) {
        Set<Block> blocks = new ControlFlowBuilder()
                .withModule(getModuleName(routine), moduleBuilder -> moduleBuilder.withRoutine(routine))
                .getControlFlow();
        return blocks.iterator().next();
    }

    private @NotNull Block computeVirtualControlFlow(@NotNull RapidRoutine routine) {
        for (HardcodedContract contract : HardcodedContract.values()) {
            if (contract.getRoutine().equals(routine)) {
                return contract.getBlock();
            }
        }
        return computePhysicalControlFlow(routine);
    }

    public @NotNull Set<DataFlowFunction.Result> getDataFlowFunction(@NotNull Set<RapidRoutine> stack, @NotNull DataFlowState callerState, @NotNull CallInstruction instruction) {
        RapidRoutine routine = getRoutine(stack, instruction);
        if (routine == null) {
            return Set.of(DataFlowFunction.getDefaultOutput(null, callerState, instruction));
        }
        Set<RapidRoutine> copy = new HashSet<>(stack);
        copy.add(routine);
        return getDataFlow(copy, routine, callerState, instruction);
    }

    private @Nullable RapidRoutine getRoutine(@NotNull Set<RapidRoutine> stack, @NotNull CallInstruction instruction) {
        Expression routineName = instruction.getRoutineName();
        if (!(routineName.getElement() instanceof RapidReferenceExpression referenceExpression)) {
            return null;
        }
        RapidSymbol symbol = referenceExpression.getSymbol();
        if (!(symbol instanceof RapidRoutine routine)) {
            return null;
        }
        if (stack.contains(routine)) {
            return null;
        }
        return routine;
    }

    private @NotNull Set<DataFlowFunction.Result> getDataFlow(@NotNull Set<RapidRoutine> stack, @NotNull RapidRoutine routine, @NotNull DataFlowState callerState, @NotNull CallInstruction instruction) {
        if (routine instanceof VirtualRoutine) {
            ControlFlowBlock block = getDataFlow(stack, routine);
            DataFlowFunction function = block.getFunction();
            return function.getOutput(callerState, instruction);
        }
        if (!(routine instanceof PhysicalRoutine physicalRoutine)) {
            throw new IllegalArgumentException();
        }
        ControlFlowBlock block = CachedValuesManager.getProjectPsiDependentCache(physicalRoutine, (symbol) -> new ControlFlowBlock(computeControlFlow(symbol)));
        if (!(block.getDataFlow().isEmpty())) {
            DataFlowFunction function = block.getFunction();
            return function.getOutput(callerState, instruction);
        }
        if (isPureMethod(stack, block, instruction)) {
            DataFlowState successorState = DataFlowState.createSuccessorState(callerState.getBlock(), callerState);
            return Set.of(new DataFlowFunction.Result.Success(successorState, null));
        }
        computeDataFlow(block, stack);
        DataFlowFunction function = block.getFunction();
        return function.getOutput(callerState, instruction);
    }

    private boolean isPureMethod(@NotNull Set<RapidRoutine> stack, @NotNull ControlFlowBlock block, @NotNull CallInstruction instruction) {
        Block controlFlow = block.getControlFlow();
        Map<Argument, ReferenceExpression> arguments = DataFlowFunction.getArguments(controlFlow, instruction.getArguments());
        ReferenceExpression returnValue = instruction.getReturnValue();
        if (returnValue != null) {
            return false;
        }
        if (arguments.keySet().stream().anyMatch(argument -> argument.getParameterType() != ParameterType.INPUT)) {
            return false;
        }
        if (controlFlow.getInstructions().stream().anyMatch(statement -> statement instanceof ThrowInstruction || statement instanceof ExitInstruction)) {
            return false;
        }
        if (!(block.getDataFlow().isEmpty())) {
            Map<DataFlowState, DataFlowFunction.Result> results = block.getFunction().getResults();
            return results.values().stream().allMatch(result -> result instanceof DataFlowFunction.Result.Success);
        }
        for (Instruction statement : controlFlow.getInstructions()) {
            if (!(statement instanceof CallInstruction callInstruction)) {
                continue;
            }
            RapidRoutine routine = getRoutine(stack, callInstruction);
            if (routine == null) {
                continue;
            }
            Set<RapidRoutine> copy = new HashSet<>(stack);
            copy.add(routine);
            if (routine instanceof VirtualRoutine virtualRoutine) {
                ControlFlowBlock dependency = getDataFlow(virtualRoutine);
                if (!(isPureMethod(copy, dependency, callInstruction))) {
                    return false;
                }
            }
            if (routine instanceof PhysicalRoutine physicalRoutine) {
                ControlFlowBlock dependency = CachedValuesManager.getProjectPsiDependentCache(physicalRoutine, (symbol) -> new ControlFlowBlock(computeControlFlow(symbol)));
                if (!(isPureMethod(copy, dependency, callInstruction))) {
                    return false;
                }
            }
        }
        return true;
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

    public void reload() {
        hardcoded.clear();
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

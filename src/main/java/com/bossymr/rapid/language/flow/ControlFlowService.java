package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.data.DataFlowFunction;
import com.bossymr.rapid.language.flow.data.DataFlowState;
import com.bossymr.rapid.language.flow.expression.Expression;
import com.bossymr.rapid.language.flow.instruction.*;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.psi.stubs.index.RapidModuleIndex;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.symbol.virtual.VirtualRoutine;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.LazyInitializer;
import com.intellij.util.messages.Topic;
import com.microsoft.z3.Context;
import com.microsoft.z3.Z3Exception;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A service used to retrieve the control flow graph for a program.
 */
@Service(Service.Level.APP)
public final class ControlFlowService implements Disposable {

    @Topic.AppLevel
    public static final Topic<ControlFlowListener> TOPIC = Topic.create("Control Flow", ControlFlowListener.class);

    private final @NotNull LazyInitializer.LazyValue<AtomicReference<Context>> context = LazyInitializer.create(() -> new AtomicReference<>(new Context()));

    private final @NotNull ControlFlowCache cache = new ControlFlowCache();

    private final @NotNull Map<Block, Boolean> sideEffectCache = new WeakHashMap<>();

    public static @NotNull ControlFlowService getInstance() {
        return ApplicationManager.getApplication().getService(ControlFlowService.class);
    }

    /**
     * Computes the control flow and data flow for the specified routine. Control flow and data flow graphs are computed
     * lazily, and will only compute the specified routine and any subsequent routines called in that routine.
     *
     * @param routine the element.
     * @return the control flow and data flow graphs for the routine.
     */
    public @NotNull ControlFlowBlock getDataFlow(@NotNull RapidRoutine routine) {
        return cache.getDataFlow(routine);
    }

    /**
     * Computes the control flow and data flow for every routine in the specified project.
     *
     * @param project the project.
     * @return the control flow and data flow graphs for every routine in the specified project.
     */
    public @NotNull Set<ControlFlowBlock> getDataFlow(@NotNull Project project) {
        return getDataFlow(project, cache::getDataFlow, cache::getDataFlow);
    }

    public @NotNull Set<Block> getControlFlow(@NotNull Project project) {
        return getDataFlow(project, cache::getControlFlow, cache::getControlFLow);
    }

    private <T> @NotNull Set<T> getDataFlow(@NotNull Project project, @NotNull Function<PhysicalRoutine, T> consumer, @NotNull Supplier<Collection<T>> supplier) {
        Set<T> routines = new HashSet<>();
        PsiManager manager = PsiManager.getInstance(project);
        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(RapidFileType.getInstance(), GlobalSearchScope.projectScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            PsiFile psiFile = manager.findFile(virtualFile);
            if (psiFile instanceof RapidFile file) {
                for (PhysicalModule module : file.getModules()) {
                    for (PhysicalRoutine routine : module.getRoutines()) {
                        routines.add(consumer.apply(routine));
                    }
                }
            }
        }
        routines.addAll(supplier.get());
        return routines;
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
        Block controlFlow = cache.getControlFlow(routine);
        if (!(hasSideEffect(controlFlow, instruction))) {
            DataFlowState successorState = DataFlowState.createSuccessorState(callerState.getInstruction(), callerState);
            return Set.of(new DataFlowFunction.Result.Success(successorState, null));
        }
        ControlFlowBlock block = cache.getDataFlow(stack, routine);
        DataFlowFunction function = block.getFunction();
        return function.getOutput(callerState, instruction);
    }

    public boolean hasSideEffect(@NotNull Block controlFlow, @NotNull CallInstruction instruction) {
        if (!(controlFlow instanceof Block.FunctionBlock functionBlock)) {
            return false;
        }
        RapidRoutine routine = functionBlock.getElement();
        Map<Argument, Expression> arguments = DataFlowFunction.getArguments(controlFlow, instruction.getArguments());
        Expression returnValue = instruction.getReturnValue();
        if (returnValue != null) {
            return true;
        }
        if (arguments.keySet().stream().anyMatch(argument -> argument.getParameterType() != ParameterType.INPUT)) {
            return true;
        }
        return hasSideEffect(Set.of(routine), controlFlow);
    }

    private boolean hasSideEffect(@NotNull Set<RapidRoutine> stack, @NotNull Block controlFlow) {
        if (sideEffectCache.containsKey(controlFlow)) {
            return sideEffectCache.get(controlFlow);
        }
        if (controlFlow.getInstructions().stream().anyMatch(statement -> statement instanceof ThrowInstruction || statement instanceof ExitInstruction || statement instanceof TryNextInstruction || statement instanceof RetryInstruction)) {
            sideEffectCache.put(controlFlow, true);
            return true;
        }
        if (controlFlow.getElement() instanceof RapidRoutine routine) {
            ControlFlowBlock block = cache.getDataFlowIfAvailable(routine);
            if (block != null) {
                Map<DataFlowState, DataFlowFunction.Result> results = block.getFunction().getResults();
                boolean hasSideEffects = !(results.values().stream().allMatch(result -> result instanceof DataFlowFunction.Result.Success));
                sideEffectCache.put(controlFlow, hasSideEffects);
                return hasSideEffects;
            }
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
            if (!(copy.add(routine))) {
                continue;
            }
            if (routine instanceof VirtualRoutine virtualRoutine) {
                ControlFlowBlock dependency = cache.getDataFlow(Set.of(virtualRoutine), virtualRoutine);
                if (hasSideEffect(copy, dependency.getControlFlow())) {
                    sideEffectCache.put(controlFlow, true);
                    return true;
                }
            }
            if (routine instanceof PhysicalRoutine) {
                Block dependency = cache.getControlFlow(routine);
                if (hasSideEffect(copy, dependency)) {
                    sideEffectCache.put(controlFlow, true);
                    return true;
                }
            }
        }
        sideEffectCache.put(controlFlow, false);
        return false;
    }

    public void reload() {
        cache.clear();
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

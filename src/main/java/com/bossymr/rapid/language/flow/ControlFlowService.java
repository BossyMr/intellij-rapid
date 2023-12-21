package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.builder.ControlFlowBuilder;
import com.bossymr.rapid.language.flow.data.DataFlow;
import com.bossymr.rapid.language.flow.data.DataFlowAnalyzer;
import com.bossymr.rapid.language.flow.data.DataFlowFunctionMap;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.data.block.DataFlowState;
import com.bossymr.rapid.language.flow.data.hardcode.HardcodedContract;
import com.bossymr.rapid.language.flow.debug.DataFlowUsage;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.LazyInitializer;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import com.microsoft.z3.Context;
import com.microsoft.z3.Z3Exception;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * A service used to retrieve the control flow graph for a program.
 */
@Service(Service.Level.APP)
public final class ControlFlowService implements Disposable {

    private final @NotNull Logger logger = Logger.getInstance(ControlFlowService.class);

    private final @NotNull LazyInitializer.LazyValue<ControlFlow> controlFlow = LazyInitializer.create(HardcodedContract::getControlFlow);
    private final @NotNull LazyInitializer.LazyValue<AtomicReference<Context>> context = LazyInitializer.create(() -> new AtomicReference<>(new Context()));

    public static @NotNull ControlFlowService getInstance() {
        return ApplicationManager.getApplication().getService(ControlFlowService.class);
    }

    @RequiresReadLock
    public static @NotNull DataFlow calculateDataFlow(@NotNull ControlFlow controlFlow, @NotNull BiPredicate<DataFlow, DataFlowState> consumer) {
        AtomicReference<Context> reference = new AtomicReference<>();
        try (Context context = new Context()) {
            reference.set(context);
            return calculateDataFlow(reference, controlFlow, consumer);
        } finally {
            reference.set(null);
        }
    }

    @RequiresReadLock
    private static @NotNull DataFlow calculateDataFlow(@NotNull AtomicReference<Context> context, @NotNull ControlFlow controlFlow, @NotNull BiPredicate<DataFlow, DataFlowState> consumer) {
        Set<String> methods = new HashSet<>();
        Map<Instruction, DataFlowBlock> dataFlow = new HashMap<>();
        Collection<Block> blocks = controlFlow.getBlocks();
        Map<BlockDescriptor, Block.FunctionBlock> descriptorMap = blocks.stream()
                                                                        .filter(block -> block instanceof Block.FunctionBlock)
                                                                        .map(block -> (Block.FunctionBlock) block)
                                                                        .collect(Collectors.toMap(BlockDescriptor::getBlockKey, block -> block));
        Deque<DataFlowState> workList = new ArrayDeque<>();
        DataFlowFunctionMap functionMap = new DataFlowFunctionMap(descriptorMap, workList, (function, map) -> {
            if (function.moduleName().isEmpty()) {
                if (methods.add(function.name())) {
                    Application application = ApplicationManager.getApplication();
                    ControlFlow cache;
                    if (application != null) {
                        cache = ControlFlowService.getInstance().getControlFlow();
                    } else {
                        cache = HardcodedContract.getControlFlow();
                    }
                    Block functionBlock = cache.getBlock("", function.name());
                    if (functionBlock instanceof Block.FunctionBlock) {
                        controlFlow.add(function, functionBlock);
                        descriptorMap.put(function, (Block.FunctionBlock) functionBlock);
                        analyzeBlock(context, controlFlow, consumer, ((Block.FunctionBlock) functionBlock), map, dataFlow);
                    }
                }
            }
        });
        for (Block block : blocks) {
            if (!(block instanceof Block.FunctionBlock functionBlock)) {
                continue;
            }
            analyzeBlock(context, controlFlow, consumer, functionBlock, functionMap, dataFlow);
        }
        while (!(workList.isEmpty())) {
            DataFlowState state = workList.removeFirst();
            reanalyzeBlock(controlFlow, consumer, state, functionMap, dataFlow);
        }
        return createDataFlow(controlFlow, dataFlow, functionMap.getUsages());
    }

    private static void analyzeBlock(@NotNull AtomicReference<Context> context, @NotNull ControlFlow
            controlFlow, @NotNull BiPredicate<DataFlow, DataFlowState> consumer, Block.FunctionBlock
                                             functionBlock, DataFlowFunctionMap functionMap, @NotNull Map<Instruction, DataFlowBlock> dataFlow) {
        Map<Instruction, DataFlowBlock> result = DataFlowAnalyzer.analyze(context, functionBlock, functionMap, (returnValue, value) -> {
            Map<Instruction, DataFlowBlock> copyMap = new HashMap<>(Map.copyOf(dataFlow));
            copyMap.putAll(returnValue);
            return consumer.test(createDataFlow(controlFlow, copyMap, functionMap.getUsages()), value);
        });
        dataFlow.putAll(result);
    }

    private static void reanalyzeBlock(@NotNull ControlFlow
                                               controlFlow, @NotNull BiPredicate<DataFlow, DataFlowState> consumer, @NotNull DataFlowState
                                               entry, DataFlowFunctionMap functionMap, Map<Instruction, DataFlowBlock> dataFlow) {
        DataFlowAnalyzer.reanalyze(entry, functionMap, dataFlow, (returnValue, value) -> {
            Map<Instruction, DataFlowBlock> copyMap = new HashMap<>(Map.copyOf(dataFlow));
            copyMap.putAll(returnValue);
            return consumer.test(createDataFlow(controlFlow, copyMap, functionMap.getUsages()), value);
        });
    }

    private static @NotNull DataFlow createDataFlow(@NotNull ControlFlow controlFlow, @NotNull Map<Instruction, DataFlowBlock> blocks, @NotNull Map<DataFlowState, DataFlowUsage> usages) {
        return new DataFlow(controlFlow, blocks, usages);
    }

    @RequiresReadLock
    public @NotNull DataFlow getDataFlow(@NotNull PsiElement element) {
        Project project = element.getProject();
        return getDataFlow(project);
    }

    @RequiresReadLock
    public @NotNull DataFlow getDataFlow(@NotNull Project project) {
        return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            long startTime = System.currentTimeMillis();
            ControlFlow controlFlow = calculateControlFlow(project);
            DataFlow result = calculateDataFlow(context.get(), controlFlow, (dataFlow, block) -> true);
            long difference = System.currentTimeMillis() - startTime;
            logger.info("Computed data flow in: " + difference + "ms");
            return CachedValueProvider.Result.createSingleDependency(result, PsiModificationTracker.MODIFICATION_COUNT);
        });
    }

    public @NotNull ControlFlow getControlFlow() {
        return controlFlow.get();
    }

    @RequiresReadLock
    public @NotNull DataFlow getDataFlow(@NotNull Project project, @NotNull BiPredicate<DataFlow, DataFlowState> consumer) {
        return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            ControlFlow controlFlow = calculateControlFlow(project);
            return CachedValueProvider.Result.createSingleDependency(calculateDataFlow(context.get(), controlFlow, consumer), PsiModificationTracker.MODIFICATION_COUNT);
        });
    }

    /**
     * Analyzes the {@code ControlFlow} for the {@link Module} containing the specified element.
     * <p>
     * If the element does not belong to any module and the specified element is a routine, the control flow for the
     * specified routine is analyzed. Otherwise, an empty control flow instance is returned.
     *
     * @param element the element to analyze.
     * @return the control flow graph for the specified element.
     */
    @RequiresReadLock
    public @NotNull ControlFlow getControlFlow(@NotNull PsiElement element) {
        Project project = element.getProject();
        return getControlFlow(project);
    }

    /**
     * Analyzes the {@code ControlFlow} for the specified module.
     *
     * @param project the project to analyze.
     * @return the control flow graph for the specified element.
     */
    @RequiresReadLock
    public @NotNull ControlFlow getControlFlow(@NotNull Project project) {
        return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            ControlFlow controlFlow = calculateControlFlow(project);
            return CachedValueProvider.Result.createSingleDependency(controlFlow, PsiModificationTracker.MODIFICATION_COUNT);
        });
    }

    @RequiresReadLock
    private @NotNull ControlFlow calculateControlFlow(@NotNull Project project) {
        ControlFlowBuilder builder = new ControlFlowBuilder();
        PsiManager manager = PsiManager.getInstance(project);
        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(RapidFileType.getInstance(), GlobalSearchScope.projectScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            if (manager.findFile(virtualFile) instanceof RapidFile file) {
                for (PhysicalModule module : file.getModules()) {
                    builder.withModule(module);
                }
            }
        }
        return builder.getControlFlow();
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

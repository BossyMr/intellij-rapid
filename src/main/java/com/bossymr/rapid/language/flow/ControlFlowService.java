package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.data.DataFlow;
import com.bossymr.rapid.language.flow.data.DataFlowAnalyzer;
import com.bossymr.rapid.language.flow.data.DataFlowFunctionMap;
import com.bossymr.rapid.language.flow.data.block.DataFlowBlock;
import com.bossymr.rapid.language.flow.debug.DataFlowUsage;
import com.bossymr.rapid.language.flow.instruction.Instruction;
import com.bossymr.rapid.language.flow.parser.ControlFlowElementBuilder;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
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
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * A service used to retrieve the control flow graph for a program.
 */
@Service(Service.Level.APP)
public final class ControlFlowService {

    public static @NotNull ControlFlowService getInstance() {
        return ApplicationManager.getApplication().getService(ControlFlowService.class);
    }

    @RequiresReadLock
    public @NotNull DataFlow getDataFlow(@NotNull PsiElement element) {
        Project project = element.getProject();
        return getDataFlow(project);
    }

    @RequiresReadLock
    public @NotNull DataFlow getDataFlow(@NotNull Project project) {
        return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            ControlFlow controlFlow = calculateControlFlow(project);
            return CachedValueProvider.Result.createSingleDependency(getDataFlow(controlFlow, (dataFlow, block) -> true), PsiModificationTracker.MODIFICATION_COUNT);
        });
    }

    @RequiresReadLock
    public @NotNull DataFlow getDataFlow(@NotNull ControlFlow controlFlow, @NotNull BiPredicate<DataFlow, DataFlowBlock> consumer) {
        Map<Instruction, DataFlowState> dataFlow = new HashMap<>();
        Collection<Block> blocks = controlFlow.getBlocks();
        Map<BlockDescriptor, Block.FunctionBlock> descriptorMap = blocks.stream()
                .filter(block -> block instanceof Block.FunctionBlock)
                .map(block -> (Block.FunctionBlock) block)
                .collect(Collectors.toMap(BlockDescriptor::getBlockKey, block -> block));
        Deque<DataFlowBlock> workList = new ArrayDeque<>();
        DataFlowFunctionMap functionMap = new DataFlowFunctionMap(descriptorMap, workList);
        for (Block block : blocks) {
            if (!(block instanceof Block.FunctionBlock functionBlock)) {
                continue;
            }
            Map<Instruction, DataFlowState> result = DataFlowAnalyzer.analyze(functionBlock, functionMap, (returnValue, value) -> {
                Map<Instruction, DataFlowState> copyMap = new HashMap<>(Map.copyOf(dataFlow));
                copyMap.putAll(returnValue);
                return consumer.test(createDataFlow(controlFlow, copyMap, functionMap.getUsages()), value);
            });
            dataFlow.putAll(result);
        }
        for (DataFlowBlock entry : workList) {
            DataFlowAnalyzer.reanalyze(entry, functionMap, dataFlow, (returnValue, value) -> {
                Map<Instruction, DataFlowBlock> copyMap = new HashMap<>(Map.copyOf(dataFlow));
                copyMap.putAll(returnValue);
                return consumer.test(createDataFlow(controlFlow, copyMap, functionMap.getUsages()), value);
            });
        }
        return createDataFlow(controlFlow, dataFlow, functionMap.getUsages());
    }

    private @NotNull DataFlow createDataFlow(@NotNull ControlFlow controlFlow, @NotNull Map<Instruction, DataFlowState> blocks, @NotNull Map<DataFlowBlock, DataFlowUsage> usages) {
        return new DataFlow(controlFlow, blocks, usages);
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
        ControlFlowElementBuilder analyzer = new ControlFlowElementBuilder();
        PsiManager manager = PsiManager.getInstance(project);
        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(RapidFileType.getInstance(), GlobalSearchScope.projectScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            if (manager.findFile(virtualFile) instanceof RapidFile file) {
                for (PhysicalModule module : file.getModules()) {
                    analyzer.process(module);
                }
            }
        }
        return analyzer.getControlFlow();
    }

}

package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.parser.ControlFlowElementVisitor;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

/**
 * A service used to retrieve the control flow graph for a program.
 */
@Service(Service.Level.APP)
public final class ControlFlowService {

    public static @NotNull ControlFlowService getInstance() {
        return ApplicationManager.getApplication().getService(ControlFlowService.class);
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
    public @NotNull ControlFlow getControlFlow(@NotNull PsiElement element) {
        Project project = element.getProject();
        PsiFile file = element.getContainingFile();
        Module module = ModuleUtil.findModuleForFile(file);
        if (module != null) {
            return getControlFlow(module);
        }
        return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            ControlFlowElementVisitor analyzer = new ControlFlowElementVisitor(project);
            PhysicalModule physicalModule = PhysicalModule.getModule(element);
            if (physicalModule != null) {
                physicalModule.accept(analyzer);
            }
            if (!(element instanceof RapidRoutine)) {
                return CachedValueProvider.Result.createSingleDependency(new ControlFlow(project, Map.of()), PsiModificationTracker.MODIFICATION_COUNT);
            }
            element.accept(analyzer);
            return CachedValueProvider.Result.createSingleDependency(analyzer.getControlFlow(), PsiModificationTracker.MODIFICATION_COUNT);
        });
    }

    /**
     * Analyzes the {@code ControlFlow} for the specified module.
     *
     * @param module the module to analyze.
     * @return the control flow graph for the specified element.
     */
    public @NotNull ControlFlow getControlFlow(@NotNull Module module) {
        Project project = module.getProject();
        return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            ControlFlowElementVisitor analyzer = new ControlFlowElementVisitor(project);
            PsiManager manager = PsiManager.getInstance(project);
            Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(RapidFileType.getInstance(), module.getModuleContentScope());
            for (VirtualFile virtualFile : virtualFiles) {
                PsiFile file = manager.findFile(virtualFile);
                if (file != null) {
                    file.accept(analyzer);
                }
            }
            return CachedValueProvider.Result.createSingleDependency(analyzer.getControlFlow(), PsiModificationTracker.MODIFICATION_COUNT);
        });
    }
}

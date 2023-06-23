package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.data.DataFlow;
import com.bossymr.rapid.language.flow.data.DataFlowElementVisitor;
import com.bossymr.rapid.language.flow.parser.ControlFlowElementVisitor;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A service used to retrieve the control flow graph for a program.
 */
@Service(Service.Level.APP)
public class ControlFlowService {

    public static @NotNull ControlFlowService getInstance() {
        return ApplicationManager.getApplication().getService(ControlFlowService.class);
    }

    /**
     * Analyzes the {@code DataFlow} for the {@link Module} containing the specified element.
     *
     * @param element the element.
     * @return the element to analyze.
     */
    public @NotNull DataFlow getDataFlow(@NotNull PsiElement element) {
        ControlFlow controlFlow = getControlFlow(element);
        return getDataFlow(controlFlow);
    }

    /**
     * Analyzes the {@code DataFlow} for the specified module.
     *
     * @param module the module to analyze.
     * @return the data flow graph for the specified module.
     */
    public @NotNull DataFlow getDataFlow(@NotNull Module module) {
        ControlFlow controlFlow = getControlFlow(module);
        return getDataFlow(controlFlow);
    }

    private @NotNull DataFlow getDataFlow(@NotNull ControlFlow controlFlow) {
        DataFlowElementVisitor visitor = new DataFlowElementVisitor();
        controlFlow.accept(visitor);
        return visitor.getDataFlow();
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
        PsiFile file = element.getContainingFile();
        Module module = ModuleUtil.findModuleForFile(file);
        if (module != null) {
            return getControlFlow(module);
        }
        ControlFlowElementVisitor analyzer = new ControlFlowElementVisitor();
        PhysicalModule physicalModule = PhysicalModule.getModule(element);
        if (physicalModule != null) {
            physicalModule.accept(analyzer);
        }

        if (!(element instanceof RapidRoutine)) {
            return new ControlFlow();
        }
        element.accept(analyzer);
        return analyzer.getControlFlow();
    }

    /**
     * Analyzes the {@code ControlFlow} for the specified module.
     *
     * @param module the module to analyze.
     * @return the control flow graph for the specified element.
     */
    public @NotNull ControlFlow getControlFlow(@NotNull Module module) {
        ControlFlowElementVisitor analyzer = new ControlFlowElementVisitor();
        PsiManager manager = PsiManager.getInstance(module.getProject());
        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(RapidFileType.getInstance(), module.getModuleContentScope());
        for (VirtualFile virtualFile : virtualFiles) {
            PsiFile file = manager.findFile(virtualFile);
            if (file != null) {
                file.accept(analyzer);
            }
        }
        return analyzer.getControlFlow();
    }
}

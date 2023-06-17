package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.parser.ControlFlowElementVisitor;
import com.bossymr.rapid.language.symbol.RapidRoutine;
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

@Service(Service.Level.APP)
public class ControlFlowService {

    public static @NotNull ControlFlowService getInstance() {
        return ApplicationManager.getApplication().getService(ControlFlowService.class);
    }

    public @NotNull ControlFlow getControlFlow(@NotNull PsiElement element) {
        PsiFile file = element.getContainingFile();
        Module module = ModuleUtil.findModuleForFile(file);
        if (module != null) {
            return getControlFlow(module);
        }
        if (!(element instanceof RapidRoutine)) {
            return new ControlFlow();
        }
        ControlFlowElementVisitor analyzer = new ControlFlowElementVisitor();
        element.accept(analyzer);
        return analyzer.getControlFlow();
    }

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

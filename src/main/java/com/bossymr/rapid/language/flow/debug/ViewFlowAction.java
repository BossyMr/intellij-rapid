package com.bossymr.rapid.language.flow.debug;

import com.bossymr.rapid.language.flow.ControlFlow;
import com.bossymr.rapid.language.flow.parser.ControlFlowElementVisitor;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

public class ViewFlowAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        PsiElement element = e.getData(CommonDataKeys.PSI_FILE);
        if (element == null) {
            return;
        }
        PsiFile containingFile = element.getContainingFile();
        Module module = ModuleUtil.findModuleForFile(containingFile);
        if (module == null) {
            return;
        }
        ControlFlow controlFlow = ControlFlowElementVisitor.createControlFlow(module);
        String text = ControlFlowFormatVisitor.format(controlFlow);
        LightVirtualFile virtualFile = new LightVirtualFile("ControlFlow.txt", text);
        FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, virtualFile), true);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
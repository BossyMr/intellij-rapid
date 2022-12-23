package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.symbol.RapidModule;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class RapidTaskImpl implements RapidTask {

    private final String name;
    private final Set<VirtualFile> files;

    public RapidTaskImpl(@NotNull String name, @NotNull Set<VirtualFile> files) {
        this.name = name;
        this.files = files;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull Set<VirtualFile> getFiles() {
        return files;
    }

    @Override
    public @NotNull Set<RapidModule> getModules(@NotNull Project project) {
        PsiManager psiManager = PsiManager.getInstance(project);
        Set<RapidModule> modules = new HashSet<>();
        NonProjectFileWritingAccessProvider.allowWriting(files);
        for (VirtualFile file : files) {
            PsiFile psiFile = psiManager.findFile(file);
            if (psiFile instanceof RapidFile rapidFile) {
                modules.addAll(rapidFile.getModules());
            }
        }
        return modules;
    }
}

package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RapidTaskImpl implements RapidTask {

    private final String name;
    private final File directory;
    private final Set<File> files;

    public RapidTaskImpl(@NotNull String name, @NotNull File directory, @NotNull Set<File> files) {
        this.name = name;
        this.directory = directory;
        this.files = files;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull Icon getIcon() {
        return RapidIcons.TASK;
    }

    @Override
    public @NotNull File getDirectory() {
        return directory;
    }

    @Override
    public @NotNull Set<File> getFiles() {
        return files;
    }

    @Override
    public @NotNull Set<PhysicalModule> getModules(@NotNull Project project) {
        PsiManager psiManager = PsiManager.getInstance(project);
        Set<PhysicalModule> modules = new HashSet<>();
        Set<VirtualFile> virtualFiles = files.stream()
                .map(file -> LocalFileSystem.getInstance().findFileByIoFile(file))
                .collect(Collectors.toSet());
        NonProjectFileWritingAccessProvider.allowWriting(virtualFiles);
        for (VirtualFile file : virtualFiles) {
            PsiFile psiFile = psiManager.findFile(file);
            if (psiFile instanceof RapidFile rapidFile) {
                modules.addAll(rapidFile.getModules());
            }
        }
        return modules;
    }
}

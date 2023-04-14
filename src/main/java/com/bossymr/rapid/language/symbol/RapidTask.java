package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class RapidTask implements RapidSymbol {

    private final String name;
    private final File directory;
    private final Set<File> files;

    public RapidTask(@NotNull String name, @NotNull File directory, @NotNull Set<File> files) {
        this.name = name;
        this.directory = directory;
        this.files = files;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull Icon getIcon() {
        return RapidIcons.TASK;
    }

    public @NotNull File getDirectory() {
        return directory;
    }

    public @NotNull Set<File> getFiles() {
        return files;
    }

    public boolean isValid() {
        if(!(getDirectory().isDirectory())) {
            return false;
        }
        if(!(getDirectory().getName().equals(getName()))) {
            return false;
        }
        return files.stream().allMatch(File::isFile);
    }

    public @NotNull Set<PhysicalModule> getModules(@NotNull Project project) {
        PsiManager psiManager = PsiManager.getInstance(project);
        Set<PhysicalModule> modules = new HashSet<>();
        Set<VirtualFile> virtualFiles = new HashSet<>();
        for (File file : files) {
            if (!(file.exists())) {
                /*
                 * Ideally, this task should not be accessed.
                 */
                continue;
            }
            VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByNioPath(file.toPath());
            if (virtualFile == null) {
                throw new IllegalStateException("File '" + file + "' not found");
            }
            virtualFiles.add(virtualFile);
        }
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

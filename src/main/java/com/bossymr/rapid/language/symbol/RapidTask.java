package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.platform.backend.navigation.NavigationTarget;
import com.intellij.platform.backend.presentation.TargetPresentation;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class RapidTask implements RapidSymbol {

    private final @NotNull String name;
    private final @NotNull File directory;
    private final @NotNull Set<File> files;

    public RapidTask(@NotNull String name, @NotNull File directory, @NotNull Set<File> files) {
        this.name = name;
        this.directory = directory;
        this.files = files;
    }

    @Override
    public @NotNull RapidPointer<RapidTask> createPointer() {
        return () -> {
            RobotService service = RobotService.getInstance();
            RapidRobot robot = service.getRobot();
            if (robot == null) {
                return null;
            }
            return robot.getTask(name);
        };
    }

    @Override
    public @Nullable String getCanonicalName() {
        return getName();
    }

    @Override
    public @Nullable String getQualifiedName() {
        return getName();
    }

    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull TargetPresentation getTargetPresentation() {
        return TargetPresentation.builder(getName())
                .icon(RapidIcons.TASK)
                .presentation();
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
        if (!(getDirectory().isDirectory())) {
            return false;
        }
        if (!(getDirectory().getName().equals(getName()))) {
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
                continue;
            }
            VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByNioPath(file.toPath());
            if (virtualFile == null) {
                continue;
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

    @Override
    public @NotNull Collection<? extends NavigationTarget> getNavigationTargets(@NotNull Project project) {
        return Set.of();
    }
}

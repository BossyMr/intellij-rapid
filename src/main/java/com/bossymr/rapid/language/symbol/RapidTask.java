package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface RapidTask {

    @NotNull String getName();

    @NotNull VirtualFile getDirectory();

    @NotNull Set<VirtualFile> getFiles();

    @NotNull Set<PhysicalModule> getModules(@NotNull Project project);

}

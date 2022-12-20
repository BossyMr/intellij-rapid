package com.bossymr.rapid.language.symbol;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface RapidTask {

    @NotNull String getName();

    @NotNull Set<VirtualFile> getFiles();

    @NotNull Set<RapidModule> getModules(@NotNull Project project);

}

package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Set;

public interface RapidTask extends RapidSymbol {

    @NotNull String getName();

    @NotNull File getDirectory();

    @NotNull Set<File> getFiles();

    @NotNull Set<PhysicalModule> getModules(@NotNull Project project);

}

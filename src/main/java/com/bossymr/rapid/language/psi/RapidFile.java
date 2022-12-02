package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a file.
 */
public interface RapidFile extends PsiFile {

    /**
     * Returns the modules declared in this file.
     *
     * @return a list of modules.
     */
    @NotNull List<PhysicalModule> getModules();

}

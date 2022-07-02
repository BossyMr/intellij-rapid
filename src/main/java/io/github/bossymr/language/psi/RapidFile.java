package io.github.bossymr.language.psi;

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a Rapid file.
 */
public interface RapidFile extends PsiFile {

    /**
     * Returns modules declared in this file.
     *
     * @return a list of modules declared in this file.
     */
    @NotNull List<@NotNull RapidModule> getModules();
}

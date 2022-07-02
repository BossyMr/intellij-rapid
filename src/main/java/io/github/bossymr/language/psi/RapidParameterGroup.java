package io.github.bossymr.language.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a group of parameters, which are mutually exclusive.
 */
public interface RapidParameterGroup extends PsiElement {

    /**
     * Checks if this parameter group is optional.
     *
     * @return if this parameter group is optional.
     */
    boolean isOptional();

    /**
     * Returns a list of parameters included in this group.
     *
     * @return the parameters included in this group.
     */
    @NotNull List<RapidParameter> getParameters();
}

package io.github.bossymr.language.psi;

import com.intellij.util.IncorrectOperationException;
import io.github.bossymr.language.psi.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a routine declaration.
 */
public interface RapidParameter extends RapidNamedElement {

    /**
     * Returns the type of parameter that this parameter was declared as.
     *
     * @return the type of this parameter.
     */
    @NotNull ParameterAttribute getAttribute();

    /**
     * Returns the type of this parameter.
     *
     * @return the type of this parameter.
     */
    @Nullable RapidType getType();

    /**
     * Sets the return type of this parameter to the specified type.
     *
     * @param type the new return type.
     * @throws IncorrectOperationException if the parameter type could not be modified.
     */
    void setType(@NotNull RapidType type) throws IncorrectOperationException;
}

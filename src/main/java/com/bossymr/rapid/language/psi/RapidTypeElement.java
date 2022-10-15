package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a type element and contains the reference to the structure of the type. However, this element does not
 * contain any potential array dimensions.
 */
public interface RapidTypeElement extends RapidElement {

    /**
     * Returns the type of this type element. This type does not include any potential array dimensions, and the result
     * might not be accurate.
     *
     * @return the type of this type element, or {@code null} if type element is a placeholder.
     */
    @Contract(pure = true)
    @Nullable RapidType getType();

    /**
     * Returns the reference to the type of this type element.
     *
     * @return the reference, or {@code null} if the type element is a placeholder.
     */
    @Nullable RapidReferenceExpression getReferenceExpression();
}

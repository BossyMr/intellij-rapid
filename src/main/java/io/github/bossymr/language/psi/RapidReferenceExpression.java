package io.github.bossymr.language.psi;

import com.intellij.psi.PsiPolyVariantReference;
import org.jetbrains.annotations.Nullable;

public interface RapidReferenceExpression extends RapidExpression, PsiPolyVariantReference {

    /**
     * Returns the qualifier of the reference (the content before the period).
     *
     * @return the qualifier, or {@code null} if the reference is not qualified.
     */
    @Nullable RapidExpression getQualifier();

    /**
     * Returns the name of the reference.
     *
     * @return the name of the reference, or {@code null} if the reference is not complete.
     */
    @Nullable String getReferenceName();
}

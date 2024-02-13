package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.intellij.model.Pointer;
import com.intellij.platform.backend.presentation.TargetPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A {@code RapidField} represents a field.
 */
@SuppressWarnings("UnstableApiUsage")
public interface RapidField extends RapidVariable, RapidVisibleSymbol {

    /**
     * Returns the field type.
     *
     * @return the filed type.
     */
    @NotNull FieldType getFieldType();

    /**
     * Returns the initializer of this field.
     *
     * @return the initializer of this field, or {@code null} if it does not have an initializer.
     */
    @Nullable RapidExpression getInitializer();

    /**
     * Sets the initializer to the specified expression, if the expression is {@code null}, the initializer is removed.
     *
     * @param expression the new initializer.
     * @throws UnsupportedOperationException if this field is not modifiable.
     */
    default void setInitializer(@Nullable RapidExpression expression) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if this field has an initializer.
     *
     * @return if this field has an initializer.
     */
    boolean hasInitializer();

    @Override
    @NotNull Pointer<? extends RapidField> createPointer();

    @Override
    default @NotNull TargetPresentation getTargetPresentation() {
        return TargetPresentation.builder(Objects.requireNonNullElseGet(this.getName(), RapidSymbol::getDefaultText))
                .icon(switch (getFieldType()) {
                    case VARIABLE -> RapidIcons.VARIABLE;
                    case CONSTANT -> RapidIcons.CONSTANT;
                    case PERSISTENT -> RapidIcons.PERSISTENT;
                }).presentation();
    }

}

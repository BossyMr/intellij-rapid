package io.github.bossymr.language.psi;

import com.intellij.util.IncorrectOperationException;
import io.github.bossymr.language.psi.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a field.
 */
public interface RapidField extends RapidNamedElement, RapidVisibleElement {

    /**
     * Returns the type of field that this field was declared as.
     *
     * @return the type of this field.
     */
    @NotNull FieldAttribute getAttribute();

    /**
     * Checks if this element is declared as a task field, with a unique value to each individual task.
     *
     * @return if this element is a task field.
     */
    boolean isTask();

    /**
     * Returns the actual type of this field, including any array declarations.
     *
     * @return the type of this field.
     */
    @Nullable RapidType getType();

    /**
     * Sets the return type of this field to the specified type.
     *
     * @param type the new return type.
     * @throws IncorrectOperationException if the field type could not be modified.
     */
    void setType(@NotNull RapidType type) throws IncorrectOperationException;

    /**
     * Returns the initializer of this field.
     *
     * @return the initializer of this field, or {@code null} if this field is not initialized.
     */
    @Nullable RapidExpression getInitializer();

    /**
     * Sets the initializer of this field to the specified expression, or removes the initializer if the expression is
     * {@code null}.
     *
     * @param expression the new field initializer.
     * @throws IncorrectOperationException if this initializer could not be modified.
     */
    void setInitializer(@Nullable RapidExpression expression) throws IncorrectOperationException;

    /**
     * Checks if this field is initialized.
     *
     * @return if this field is initialized.
     */
    boolean hasInitializer();
}

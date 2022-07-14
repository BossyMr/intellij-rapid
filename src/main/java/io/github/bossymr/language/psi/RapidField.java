package io.github.bossymr.language.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a field (variable, persistent, or constant).
 *
 * @see RapidModule#getFields()
 * @see RapidRoutine#getFields()
 */
public interface RapidField extends RapidSymbol {

    /**
     * Checks if this field is only visible inside the module in which it was declared.
     *
     * @return if this field is marked as local.
     */
    boolean isLocal();

    /**
     * Checks if the value of this field is unique to each individual tasks.
     *
     * @return if this field is marked as task.
     */
    boolean isTask();

    /**
     * Returns the attribute with which this field was declared.
     *
     * @return the attribute of this field.
     */
    @NotNull Attribute getAttribute();

    /**
     * Returns the type of this field.
     *
     * @return the type of this field, or {@code null} if the field is incomplete.
     */
    @Nullable RapidType getType();

    /**
     * Returns the type element which declares the type of this field.
     *
     * @return the type element of this field.
     */
    @Nullable RapidTypeElement getTypeElement();

    /**
     * Returns the initializer of this field.
     *
     * @return the initializer of this field, or {@code null} if the field has no initializer.
     */
    @Nullable RapidExpression getInitializer();

    /**
     * Sets the initializer of the field, or, if the specified initializer is {@code null}, removes the initializer.
     *
     * @param initializer the new initializer, or {@code null} to remove the existing initializer.
     * @throws UnsupportedOperationException if the initializer of this field could not be modified.
     */
    default void setInitializer(@Nullable RapidExpression initializer) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if the field has an initializer.
     *
     * @return if the field has an initializer.
     */
    boolean hasInitializer();

    /**
     * Represents the attributes with which a field can be declared.
     */
    enum Attribute {
        /**
         * A variable field can be modified and assigned to any type.
         */
        VARIABLE,
        /**
         * A persistent field can be modified and assigned to any value type, and its value is persisted across
         * sessions.
         */
        PERSISTENT,
        /**
         * A constant field can be assigned to any value, and cannot be reassigned.
         */
        CONSTANT
    }
}

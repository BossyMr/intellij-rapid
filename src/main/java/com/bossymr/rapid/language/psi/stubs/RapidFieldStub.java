package com.bossymr.rapid.language.psi.stubs;

import com.intellij.psi.stubs.NamedStub;
import com.bossymr.rapid.language.psi.RapidField;
import com.bossymr.rapid.language.psi.RapidField.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a field stub.
 */
public interface RapidFieldStub extends NamedStub<RapidField> {

    /**
     * Returns the attribute of this field.
     *
     * @return the attribute of this field.
     */
    @NotNull Attribute getAttribute();

    /**
     * Checks if the field is local.
     *
     * @return if the field is local.
     */
    boolean isLocal();

    /**
     * Checks if the field is task.
     *
     * @return if the field is task.
     */
    boolean isTask();

    /**
     * Returns the type of this field.
     *
     * @return the type of this field.
     */
    @Nullable String getType();

    /**
     * Returns the initializer of this field.
     *
     * @return the initializer of this field, or {@code null} if the field has no initializer.
     */
    @Nullable String getInitializer();
}

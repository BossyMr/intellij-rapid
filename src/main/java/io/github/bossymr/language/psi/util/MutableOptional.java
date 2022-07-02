package io.github.bossymr.language.psi.util;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

/**
 * Represents an element which is optional, and which can be created and removed.
 */
public abstract class MutableOptional<T extends PsiElement> {

    /**
     * Checks if the element exists and is accessible, can be accessed using {@link #get()}.
     *
     * @return if the element is accessible.
     */
    public abstract boolean isAccessible();

    /**
     * Checks if the element of this optional can be created or removed.
     *
     * @return if the element is modifiable.
     */
    public abstract boolean isModifiable();

    /**
     * Returns the element which this optional represents.
     *
     * @return the element which this optional represents.
     * @throws NoSuchElementException if the element is not accessible.
     */
    public abstract @NotNull T get() throws NoSuchElementException;

    /**
     * Creates the specified object.
     *
     * @return the created object.
     * @throws UnsupportedOperationException if the object couldn't be created.
     */
    public abstract @NotNull T create() throws UnsupportedOperationException;

    /**
     * Removes the specified object.
     *
     * @throws UnsupportedOperationException if the object couldn't be removed.
     */
    public abstract void remove() throws UnsupportedOperationException;
}

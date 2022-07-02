package io.github.bossymr.language.psi.node;

import com.intellij.psi.tree.IElementType;
import io.github.bossymr.language.RapidLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an element type for the Rapid language.
 */
public class RapidElementType extends IElementType {

    private final boolean isLeft;

    /**
     * Creates a new element type for the Rapid language.
     *
     * @param debugName the name of the element type.
     */
    public RapidElementType(@NonNls @NotNull String debugName) {
        this(debugName, false);
    }

    /**
     * Creates a new element type for the Rapid language
     *
     * @param debugName the name of the element type.
     * @param isLeft    if an empty element should be bound to the left element.
     */
    public RapidElementType(@NonNls String debugName, boolean isLeft) {
        super(debugName, RapidLanguage.INSTANCE);
        this.isLeft = isLeft;
    }

    @Override
    public boolean isLeftBound() {
        return isLeft;
    }
}

package com.bossymr.rapid.language.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.bossymr.rapid.language.psi.RapidTokenTypes.*;

/**
 * Represents a field (variable, persistent, or constant).
 *
 * @see RapidModule#getFields()
 * @see RapidRoutine#getFields()
 */
public interface RapidField extends RapidVariable {

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
        VARIABLE(VAR_KEYWORD, "VAR"),
        /**
         * A persistent field can be modified and assigned to any value type, and its value is persisted across
         * sessions.
         */
        PERSISTENT(PERS_KEYWORD, "PERS"),
        /**
         * A constant field can be assigned to any value, and cannot be reassigned.
         */
        CONSTANT(CONST_KEYWORD, "CONST");

        public static final @NotNull TokenSet TOKEN_SET = TokenSet.create(RapidTokenTypes.VAR_KEYWORD, RapidTokenTypes.CONST_KEYWORD, RapidTokenTypes.PERS_KEYWORD);
        private static final Map<IElementType, Attribute> TYPE_TO_ATTRIBUTE;

        static {
            Map<IElementType, Attribute> MODIFIABLE_MAP = new HashMap<>();
            for (Attribute value : values()) {
                MODIFIABLE_MAP.put(value.getElementType(), value);
            }
            TYPE_TO_ATTRIBUTE = Collections.unmodifiableMap(MODIFIABLE_MAP);
        }

        private final IElementType keyword;
        private final String text;

        Attribute(IElementType keyword, String text) {
            this.keyword = keyword;
            this.text = text;
        }

        public static @Nullable Attribute getAttribute(@NotNull IElementType elementType) {
            return TYPE_TO_ATTRIBUTE.get(elementType);
        }

        public IElementType getElementType() {
            return keyword;
        }

        public @NotNull String getText() {
            return text;
        }
    }
}

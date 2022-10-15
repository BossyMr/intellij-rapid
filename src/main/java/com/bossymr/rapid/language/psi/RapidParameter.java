package com.bossymr.rapid.language.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a parameter.
 */
public interface RapidParameter extends RapidVariable {

    /**
     * Returns the attribute with which this parameter was declared.
     *
     * @return the attribute of this parameter.
     */
    @NotNull Attribute getAttribute();

    /**
     * Represents the attributes of a parameter.
     */
    enum Attribute {
        /**
         * The parameter is initialized to the value of the argument, but can be reassigned to another value and used as
         * a regular variable. This is the default attribute of a parameter.
         */
        INPUT,

        /**
         * A variable parameter can be initialized to a variable, and any changes to the parameter updates the argument
         * variable.
         */
        VARIABLE(RapidTokenTypes.VAR_KEYWORD, "VAR"),

        /**
         * A persistent parameter can be initialized to a persistent, and any changes to the parameter updates the
         * argument persistent.
         */
        PERSISTENT(RapidTokenTypes.PERS_KEYWORD, "PERS"),

        /**
         * An input-output parameter can be initialized to any field, and any changes update the argument field.
         */
        INOUT(RapidTokenTypes.INOUT_KEYWORD, "INOUT"),

        /**
         * This parameter can be initialized to any value. This attribute can only be used by predefined routines.
         */
        REFERENCE;

        public static final @NotNull TokenSet TOKEN_SET = TokenSet.create(RapidTokenTypes.VAR_KEYWORD, RapidTokenTypes.PERS_KEYWORD, RapidTokenTypes.INOUT_KEYWORD);

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

        Attribute() {
            this(null, null);
        }

        Attribute(IElementType keyword, String text) {
            this.keyword = keyword;
            this.text = text;
        }

        public static @NotNull Attribute getAttribute(@NotNull IElementType elementType) {
            Attribute attribute = TYPE_TO_ATTRIBUTE.get(elementType);
            return attribute != null ? attribute : Attribute.INPUT;
        }

        public IElementType getElementType() {
            return keyword;
        }

        public @NotNull String getText() {
            return text;
        }
    }

}

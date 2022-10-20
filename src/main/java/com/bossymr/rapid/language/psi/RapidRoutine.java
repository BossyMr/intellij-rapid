package com.bossymr.rapid.language.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a routine (function, procedure, or trap).
 */
public interface RapidRoutine extends RapidSymbol {

    /**
     * Checks if this routine is only visible inside the module in which it was declared.
     *
     * @return if the routine is marked as local.
     */
    boolean isLocal();

    /**
     * Returns the attributes of which this routine was declared.
     *
     * @return the attribute of this routine.
     */
    @NotNull Attribute getAttribute();

    /**
     * Returns the type of this routine.
     *
     * @return the type of this routine, or {@code null} if this routine is not a function.
     */
    @Nullable RapidType getType();

    /**
     * Returns the type element which declares the type of this routine.
     *
     * @return the type element of this routine, or {@code null} if this routine is not a function.
     */
    @Nullable RapidTypeElement getTypeElement();

    /**
     * Returns the parameter groups of this routine.
     *
     * @return the parameter groups of this routine, or {@code null} if this routine is a trap.
     */
    default @Nullable List<RapidParameterGroup> getParameters() {
        return getParameterList() != null ? getParameterList().getParameters() : null;
    }

    /**
     * Returns the parameter list of this routine.
     *
     * @return the parameter list of this routine, or {@code null} if this routine is a trap.
     */
    @Nullable RapidParameterList getParameterList();

    /**
     * Returns the fields declared in this routine.
     *
     * @return the fields declared in this routine.
     */
    @NotNull List<RapidField> getFields();

    /**
     * Returns the statements contained in this routine.
     *
     * @return the statements contained in this routine.
     */
    default @NotNull List<RapidStatement> getStatements() {
        return getStatementList().getStatements();
    }

    /**
     * Returns the body statement list of this routine.
     *
     * @return the body statement list of this routine.
     */
    @NotNull RapidStatementList getStatementList();

    @Nullable RapidStatementList getBackwardStatementList();

    @Nullable RapidStatementList getErrorStatementList();

    @Nullable RapidStatementList getUndoStatementList();


    /**
     * Represents the attributes with which a routine can be declared.
     */
    enum Attribute {
        /**
         * A function executes a set of instructions and returns a value.
         */
        FUNCTION(RapidTokenTypes.FUNC_KEYWORD, "FUNC"),

        /**
         * A procedure executes a set of instructions.
         */
        PROCEDURE(RapidTokenTypes.PROC_KEYWORD, "PROC"),

        /**
         * A trap is associated with an interrupt.
         */
        TRAP(RapidTokenTypes.TRAP_KEYWORD, "TRAP");

        public static final @NotNull TokenSet TOKEN_SET = TokenSet.create(RapidTokenTypes.PROC_KEYWORD, RapidTokenTypes.FUNC_KEYWORD, RapidTokenTypes.TRAP_KEYWORD);
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

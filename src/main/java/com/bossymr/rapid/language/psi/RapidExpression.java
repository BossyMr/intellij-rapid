package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a Rapid expression.
 */
public interface RapidExpression extends RapidElement {

    /**
     * Returns the type of the expression.
     *
     * @return the expression type, or {@code null} if the type could not be determined.
     */
    @Contract(pure = true)
    @Nullable RapidType getType();

    boolean isConstant();

    default boolean isLiteral() {
        return false;
    }

    boolean isConditional();

}

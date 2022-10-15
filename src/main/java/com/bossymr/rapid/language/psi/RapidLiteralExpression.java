package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a literal expression (boolean, integer, double, or string).
 */
public interface RapidLiteralExpression extends RapidExpression {

    /**
     * Returns the value of this literal expression.
     * <p>
     * The value will be of type:
     * <li>Long: for integer, hex, octal, binary literals.</li>
     * <li>Double: for double literals.</li>
     * <li>String: for string literals.</li>
     * <li>Boolean: for boolean literals.</li>
     *
     * @return the value of this literal.
     */
    @Nullable Object getValue();

}

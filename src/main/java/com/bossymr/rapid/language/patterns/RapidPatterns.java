package com.bossymr.rapid.language.patterns;

import com.bossymr.rapid.language.psi.RapidExpression;
import com.intellij.patterns.StandardPatterns;
import org.jetbrains.annotations.NotNull;

public class RapidPatterns extends StandardPatterns {

    public static @NotNull RapidExpressionPattern.Capture<RapidExpression> expression() {
        return new RapidExpressionPattern.Capture<>(RapidExpression.class);
    }

    public static @NotNull RapidBinaryExpressionPattern binaryExpression() {
        return new RapidBinaryExpressionPattern();
    }

}

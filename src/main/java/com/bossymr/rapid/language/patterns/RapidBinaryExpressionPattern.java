package com.bossymr.rapid.language.patterns;

import com.bossymr.rapid.language.psi.RapidBinaryExpression;
import com.intellij.patterns.PatternCondition;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class RapidBinaryExpressionPattern extends RapidExpressionPattern<RapidBinaryExpression, RapidBinaryExpressionPattern> {

    public RapidBinaryExpressionPattern() {
        super(RapidBinaryExpression.class);
    }

    public RapidBinaryExpressionPattern left(@NotNull RapidExpressionPattern<?, ?> pattern) {
        return with(new PatternCondition<>("left") {
            @Override
            public boolean accepts(@NotNull RapidBinaryExpression expression, ProcessingContext context) {
                return pattern.accepts(expression.getLeft(), context);
            }
        });
    }

    public RapidBinaryExpressionPattern right(@NotNull RapidExpressionPattern<?, ?> pattern) {
        return with(new PatternCondition<>("right") {
            @Override
            public boolean accepts(@NotNull RapidBinaryExpression expression, ProcessingContext context) {
                return pattern.accepts(expression.getRight(), context);
            }
        });
    }
}

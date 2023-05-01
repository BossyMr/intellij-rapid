package com.bossymr.rapid.language.patterns;

import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.symbol.RapidType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.InitialPatternCondition;
import com.intellij.patterns.PatternCondition;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class RapidExpressionPattern<T extends RapidExpression, S extends RapidExpressionPattern<T, S>> extends RapidElementPattern<T, S> {

    public RapidExpressionPattern(@NotNull Class<T> type) {
        super(type);
    }

    public RapidExpressionPattern(@NotNull InitialPatternCondition<T> condition) {
        super(condition);
    }

    public @NotNull S ofType(@NotNull ElementPattern<RapidType> pattern) {
        return with(new PatternCondition<T>("ofType") {
            @Override
            public boolean accepts(@NotNull T t, ProcessingContext context) {
                return pattern.accepts(t.getType(), context);
            }
        });
    }

    public static class Capture<T extends RapidExpression> extends RapidExpressionPattern<T, Capture<T>> {

        public Capture(@NotNull Class<T> type) {
            super(type);
        }

        public Capture(@NotNull InitialPatternCondition<T> condition) {
            super(condition);
        }
    }
}

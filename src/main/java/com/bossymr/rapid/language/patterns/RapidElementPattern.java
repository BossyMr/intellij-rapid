package com.bossymr.rapid.language.patterns;

import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.patterns.*;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class RapidElementPattern<T extends PsiElement, S extends RapidElementPattern<T, S>> extends PsiElementPattern<T, S> {

    public RapidElementPattern(@NotNull Class<T> type) {
        super(type);
    }

    public RapidElementPattern(@NotNull InitialPatternCondition<T> condition) {
        super(condition);
    }

    public @NotNull S nameIdentifierOf(@NotNull Class<? extends PhysicalSymbol> type) {
        return nameIdentifierOf(StandardPatterns.instanceOf(type));
    }

    public @NotNull S nameIdentifierOf(@NotNull ElementPattern<? extends PhysicalSymbol> pattern) {
        return with(new PatternCondition<T>("nameIdentifierOf") {
            @Override
            public boolean accepts(@NotNull T object, ProcessingContext context) {
                PsiElement parent = object.getParent();
                if (!(parent instanceof PhysicalSymbol symbol)) {
                    return false;
                }
                PsiElement nameIdentifier = symbol.getNameIdentifier();
                if (object != nameIdentifier) {
                    return false;
                }
                return pattern.accepts(parent, context);
            }
        });
    }

    public static class Capture<T extends PsiElement> extends RapidElementPattern<T, Capture<T>> {

        public Capture(@NotNull Class<T> type) {
            super(type);
        }

        public Capture(@NotNull InitialPatternCondition<T> condition) {
            super(condition);
        }
    }
}

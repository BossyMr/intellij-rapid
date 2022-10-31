package com.bossymr.rapid.language.symbol;

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ResolveUtil {

    private ResolveUtil() {}

    public static @NotNull ResolveResult[] getSymbols(@NotNull PsiElement context, @NotNull String name) {
        return new ResolveResult[0];
    }

    public static @Nullable RapidStructure getStructure(@NotNull PsiElement context, @NotNull String name) {
        RapidSymbol symbol = getSymbol(context, name);
        if (symbol instanceof RapidStructure structure) {
            return structure;
        } else {
            return null;
        }
    }

    public static @Nullable RapidSymbol getSymbol(@NotNull PsiElement context, @NotNull String name) {
        ResolveResult[] results = getSymbols(context, name);
        PsiElement element = results.length == 1 ? results[0].getElement() : null;
        return element instanceof RapidSymbol symbol ? symbol : null;
    }

}

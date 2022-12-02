package com.bossymr.rapid.language.symbol.resolve;

import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.symbol.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class ResolveUtil {

    private ResolveUtil() {}

    public static @Nullable RapidStructure getStructure(@NotNull PsiElement element, @NotNull String name) {
        Set<RapidSymbol> results = getSymbols(element, name);
        if (results.size() == 0) return null;
        RapidSymbol symbol = results.iterator().next();
        return symbol instanceof RapidStructure structure ? structure : null;
    }

    public static @NotNull Set<RapidSymbol> getSymbols(@NotNull RapidReferenceExpression expression, @NotNull String name) {
        RapidExpression qualifier = expression.getQualifier();
        if (qualifier != null) {
            RapidType dataType = qualifier.getType();
            if (dataType != null) {
                RapidStructure structure = dataType.getTargetStructure();
                if (structure instanceof RapidRecord record) {
                    Set<RapidSymbol> symbols = new HashSet<>();
                    for (RapidComponent component : record.getComponents()) {
                        if (Objects.equals(component.getName(), name)) {
                            symbols.add(component);
                        }
                    }
                    return symbols;
                }
            }
            return Collections.emptySet();
        }
        return getSymbols((PsiElement) expression, name);
    }


    public static @NotNull Set<RapidSymbol> getSymbols(@NotNull PsiElement element, @NotNull String name) {
        return getSymbols(element, new RapidScopeProcessor(element, name));
    }

    public static @NotNull Set<RapidSymbol> getSymbols(@NotNull PsiElement element, @NotNull RapidScopeProcessor processor) {
        element.accept(new RapidScopeVisitor(processor));
        return processor.getSymbols();
    }

}

package com.bossymr.rapid.language.symbol.resolve;

import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.symbol.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ResolveUtil {

    private ResolveUtil() {
        throw new UnsupportedOperationException();
    }

    public static @Nullable RapidStructure getStructure(@NotNull PsiElement element, @NotNull String name) {
        List<RapidSymbol> results = getSymbols(element, name);
        if (results.size() == 0) return null;
        RapidSymbol symbol = results.iterator().next();
        return symbol instanceof RapidStructure structure ? structure : null;
    }

    public static @NotNull List<RapidSymbol> getSymbols(@NotNull RapidReferenceExpression expression, @NotNull String name) {
        RapidExpression qualifier = expression.getQualifier();
        if (qualifier != null) {
            RapidType dataType = qualifier.getType();
            if (dataType != null) {
                RapidStructure structure = dataType.getTargetStructure();
                if (structure instanceof RapidRecord record) {
                    List<RapidSymbol> symbols = new ArrayList<>();
                    for (RapidComponent component : record.getComponents()) {
                        if (name.equalsIgnoreCase(component.getName())) {
                            symbols.add(component);
                        }
                    }
                    return symbols;
                }
            }
            return Collections.emptyList();
        }
        return getSymbols((PsiElement) expression, name);
    }


    public static @NotNull List<RapidSymbol> getSymbols(@NotNull PsiElement element, @NotNull String name) {
        return getSymbols(element, new ResolveScopeProcessor(element, name));
    }

    public static @NotNull List<RapidSymbol> getSymbols(@NotNull PsiElement element, @NotNull ResolveScopeProcessor processor) {
        ResolveScopeVisitor visitor = new ResolveScopeVisitor(element, processor);
        visitor.process();
        return processor.getSymbol();
    }

}

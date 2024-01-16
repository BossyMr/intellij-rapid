package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.symbol.physical.*;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.modcommand.ActionContext;
import com.intellij.modcommand.ModPsiUpdater;
import com.intellij.modcommand.PsiUpdateModCommandAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class SymbolArrangementOrderFix extends PsiUpdateModCommandAction<PhysicalModule> {

    public SymbolArrangementOrderFix(@NotNull PhysicalModule element) {
        super(element);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.symbol.arrangement");
    }

    @Override
    protected void invoke(@NotNull ActionContext context, @NotNull PhysicalModule element, @NotNull ModPsiUpdater updater) {
        List<PhysicalVisibleSymbol> symbols = element.getSymbols();
        List<PhysicalVisibleSymbol> reordered = new ArrayList<>(symbols);
        reordered.sort(Comparator.comparing(this::getSymbolOrder));
        for (int i = 0; i < symbols.size(); i++) {
            symbols.get(i).replace(reordered.get(i));
        }
    }

    private int getSymbolOrder(@NotNull PhysicalVisibleSymbol symbol) {
        if (symbol instanceof PhysicalStructure) {
            return 1;
        }
        if (symbol instanceof PhysicalField) {
            return 2;
        }
        if (symbol instanceof PhysicalRoutine) {
            return 3;
        }
        throw new IllegalArgumentException("Unexpected symbol: " + symbol);
    }
}

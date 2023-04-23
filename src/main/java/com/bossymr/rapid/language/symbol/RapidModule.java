package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import com.intellij.navigation.TargetPresentation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public interface RapidModule extends RapidSymbol {

    @NotNull List<ModuleType> getAttributes();

    boolean hasAttribute(@NotNull ModuleType moduleType);

    default void setAttribute(@NotNull ModuleType moduleType, boolean setAttribute) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @NotNull List<RapidVisibleSymbol> getSymbols();

    @NotNull List<RapidStructure> getStructures();

    @NotNull List<RapidField> getFields();

    @NotNull List<RapidRoutine> getRoutines();

    @Override
    default @NotNull TargetPresentation getTargetPresentation() {
        return TargetPresentation.builder(getPresentableName())
                .icon(hasAttribute(ModuleType.SYSTEM_MODULE) ? RapidIcons.SYSTEM_MODULE : RapidIcons.MODULE)
                .presentation();
    }

}

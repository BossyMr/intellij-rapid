package com.bossymr.rapid.ide.structure;

import com.bossymr.rapid.language.symbol.physical.PhysicalAlias;
import com.intellij.ide.structureView.StructureViewTreeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class RapidAliasTreeElement extends RapidSymbolTreeElement<PhysicalAlias> {

    public RapidAliasTreeElement(@NotNull PhysicalAlias alias) {
        super(alias);
    }

    @Override
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        return Collections.emptyList();
    }

    @Override
    public @Nullable String getPresentableText() {
        return getValue() != null ? getValue().getName() : "";
    }
}

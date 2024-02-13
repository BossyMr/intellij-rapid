package com.bossymr.rapid.ide.structure;

import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.intellij.ide.structureView.StructureViewTreeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class RapidFieldTreeElement extends RapidSymbolTreeElement<PhysicalField> {

    public RapidFieldTreeElement(@NotNull PhysicalField alias) {
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

package com.bossymr.rapid.ide.structure;

import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.physical.PhysicalComponent;
import com.bossymr.rapid.language.symbol.physical.PhysicalRecord;
import com.intellij.ide.structureView.StructureViewTreeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RapidRecordTreeElement extends RapidSymbolTreeElement<PhysicalRecord> {

    public RapidRecordTreeElement(@NotNull PhysicalRecord alias) {
        super(alias);
    }

    @Override
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        PhysicalRecord element = getValue();
        if (element == null) return Collections.emptyList();
        List<StructureViewTreeElement> elements = new ArrayList<>();
        for (RapidComponent component : element.getComponents()) {
            elements.add(new RapidComponentTreeElement((PhysicalComponent) component));
        }
        return elements;
    }

    @Override
    public @Nullable String getPresentableText() {
        return getValue() != null ? getValue().getName() : "";
    }
}

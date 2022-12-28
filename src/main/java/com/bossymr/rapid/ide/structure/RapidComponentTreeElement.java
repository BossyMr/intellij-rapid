package com.bossymr.rapid.ide.structure;

import com.bossymr.rapid.language.symbol.physical.PhysicalComponent;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class RapidComponentTreeElement extends PsiTreeElementBase<PhysicalComponent> {

    public RapidComponentTreeElement(@Nullable PhysicalComponent alias) {
        super(alias);
    }

    @Override
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        return Collections.emptyList();
    }

    @Override
    public @Nullable String getPresentableText() {
        PhysicalComponent element = getElement();
        return element != null ? element.getName() : "";
    }
}

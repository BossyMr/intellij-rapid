package com.bossymr.rapid.ide.structure;

import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RapidFileTreeElement extends PsiTreeElementBase<RapidFile> {

    public RapidFileTreeElement(@Nullable RapidFile file) {
        super(file);
    }

    @Override
    public @Nullable String getPresentableText() {
        RapidFile element = getElement();
        return element != null ? element.getName() : "";
    }

    @Override
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        RapidFile element = getElement();
        if (element == null) return Collections.emptyList();
        List<StructureViewTreeElement> elements = new ArrayList<>();
        for (PhysicalModule module : element.getModules()) {
            elements.add(new RapidModuleTreeElement(module));
        }
        return elements;
    }
}
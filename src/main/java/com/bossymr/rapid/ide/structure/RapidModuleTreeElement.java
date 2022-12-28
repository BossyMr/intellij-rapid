package com.bossymr.rapid.ide.structure;

import com.bossymr.rapid.language.symbol.RapidField;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.physical.*;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RapidModuleTreeElement extends PsiTreeElementBase<PhysicalModule> {

    public RapidModuleTreeElement(@Nullable PhysicalModule module) {
        super(module);
    }

    @Override
    public @Nullable String getPresentableText() {
        PhysicalModule element = getElement();
        return element != null ? element.getName() : "";
    }

    @Override
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        PhysicalModule element = getElement();
        if (element == null) return Collections.emptyList();
        List<StructureViewTreeElement> elements = new ArrayList<>();
        for (RapidStructure structure : element.getStructures()) {
            if (structure instanceof PhysicalAlias alias) {
                elements.add(new RapidAliasTreeElement(alias));
            }
            if (structure instanceof PhysicalRecord record) {
                elements.add(new RapidRecordTreeElement(record));
            }
        }
        for (RapidField field : element.getFields()) {
            elements.add(new RapidFieldTreeElement((PhysicalField) field));
        }
        for (RapidRoutine routine : element.getRoutines()) {
            elements.add(new RapidRoutineTreeElement((PhysicalRoutine) routine));
        }
        return elements;
    }
}

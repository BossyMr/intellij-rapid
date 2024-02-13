package com.bossymr.rapid.ide.structure;

import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.symbol.RapidModule;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.symbol.physical.*;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidFileTreeModel extends TextEditorBasedStructureViewModel implements StructureViewModel.ElementInfoProvider {

    public RapidFileTreeModel(@NotNull RapidFile file, @Nullable Editor editor) {
        super(editor, file);
    }


    @Override
    public Sorter @NotNull [] getSorters() {
        return new Sorter[]{
                Sorter.ALPHA_SORTER
        };
    }

    @Override
    public boolean shouldEnterElement(Object element) {
        return element instanceof RapidModule || element instanceof RapidRecord;
    }

    @Override
    public @NotNull StructureViewTreeElement getRoot() {
        return new RapidFileTreeElement((RapidFile) getPsiFile());
    }

    @Override
    protected Class<?> @NotNull [] getSuitableClasses() {
        return new Class[]{PhysicalModule.class, PhysicalAlias.class, PhysicalRecord.class, PhysicalComponent.class,
                PhysicalField.class, PhysicalRoutine.class};
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        Object value = element.getValue();
        return value instanceof RapidModule || value instanceof RapidRecord || value instanceof RapidFile;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        return false;
    }
}

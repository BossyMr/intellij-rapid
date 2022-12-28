package com.bossymr.rapid.ide.structure;

import com.bossymr.rapid.language.psi.RapidFile;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidStructureViewBuilderFactory implements PsiStructureViewFactory {
    @Override
    public @Nullable StructureViewBuilder getStructureViewBuilder(@NotNull PsiFile psiFile) {
        if (!(psiFile instanceof RapidFile)) return null;
        return new TreeBasedStructureViewBuilder() {
            @Override
            public @NotNull StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                return new RapidFileTreeModel((RapidFile) psiFile, editor);
            }

            @Override
            public boolean isRootNodeShown() {
                return false;
            }
        };
    }
}

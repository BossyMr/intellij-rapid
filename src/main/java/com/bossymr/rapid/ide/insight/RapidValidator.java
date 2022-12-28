package com.bossymr.rapid.ide.insight;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.insight.quickfix.MoveModuleToSeparateFileFix;
import com.bossymr.rapid.ide.insight.quickfix.RenameElementFix;
import com.bossymr.rapid.ide.insight.quickfix.RenameFileFix;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class RapidValidator {

    private final AnnotationHolder annotationHolder;

    public RapidValidator(@NotNull AnnotationHolder annotationHolder) {
        this.annotationHolder = annotationHolder;
    }

    /**
     * Checks if the specified module is declared in a file with the same name as the module.
     *
     * @param module the module to validate.
     */
    public void checkModuleFile(@NotNull PhysicalModule module) {
        String name = module.getName();
        PsiElement nameIdentifier = module.getNameIdentifier();
        if (name == null || nameIdentifier == null) return;
        if (!(module.getContainingFile() instanceof RapidFile containingFile)) return;
        VirtualFile virtualFile = containingFile.getVirtualFile();
        if (name.equals(virtualFile.getNameWithoutExtension())) return;
        int startOffset = module.getNode().getStartOffset();
        int endOffset = nameIdentifier.getNode().getStartOffset() + nameIdentifier.getTextLength();
        TextRange textRange = new TextRange(startOffset, endOffset);
        String message = RapidBundle.message("annotation.module.multiple", name);
        AnnotationBuilder annotationBuilder = annotationHolder.newAnnotation(HighlightSeverity.ERROR, message).range(textRange);
        if (containingFile.getModules().size() > 1) {
            annotationBuilder = annotationBuilder.withFix(new MoveModuleToSeparateFileFix(module));
        }
        boolean existsModuleForFile = containingFile.getModules().stream()
                .anyMatch(otherModule -> !(otherModule.getManager().areElementsEquivalent(otherModule, module)) && virtualFile.getNameWithoutExtension().equals(otherModule.getName()));
        if (!(existsModuleForFile)) {
            // Rename the file to match the module name.
            annotationBuilder = annotationBuilder.withFix(new RenameFileFix(module.getContainingFile(), name + RapidFileType.DEFAULT_DOT_EXTENSION));
            // Rename the module to match the file name.
            annotationBuilder = annotationBuilder.withFix(new RenameElementFix(module, virtualFile.getNameWithoutExtension()));
        }
        annotationBuilder.create();
    }
}
package com.bossymr.rapid.ide.insight;

import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class RapidAnnotatorVisitor extends RapidElementVisitor {

    private final RapidValidator validator;

    public RapidAnnotatorVisitor(@NotNull AnnotationHolder annotationHolder) {
        this.validator = new RapidValidator(annotationHolder);
    }

    @Override
    public void visitFile(@NotNull PsiFile file) {
        if (file instanceof RapidFile) {
            for (PhysicalModule module : ((RapidFile) file).getModules()) {
                validator.checkModuleFile(module);
            }
        }
        super.visitFile(file);
    }
}
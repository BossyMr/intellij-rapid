package com.bossymr.rapid.ide.highlight;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class RapidHighlighterAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
        RapidHighlighterVisitor annotatorVisitor = new RapidHighlighterVisitor(annotationHolder);
        element.accept(annotatorVisitor);
    }
}

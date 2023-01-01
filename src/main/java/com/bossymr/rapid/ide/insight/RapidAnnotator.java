package com.bossymr.rapid.ide.insight;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class RapidAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
        RapidAnnotatorVisitor annotatorVisitor = new RapidAnnotatorVisitor(annotationHolder);
        element.accept(annotatorVisitor);
    }
}
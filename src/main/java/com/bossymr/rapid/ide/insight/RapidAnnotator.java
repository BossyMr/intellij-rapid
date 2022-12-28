package com.bossymr.rapid.ide.insight;

import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class RapidAnnotator extends RapidElementVisitor implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
        RapidAnnotatorVisitor annotatorVisitor = new RapidAnnotatorVisitor(annotationHolder);
        element.accept(annotatorVisitor);
    }
}
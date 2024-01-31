package com.bossymr.rapid.ide.editor.insight.surrounder;

import com.bossymr.rapid.language.psi.RapidStatementList;
import com.intellij.lang.surroundWith.SurroundDescriptor;
import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RapidStatementsSurroundDescriptor implements SurroundDescriptor {

    private static final Surrounder[] SURROUNDERS = {
            new RapidWithIfSurrounder()
    };

    @Override
    public PsiElement @NotNull [] getElementsToSurround(PsiFile file, int startOffset, int endOffset) {
        return getStatementsInOffset(file, startOffset, endOffset);
    }

    public static @NotNull PsiElement[] getStatementsInOffset(@NotNull PsiElement element) {
        TextRange range = element.getTextRange();
        return getStatementsInOffset(element.getContainingFile(), range.getStartOffset(), range.getEndOffset());
    }

    public static @NotNull PsiElement[] getStatementsInOffset(@NotNull PsiFile file, int startOffset, int endOffset) {
        PsiElement startElement = file.findElementAt(startOffset);
        if (startElement instanceof PsiWhiteSpace) {
            startElement = file.findElementAt(startElement.getTextRange().getEndOffset());
        }
        PsiElement endElement = file.findElementAt(endOffset - 1);
        if (endElement instanceof PsiWhiteSpace) {
            endElement = file.findElementAt(endElement.getTextRange().getStartOffset() - 1);
        }
        if (startElement == null || endElement == null) {
            return PsiElement.EMPTY_ARRAY;
        }
        PsiElement parent = PsiTreeUtil.findCommonParent(startElement, endElement);
        if (parent == null) {
            return PsiElement.EMPTY_ARRAY;
        }
        RapidStatementList statementList = PsiTreeUtil.getParentOfType(parent, RapidStatementList.class, false);
        if (statementList == null) {
            return PsiElement.EMPTY_ARRAY;
        }
        PsiElement startChild = getParentInside(startElement, statementList);
        if (startElement.getTextRange().getStartOffset() != startChild.getTextRange().getStartOffset()) {
            return PsiElement.EMPTY_ARRAY;
        }
        PsiElement stopChild = getParentInside(endElement, statementList);
        if (endElement.getTextRange().getEndOffset() != stopChild.getTextRange().getEndOffset()) {
            return PsiElement.EMPTY_ARRAY;
        }
        List<PsiElement> elements = new ArrayList<>();
        PsiElement element = startChild;
        while (element != null) {
            elements.add(element);
            if (element.equals(stopChild)) {
                break;
            }
            element = element.getNextSibling();
        }
        return elements.toArray(PsiElement.EMPTY_ARRAY);
    }

    private static @NotNull PsiElement getParentInside(@NotNull PsiElement element, @NotNull PsiElement parent) {
        if (element.equals(parent)) {
            return element;
        }
        while (!(parent.equals(element.getParent()))) {
            element = element.getParent();
        }
        return element;
    }

    @Override
    public Surrounder @NotNull [] getSurrounders() {
        return SURROUNDERS;
    }

    @Override
    public boolean isExclusive() {
        return false;
    }
}

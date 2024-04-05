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
            new RapidWithIfSurrounder(),
            new RapidWithIfElseSurrounder(),
            new RapidWithForSurrounder(),
            new RapidWithWhileSurrounder()
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
            // The selection begins with whitespace, select the first non-whitespace element.
            startElement = file.findElementAt(startElement.getTextRange().getEndOffset());
        }
        PsiElement endElement = file.findElementAt(endOffset - 1);
        if (endElement instanceof PsiWhiteSpace) {
            // The selection ends with whitespace, select the last non-whitespace element.
            endElement = file.findElementAt(endElement.getTextRange().getStartOffset() - 1);
        }
        if (startElement == null || endElement == null) {
            // The selection is invalid
            return PsiElement.EMPTY_ARRAY;
        }
        /*
         * If the selection spans multiple elements, select their common parent.
         * We can't surround elements at different levels.
         */
        PsiElement parent = PsiTreeUtil.findCommonParent(startElement, endElement);
        if (parent == null) {
            return PsiElement.EMPTY_ARRAY;
        }
        RapidStatementList statementList = PsiTreeUtil.getParentOfType(parent, RapidStatementList.class, false);
        if (statementList == null) {
            // The selection isn't inside a code block.
            return PsiElement.EMPTY_ARRAY;
        }
        // Find the child to the code block which is the parent of the starting element.
        // Code Block -> Statement -> [Identifier] => Code Block -> [Statement]
        // We always want to select an entire statement
        PsiElement startChild = getParentInside(startElement, statementList);
        PsiElement stopChild = getParentInside(endElement, statementList);
        if(startOffset != endOffset) {
            // This is a selection. We want to only provide this surrounder if the
            // selection already contains the entire element.
            if (startElement.getTextRange().getStartOffset() != startChild.getTextRange().getStartOffset()) {
                return PsiElement.EMPTY_ARRAY;
            }
            if (endElement.getTextRange().getEndOffset() != stopChild.getTextRange().getEndOffset()) {
                return PsiElement.EMPTY_ARRAY;
            }
        }
        // Find all elements between the starting and stopping element.
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

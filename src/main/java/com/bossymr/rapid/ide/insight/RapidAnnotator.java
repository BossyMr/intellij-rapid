package com.bossymr.rapid.ide.insight;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.symbol.resolve.ResolveUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.ui.ColorUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class RapidAnnotator extends RapidElementVisitor implements Annotator {

    private AnnotationHolder annotationHolder;

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
        this.annotationHolder = annotationHolder;
        try {
            element.accept(this);
        } finally {
            this.annotationHolder = null;
        }
    }

    @Override
    public void visitSymbol(@NotNull PhysicalSymbol symbol) {
        String name = symbol.getName();
        PsiElement identifier = symbol.getNameIdentifier();
        if (identifier != null && name != null) {
            Set<RapidSymbol> results = ResolveUtil.getSymbols(symbol, name);
            if (results.size() > 1) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.declaration.duplicate.symbol", name))
                        .range(identifier)
                        .create();
            }
        }
        super.visitSymbol(symbol);
    }

    @Override
    public void visitReferenceExpression(@NotNull RapidReferenceExpression expression) {
        RapidSymbol element = expression.resolve();
        PsiElement identifier = expression.getIdentifier();
        if (identifier != null && element == null) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.reference.cannot.resolve.symbol", expression.getCanonicalText()))
                    .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                    .range(identifier)
                    .create();
        }
        super.visitReferenceExpression(expression);
    }

    @Override
    public void visitLiteralExpression(@NotNull RapidLiteralExpression expression) {
        RapidType type = expression.getType();
        if (type != null && RapidType.NUMBER.isAssignable(type)) {
            if (expression.getValue() == null) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.literal.numerical.size"))
                        .range(expression)
                        .create();
            }
            // TODO: 2022-10-20 Check max value of numeric literal
        }
        if (type != null && RapidType.STRING.isAssignable(type)) {
            Object value = expression.getValue();
            if (value instanceof String string && string.length() > 80) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.string.length"))
                        .range(expression)
                        .create();
            }
        }
        super.visitLiteralExpression(expression);
    }

    @Override
    public void visitArray(@NotNull RapidArray array) {
        for (RapidExpression expression : array.getDimensions()) {
            RapidType type = expression.getType();
            if (type != null && !RapidType.NUMBER.isAssignable(type)) {
                createIncompatibleType(RapidType.NUMBER, type, expression.getTextRange());
            }
        }
        super.visitArray(array);
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {
        if (element.getNode().getElementType() == RapidTokenTypes.IDENTIFIER) {
            if (element.getText().length() > 32) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.identifier.length"))
                        .range(element)
                        .create();
            }
        }
        if (element.getNode().getElementType() == RapidTokenTypes.COMMENT) {
            // TODO: 2022-10-20 According to specification, comment placement is restricted
        }
        super.visitElement(element);
    }

    private void createIncompatibleType(@NotNull RapidType left, @Nullable RapidType right, @NotNull TextRange range) {
        String leftPresentableText = left.getPresentableText();
        String rightPresentableText = right != null ? right.getPresentableText() : "null";
        String description = RapidBundle.message("annotation.description.incompatible.types", leftPresentableText, rightPresentableText);
        String tooltip = RapidBundle.message("annotation.tooltip.incompatible.types", leftPresentableText, rightPresentableText, "#" + ColorUtil.toHex(UIUtil.getContextHelpForeground()));
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, description)
                .tooltip(tooltip)
                .range(range)
                .create();
    }
}

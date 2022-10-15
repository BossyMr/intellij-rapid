package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.RapidElementFactory;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.PsiImplUtil;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class RapidElementUtil {

    private RapidElementUtil() {
    }

    public static void setName(@NotNull PsiElement element, @NotNull String name) throws UnsupportedOperationException {
        PsiElement identifier = RapidElementFactory.getInstance(element.getProject()).createIdentifier(name);
        element.replace(identifier);
    }

    public static void ensureSurroundingParenthesis(@NotNull CompositeElement node) {
        if (node.findChildByType(RapidTokenTypes.LPARENTH) != null) return;
        node.addLeaf(RapidTokenTypes.LPARENTH, "(", node.getFirstChildNode());
        node.addLeaf(RapidTokenTypes.RPARENTH, ")", null);
    }

    public static void deleteSeparatingComma(@NotNull CompositeElement element, @NotNull ASTNode child) {
        deleteSeparatingComma(element, child, RapidTokenTypes.COMMA);
    }

    public static void deleteSeparatingComma(@NotNull CompositeElement element, @NotNull ASTNode child, @NotNull IElementType separator) {
        ASTNode next = PsiImplUtil.skipWhitespaceAndComments(child.getTreeNext());
        if (next != null && next.getElementType() == separator) {
            element.deleteChildInternal(next);
        } else {
            ASTNode previous = PsiImplUtil.skipWhitespaceAndCommentsBack(child.getTreePrev());
            if (previous != null && previous.getElementType() == separator) {
                element.deleteChildInternal(previous);
            }
        }
    }


    public static void addSeparatingComma(@NotNull CompositeElement element, @NotNull ASTNode child, @NotNull TokenSet tokenSet) {
        addSeparatingComma(element, child, RapidTokenTypes.COMMA, tokenSet);
    }

    public static void addSeparatingComma(@NotNull CompositeElement element, @NotNull ASTNode child, @NotNull IElementType separator, @NotNull TokenSet tokenSet) {
        addSeparatingComma(element, child, separator, tokenSet, true);
        addSeparatingComma(element, child, separator, tokenSet, false);
    }

    private static void addSeparatingComma(@NotNull CompositeElement element, @NotNull ASTNode node, @NotNull IElementType separator, @NotNull TokenSet tokenSet, boolean forward) {
        Function<ASTNode, ASTNode> next = (current) -> forward ? current.getTreeNext() : current.getTreePrev();
        for (ASTNode child = next.apply(node); child != null && child.getElementType() != separator; child = next.apply(node)) {
            if (tokenSet.contains(child.getElementType())) {
                element.addLeaf(separator, separator.toString(), (forward ? node : child).getTreeNext());
            }
        }
    }
}

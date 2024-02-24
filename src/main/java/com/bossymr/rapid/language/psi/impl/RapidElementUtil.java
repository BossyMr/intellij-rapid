package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.RapidElementFactory;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class RapidElementUtil {

    private RapidElementUtil() {
        throw new AssertionError();
    }

    public static void setName(@NotNull PsiElement element, @NotNull String name) throws UnsupportedOperationException {
        PsiElement identifier = RapidElementFactory.getInstance(element.getProject()).createIdentifier(name);
        element.replace(identifier);
    }

    /**
     * Checks if the specified element is surrounded by parenthesis, otherwise, it automatically surrounds the specified
     * element with both a left and right parenthesis.
     *
     * @param element the element.
     * @throws IllegalArgumentException if the node of the specified element does not implement {@link CompositeElement}
     * (if the node is a leaf).
     */
    public static void ensureSurroundingParenthesis(@NotNull PsiElement element) {
        ASTNode node = element.getNode();
        if (!(node instanceof CompositeElement compositeElement)) {
            throw new IllegalArgumentException();
        }
        ensureSurroundingParenthesis(compositeElement);
    }


    /**
     * Checks if the specified element is surrounded by parenthesis, otherwise, it automatically surrounds the specified
     * element with both a left and right parenthesis.
     *
     * @param node the element.
     */
    public static void ensureSurroundingParenthesis(@NotNull CompositeElement node) {
        if (node.findChildByType(RapidTokenTypes.LPARENTH) != null) return;
        node.addLeaf(RapidTokenTypes.LPARENTH, "(", node.getFirstChildNode());
        node.addLeaf(RapidTokenTypes.RPARENTH, ")", null);
    }

    /**
     * Deletes the comma separating the specified child.
     *
     * @param element the list element.
     * @param child the child element.
     * @throws IllegalArgumentException if the node of the specified element does not implement {@link CompositeElement}
     * (if the node is a leaf).
     */
    public static void deleteSeparatingComma(@NotNull PsiElement element, @NotNull ASTNode child) {
        ASTNode node = element.getNode();
        if (!(node instanceof CompositeElement compositeElement)) {
            throw new IllegalArgumentException();
        }
        deleteSeparatingComma(compositeElement, child);
    }

    /**
     * Deletes the comma separating the specified child.
     *
     * @param element the list element.
     * @param child the child element.
     */
    public static void deleteSeparatingComma(@NotNull CompositeElement element, @NotNull ASTNode child) {
        deleteSeparator(element, child, RapidTokenTypes.COMMA);
    }

    /**
     * Deletes the separator of the specified type separating the specified child.
     *
     * @param element the list element.
     * @param child the child element.
     * @param separator the separator type.
     * @throws IllegalArgumentException if the node of the specified element does not implement {@link CompositeElement}
     * (if the node is a leaf).
     */
    public static void deleteSeparator(@NotNull PsiElement element, @NotNull ASTNode child, @NotNull IElementType separator) {
        ASTNode node = element.getNode();
        if (!(node instanceof CompositeElement compositeElement)) {
            throw new IllegalArgumentException();
        }
        deleteSeparator(compositeElement, child, separator);
    }

    /**
     * Deletes a separator of the specified type separating the specified child.
     *
     * @param element the list element.
     * @param child the child element.
     * @param separator the separator type.
     */
    public static void deleteSeparator(@NotNull CompositeElement element, @NotNull ASTNode child, @NotNull IElementType separator) {
        PsiElement next = PsiTreeUtil.skipWhitespacesAndCommentsForward(child.getPsi());
        ASTNode nextNode = next != null ? next.getNode() : null;
        if (next != null && nextNode.getElementType() == separator) {
            element.deleteChildInternal(nextNode);
        } else {
            ASTNode previousNode = child.getTreePrev();
            if (previousNode != null) {
                PsiElement previous = PsiTreeUtil.skipWhitespacesAndCommentsBackward(previousNode.getPsi());
                if (previous != null) {
                    ASTNode node = previous.getNode();
                    if (node.getElementType() == separator) {
                        element.deleteChildInternal(node);
                    }
                }
            }
        }
    }

    /**
     * Adds a comma separating the specified node.
     *
     * @param element the list element.
     * @param child the child element.
     * @param tokenSet a TokenSet containing the element type of elements in the list.
     * @throws IllegalArgumentException if the node of the specified element does not implement {@link CompositeElement}
     * (if the node is a leaf).
     */
    public static void addSeparatingComma(@NotNull PsiElement element, @NotNull ASTNode child, @NotNull TokenSet tokenSet) {
        ASTNode node = element.getNode();
        if (!(node instanceof CompositeElement compositeElement)) {
            throw new IllegalArgumentException();
        }
        addSeparatingComma(compositeElement, child, tokenSet);
    }

    /**
     * Adds a separator of the specified type, separating the specified node.
     *
     * @param element the list element.
     * @param child the child element.
     * @param separator the separator type.
     * @param tokenSet a TokenSet containing the element type of elements in the list.
     * @throws IllegalArgumentException if the node of the specified element does not implement {@link CompositeElement}
     * (if the node is a leaf).
     */
    public static void addSeparatingComma(@NotNull PsiElement element, @NotNull ASTNode child, @NotNull IElementType separator, @NotNull TokenSet tokenSet) {
        ASTNode node = element.getNode();
        if (!(node instanceof CompositeElement compositeElement)) {
            throw new IllegalArgumentException();
        }
        addSeparatingComma(compositeElement, child, separator, tokenSet);
    }


    /**
     * Adds a separating comma separating the specified node.
     *
     * @param element the list element.
     * @param child the child element.
     * @param tokenSet a TokenSet containing the element type of elements in the list.
     */
    public static void addSeparatingComma(@NotNull CompositeElement element, @NotNull ASTNode child, @NotNull TokenSet tokenSet) {
        addSeparatingComma(element, child, RapidTokenTypes.COMMA, tokenSet);
    }

    /**
     * Adds a separator of the specified type, separating the specified node.
     *
     * @param element the list element.
     * @param child the child element.
     * @param separator the separator type.
     * @param tokenSet a TokenSet containing the element type of elements in the list.
     */
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

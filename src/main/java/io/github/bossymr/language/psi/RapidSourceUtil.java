package io.github.bossymr.language.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.PsiImplUtil;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class RapidSourceUtil {

    private RapidSourceUtil() {}

    public static void

    public static void addSurroundingParenthesis(@NotNull ASTNode node) {
        if(node.getFirstChildNode().getElementType() != RapidTokenTypes.LPARENTH) {
            node.addLeaf(RapidTokenTypes.LPARENTH, "(", node.getFirstChildNode());
            node.addLeaf(RapidTokenTypes.RPARENTH, ")", null);
        }
    }

    public static void addSeparatingComma(@NotNull ASTNode node, @NotNull ASTNode child, @NotNull TokenSet elements) {
        processChildren(node, child, elements, true);
        processChildren(node, child, elements, false);
    }

    private static void processChildren(@NotNull ASTNode node, @NotNull ASTNode child, @NotNull TokenSet elements, boolean forward) {
        for (ASTNode target = (forward ? child.getTreeNext() : child.getTreePrev()); target != null && target.getElementType() != RapidTokenTypes.COMMA; target = (forward ? child.getTreeNext() : child.getTreePrev())) {
            if(elements.contains(target.getElementType())) {
                node.addLeaf(RapidTokenTypes.COMMA, ",", (forward ? child.getTreeNext() : target.getTreeNext()));
            }
        }
    }

    public static void deleteSeparatingComma(@NotNull ASTNode node, @NotNull ASTNode child) {
        ASTNode next = PsiImplUtil.skipWhitespaceAndComments(child.getTreeNext());
        if(next != null && next.getElementType() == RapidTokenTypes.COMMA) {
            node.removeChild(next);
        } else {
            ASTNode previous = PsiImplUtil.skipWhitespaceAndCommentsBack(child.getTreePrev());
            if(previous != null && previous.getElementType() == RapidTokenTypes.COMMA) {
                node.removeChild(previous);
            }
        }
    }

}

package io.github.bossymr.language.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.PsiImplUtil;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.tree.TokenSet;
import io.github.bossymr.language.psi.RapidElementFactory;
import io.github.bossymr.language.psi.RapidSymbol;
import io.github.bossymr.language.psi.RapidTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class RapidElementUtil {

    private RapidElementUtil() {
    }

    public static <T extends RapidStubElement<? extends NamedStub<?>> & RapidSymbol> @Nullable String getName(@NotNull T element) {
        final NamedStub<?> stub = element.getGreenStub();
        if (stub != null) {
            return stub.getName();
        } else {
            PsiElement identifier = element.getNameIdentifier();
            return identifier != null ? identifier.getText() : null;
        }
    }

    public static @NotNull PsiElement setName(@NotNull PsiElement element, @NotNull String name) throws UnsupportedOperationException {
        PsiElement identifier = RapidElementFactory.getInstance(element.getProject()).createIdentifier(name);
        return element.replace(identifier);
    }

    public static void ensureSurroundingParenthesis(@NotNull CompositeElement node) {
        if (node.findChildByType(RapidTokenTypes.LPARENTH) != null) return;
        node.addLeaf(RapidTokenTypes.LPARENTH, "(", node.getFirstChildNode());
        node.addLeaf(RapidTokenTypes.RPARENTH, ")", null);
    }

    public static void deleteSeparatingComma(@NotNull CompositeElement element, @NotNull ASTNode child) {
        ASTNode next = PsiImplUtil.skipWhitespaceAndComments(child.getTreeNext());
        if (next != null && next.getElementType() == RapidTokenTypes.COMMA) {
            element.deleteChildInternal(next);
        } else {
            ASTNode previous = PsiImplUtil.skipWhitespaceAndCommentsBack(child.getTreePrev());
            if (previous != null && previous.getElementType() == RapidTokenTypes.COMMA) {
                element.deleteChildInternal(previous);
            }
        }
    }

    public static void addSeparatingComma(@NotNull CompositeElement element, @NotNull ASTNode child, @NotNull TokenSet tokenSet) {
        addSeparatingComma(element, child, tokenSet, true);
        addSeparatingComma(element, child, tokenSet, false);
    }

    private static void addSeparatingComma(@NotNull CompositeElement element, @NotNull ASTNode node, @NotNull TokenSet tokenSet, @NotNull boolean forward) {
        Function<ASTNode, ASTNode> next = (current) -> forward ? current.getTreeNext() : current.getTreePrev();
        for (ASTNode child = next.apply(node); child != null && child.getElementType() != RapidTokenTypes.COMMA; child = next.apply(node)) {
            if (tokenSet.contains(child.getElementType())) {
                element.addLeaf(RapidTokenTypes.COMMA, ",", (forward ? node : child).getTreeNext());
            }
        }
    }
}

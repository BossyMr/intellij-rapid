package com.bossymr.rapid.language.psi.stubs.node;

import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidAttributeListElement extends CompositeElement {

    public RapidAttributeListElement(IElementType elementType) {
        super(elementType);
    }

    @Override
    public TreeElement addInternal(TreeElement first, ASTNode last, @Nullable ASTNode anchor, @Nullable Boolean before) {
        if (anchor == null) {
            if (before == null || before) {
                anchor = findChildByType(RapidTokenTypes.RPARENTH);
                before = true;
            } else {
                anchor = findChildByType(RapidTokenTypes.LPARENTH);
                before = false;
            }
        }
        TreeElement treeElement = super.addInternal(first, last, anchor, before);
        if (first == last && RapidTokenTypes.ATTRIBUTES.contains(first.getElementType())) {
            RapidElementUtil.addSeparatingComma(this, first, RapidTokenTypes.ATTRIBUTES);
        }
        return treeElement;
    }

    @Override
    public void deleteChildInternal(@NotNull ASTNode child) {
        if (RapidTokenTypes.ATTRIBUTES.contains(child.getElementType())) {
            RapidElementUtil.deleteSeparatingComma(this, child);
            RapidElementUtil.ensureSurroundingParenthesis(this);
        }
        super.deleteChildInternal(child);
    }
}

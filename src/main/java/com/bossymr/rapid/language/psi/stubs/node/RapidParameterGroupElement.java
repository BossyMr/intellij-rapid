package com.bossymr.rapid.language.psi.stubs.node;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidParameterGroupElement extends CompositeElement {

    public RapidParameterGroupElement() {
        super(RapidElementTypes.PARAMETER_GROUP);
    }

    @Override
    public TreeElement addInternal(TreeElement first, ASTNode last, @Nullable ASTNode anchor, @Nullable Boolean before) {
        TreeElement treeElement = super.addInternal(first, last, anchor, before);
        if (first == last && first.getElementType().equals(RapidElementTypes.PARAMETER)) {
            RapidElementUtil.addSeparatingComma(this, first, RapidTokenTypes.LINE, TokenSet.create(RapidElementTypes.PARAMETER));
        }
        return treeElement;
    }

    @Override
    public void deleteChildInternal(@NotNull ASTNode child) {
        if (child.getElementType().equals(RapidElementTypes.PARAMETER)) {
            RapidElementUtil.deleteSeparatingComma(this, child, RapidTokenTypes.LINE);
        }
        super.deleteChildInternal(child);
    }
}

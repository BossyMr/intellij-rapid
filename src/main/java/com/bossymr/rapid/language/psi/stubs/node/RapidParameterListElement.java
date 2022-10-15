package com.bossymr.rapid.language.psi.stubs.node;

import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidParameterListElement extends CompositeElement {

    private final TokenSet tokenSet;

    public RapidParameterListElement(IElementType elementType, TokenSet tokenSet) {
        super(elementType);
        this.tokenSet = tokenSet;
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
        if (first == last && tokenSet.contains(first.getElementType())) {
            RapidElementUtil.addSeparatingComma(this, first, tokenSet);
        }
        return treeElement;
    }

    @Override
    public void deleteChildInternal(@NotNull ASTNode child) {
        if (tokenSet.contains(child.getElementType())) {
            RapidElementUtil.deleteSeparatingComma(this, child);
            RapidElementUtil.ensureSurroundingParenthesis(this);
        }
        super.deleteChildInternal(child);
    }
}

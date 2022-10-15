package com.bossymr.rapid.language.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class RapidArgumentListElement extends CompositePsiElement {

    private final TokenSet tokenSet;

    protected RapidArgumentListElement(IElementType type, TokenSet tokenSet) {
        super(type);
        this.tokenSet = tokenSet;
    }

    @Override
    public TreeElement addInternal(TreeElement first, ASTNode last, @Nullable ASTNode anchor, @Nullable Boolean before) {
        if (anchor == null) {
            if (before == null || before) {
                anchor = findChildByType(RapidTokenTypes.RPARENTH);
                if (anchor == null) {
                    anchor = getLastChildNode();
                    before = false;
                } else {
                    before = true;
                }
            } else {
                anchor = findChildByType(RapidTokenTypes.LPARENTH);
                if(anchor == null) {
                    anchor = getFirstChildNode();
                    before = true;
                } else {
                    before = false;
                }
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
        }
        super.deleteChildInternal(child);
    }

}

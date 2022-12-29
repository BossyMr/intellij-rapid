package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class RapidElementListImpl extends RapidElementImpl {

    private final TokenSet tokenSet;

    protected RapidElementListImpl(@NotNull ASTNode node, @NotNull TokenSet tokenSet) {
        super(node);
        this.tokenSet = tokenSet;
    }

    @Override
    public @Nullable ASTNode addInternal(@Nullable ASTNode first, @Nullable ASTNode last, @Nullable ASTNode anchor, @Nullable Boolean before) {
        if (!(first instanceof TreeElement)) return null;
        if (anchor == null) {
            if (before == null || before) {
                anchor = findChildByType(RapidTokenTypes.RPARENTH);
                if (anchor == null) {
                    anchor = getNode().getLastChildNode();
                    before = false;
                } else {
                    before = true;
                }
            } else {
                anchor = findChildByType(RapidTokenTypes.LPARENTH);
                if (anchor == null) {
                    anchor = getNode().getFirstChildNode();
                    before = true;
                } else {
                    before = false;
                }
            }
        }
        ASTNode node = super.addInternal(first, last, anchor, before);
        if (first == last && RapidElementTypes.ARGUMENTS.contains(first.getElementType())) {
            RapidElementUtil.addSeparatingComma(this, first, RapidElementTypes.ARGUMENTS);
        }
        return node;
    }

    @Override
    public void deleteChildInternal(@NotNull ASTNode child) {
        if (tokenSet.contains(child.getElementType())) {
            RapidElementUtil.deleteSeparatingComma(this, child);
        }
        super.deleteChildInternal(child);
    }

}

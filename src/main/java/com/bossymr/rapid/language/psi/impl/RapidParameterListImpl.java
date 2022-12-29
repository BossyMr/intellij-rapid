package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.stubs.RapidParameterListStub;
import com.bossymr.rapid.language.symbol.RapidParameterGroup;
import com.bossymr.rapid.language.symbol.physical.PhysicalParameterGroup;
import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RapidParameterListImpl extends RapidStubElement<RapidParameterListStub> implements RapidParameterList {
    public RapidParameterListImpl(@NotNull RapidParameterListStub stub) {
        super(stub, RapidStubElementTypes.PARAMETER_LIST);
    }

    public RapidParameterListImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitParameterList(this);
    }

    @Override
    public @NotNull List<RapidParameterGroup> getParameters() {
        return List.of(getStubOrPsiChildren(RapidStubElementTypes.PARAMETER_GROUP, new PhysicalParameterGroup[0]));
    }

    @Override
    public @Nullable ASTNode addInternal(@Nullable ASTNode first, @Nullable ASTNode last, @Nullable ASTNode anchor, @Nullable Boolean before) {
        if (!(first instanceof TreeElement)) return null;
        if (anchor == null) {
            if (before == null || before) {
                anchor = findChildByType(RapidTokenTypes.RPARENTH);
                before = true;
            } else {
                anchor = findChildByType(RapidTokenTypes.LPARENTH);
                before = false;
            }
        }
        ASTNode treeElement = super.addInternal(first, last, anchor, before);
        if (first == last && TokenSet.create(RapidElementTypes.PARAMETER_GROUP).contains(first.getElementType())) {
            RapidElementUtil.addSeparatingComma(this, first, TokenSet.create(RapidElementTypes.PARAMETER_GROUP));
        }
        return treeElement;
    }

    @Override
    public void deleteChildInternal(@NotNull ASTNode child) {
        if (TokenSet.create(RapidElementTypes.PARAMETER_GROUP).contains(child.getElementType())) {
            RapidElementUtil.deleteSeparatingComma(this, child);
            RapidElementUtil.ensureSurroundingParenthesis(this);
        }
        super.deleteChildInternal(child);
    }

    @Override
    public String toString() {
        return "RapidParameterList:" + getText();
    }
}

package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.bossymr.rapid.language.psi.impl.RapidStubElement;
import com.bossymr.rapid.language.psi.stubs.RapidParameterGroupStub;
import com.bossymr.rapid.language.symbol.RapidParameterGroup;
import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PhysicalParameterGroup extends RapidStubElement<RapidParameterGroupStub> implements RapidParameterGroup, RapidElement {

    public PhysicalParameterGroup(@NotNull RapidParameterGroupStub stub) {
        super(stub, RapidStubElementTypes.PARAMETER_GROUP);
    }

    public PhysicalParameterGroup(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitParameterGroup(this);
    }

    @Override
    public boolean isOptional() {
        RapidParameterGroupStub stub = getGreenStub();
        if (stub != null) {
            return stub.isOptional();
        } else {
            return findChildByType(RapidTokenTypes.BACKSLASH) != null;
        }
    }

    @Override
    public @NotNull List<PhysicalParameter> getParameters() {
        return List.of(getStubOrPsiChildren(RapidStubElementTypes.PARAMETER, new PhysicalParameter[0]));
    }

    @Override
    public @Nullable ASTNode addInternal(@Nullable ASTNode first, @Nullable ASTNode last, @Nullable ASTNode anchor, @Nullable Boolean before) {
        if (!(first instanceof TreeElement)) return null;
        ASTNode treeElement = super.addInternal(first, last, anchor, before);
        if (first == last && first.getElementType().equals(RapidElementTypes.PARAMETER)) {
            RapidElementUtil.addSeparatingComma(this, first, RapidTokenTypes.LINE, TokenSet.create(RapidElementTypes.PARAMETER));
        }
        return treeElement;

    }

    @Override
    public void deleteChildInternal(@NotNull ASTNode child) {
        if (child.getElementType().equals(RapidElementTypes.PARAMETER)) {
            RapidElementUtil.deleteSeparator(this, child, RapidTokenTypes.LINE);
        }
        super.deleteChildInternal(child);
    }

    @Override
    public String toString() {
        return "PhysicalParameterGroup";
    }
}

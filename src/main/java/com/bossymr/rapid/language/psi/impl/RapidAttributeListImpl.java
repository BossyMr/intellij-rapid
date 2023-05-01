package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.RapidAttributeList;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.stubs.RapidAttributeListStub;
import com.bossymr.rapid.language.symbol.ModuleType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RapidAttributeListImpl extends RapidStubElement<RapidAttributeListStub> implements RapidAttributeList {

    public RapidAttributeListImpl(@NotNull RapidAttributeListStub stub) {
        super(stub, RapidStubElementTypes.ATTRIBUTE_LIST);
    }

    public RapidAttributeListImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitAttributeList(this);
    }

    @Override
    public @NotNull List<ModuleType> getAttributes() {
        RapidAttributeListStub stub = getGreenStub();
        if (stub != null) {
            return stub.getAttributes();
        } else {
            return ModuleType.getAttributes(this);
        }
    }

    @Override
    public boolean hasAttribute(@NotNull ModuleType moduleType) {
        RapidAttributeListStub stub = getGreenStub();
        if (stub != null) {
            return stub.hasAttribute(moduleType);
        } else {
            IElementType elementType = moduleType.getElementType();
            return findChildByType(elementType) != null;
        }
    }

    @Override
    public void setAttribute(@NotNull ModuleType moduleType, boolean value) throws UnsupportedOperationException {
        if (value) {
            // TODO: 2023-05-01 Insert attribute in the correct order.
            LeafElement element = Factory.createSingleLeafElement(moduleType.getElementType(), moduleType.getText(), null, getManager());
            addInternal(element, element, null, null);
        } else {
            PsiElement child = findChildByType(moduleType.getElementType());
            if (child != null) {
                child.delete();
            }
        }
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

    @Override
    public String toString() {
        return "RapidAttributeList:" + getText();
    }
}

package com.bossymr.rapid.language.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.TokenSet;
import com.bossymr.rapid.language.psi.ModuleAttribute;
import com.bossymr.rapid.language.psi.RapidAttributeList;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.stubs.RapidAttributeListStub;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

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
    public @NotNull Set<ModuleAttribute> getAttributes() {
        RapidAttributeListStub stub = getGreenStub();
        if (stub != null) {
            return stub.getAttributes();
        } else {
            Set<ModuleAttribute> attributes = EnumSet.noneOf(ModuleAttribute.class);
            for (ASTNode child : getNode().getChildren(TokenSet.ANY)) {
                ModuleAttribute attribute = ModuleAttribute.getAttribute(child.getElementType());
                if (attribute != null) {
                    attributes.add(attribute);
                }
            }
            return attributes;
        }
    }

    @Override
    public boolean hasAttribute(ModuleAttribute attribute) {
        RapidAttributeListStub stub = getGreenStub();
        if (stub != null) {
            return stub.hasAttribute(attribute);
        } else {
            return findChildByType(attribute.getElementType()) != null;
        }
    }

    @Override
    public void setAttribute(ModuleAttribute attribute, boolean value) throws UnsupportedOperationException {
        CompositeElement node = (CompositeElement) getNode();
        if (value) {
            LeafElement element = Factory.createSingleLeafElement(attribute.getElementType(), attribute.getText(), null, getManager());
            node.addInternal(element, element, null, null);
        } else {
            PsiElement child = findChildByType(attribute.getElementType());
            if (child != null) {
                child.delete();
            }
        }
    }

    @Override
    public String toString() {
        return "RapidAttributeList:" + getText();
    }
}

package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.stubs.RapidParameterStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidParameterImpl extends RapidStubElement<RapidParameterStub> implements RapidParameter {

    public RapidParameterImpl(@NotNull RapidParameterStub stub) {
        super(stub, RapidStubElementTypes.PARAMETER);
    }

    public RapidParameterImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitParameter(this);
    }

    @Override
    public @NotNull Attribute getAttribute() {
        RapidParameterStub stub = getGreenStub();
        if (stub != null) {
            return stub.getAttribute();
        } else {
            PsiElement node = findChildByType(Attribute.TOKEN_SET);
            return node != null ? Attribute.getAttribute(node.getNode().getElementType()) : Attribute.INPUT;
        }
    }

    @Override
    public @Nullable RapidType getType() {
        RapidType type = getTypeElement() != null ? getTypeElement().getType() : null;
        if (type != null) {
            int dimensions = findChildrenByType(RapidTokenTypes.ASTERISK).size();
            if (dimensions > 0) {
                type = type.createArrayType(dimensions);
            }
        }
        return type;
    }

    @Override
    public @Nullable RapidTypeElement getTypeElement() {
        return findChildByType(RapidElementTypes.TYPE_ELEMENT);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return findChildByType(RapidTokenTypes.IDENTIFIER);
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        RapidElementUtil.setName(Objects.requireNonNull(getNameIdentifier()), name);
        return this;
    }

    @Override
    public String getName() {
        final NamedStub<?> stub = getGreenStub();
        if (stub != null) {
            return stub.getName();
        } else {
            PsiElement identifier = getNameIdentifier();
            return identifier != null ? identifier.getText() : null;
        }
    }

    @Override
    public String toString() {
        return "RapidParameter:" + getName();
    }
}

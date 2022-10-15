package com.bossymr.rapid.language.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.util.IncorrectOperationException;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.stubs.RapidFieldStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidFieldImpl extends RapidStubElement<RapidFieldStub> implements RapidField {

    public RapidFieldImpl(@NotNull RapidFieldStub stub) {
        super(stub, RapidStubElementTypes.FIELD);
    }

    public RapidFieldImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitField(this);
    }

    @Override
    public boolean isLocal() {
        RapidFieldStub stub = getGreenStub();
        if (stub != null) {
            return stub.isLocal();
        } else {
            return findChildByType(RapidTokenTypes.LOCAL_KEYWORD) != null;
        }
    }

    @Override
    public boolean isTask() {
        RapidFieldStub stub = getGreenStub();
        if (stub != null) {
            return stub.isTask();
        } else {
            return findChildByType(RapidTokenTypes.TASK_KEYWORD) != null;
        }
    }

    @Override
    public @NotNull Attribute getAttribute() {
        RapidFieldStub stub = getGreenStub();
        if (stub != null) {
            return stub.getAttribute();
        } else {
            PsiElement node = findNotNullChildByType(Attribute.TOKEN_SET);
            return Objects.requireNonNull(Attribute.getAttribute(node.getNode().getElementType()));
        }
    }

    @Override
    public @Nullable RapidType getType() {
        RapidType type = getTypeElement() != null ? getTypeElement().getType() : null;
        if (type != null) {
            RapidArray array = findChildByType(RapidElementTypes.ARRAY);
            int dimensions = array != null ? array.getDimensions().size() : 0;
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
    public @Nullable RapidExpression getInitializer() {
        return findChildByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public boolean hasInitializer() {
        RapidFieldStub stub = getGreenStub();
        if (stub != null) {
            return stub.getInitializer() != null;
        } else {
            return findChildByType(RapidElementTypes.EXPRESSIONS) != null;
        }
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return findChildByType(RapidTokenTypes.IDENTIFIER);
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
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        RapidElementUtil.setName(Objects.requireNonNull(getNameIdentifier()), name);
        return this;
    }

    @Override
    public String toString() {
        return "RapidField:" + getName();
    }
}

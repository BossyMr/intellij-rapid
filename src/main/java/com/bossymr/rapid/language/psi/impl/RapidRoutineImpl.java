package com.bossymr.rapid.language.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.util.IncorrectOperationException;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.stubs.RapidRoutineStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class RapidRoutineImpl extends RapidStubElement<RapidRoutineStub> implements RapidRoutine {

    public RapidRoutineImpl(@NotNull RapidRoutineStub stub) {
        super(stub, RapidStubElementTypes.ROUTINE);
    }

    public RapidRoutineImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitRoutine(this);
    }

    @Override
    public boolean isLocal() {
        RapidRoutineStub stub = getGreenStub();
        if (stub != null) {
            return stub.isLocal();
        } else {
            return findChildByType(RapidTokenTypes.LOCAL_KEYWORD) != null;
        }
    }

    @Override
    public @NotNull Attribute getAttribute() {
        RapidRoutineStub stub = getGreenStub();
        if (stub != null) {
            return stub.getAttribute();
        } else {
            PsiElement node = findNotNullChildByType(Attribute.TOKEN_SET);
            return Objects.requireNonNull(Attribute.getAttribute(node.getNode().getElementType()));
        }
    }

    @Override
    public @Nullable RapidType getType() {
        return getTypeElement() != null ? getTypeElement().getType() : null;
    }

    @Override
    public @Nullable RapidTypeElement getTypeElement() {
        return findChildByType(RapidElementTypes.TYPE_ELEMENT);
    }

    @Override
    public @Nullable RapidParameterList getParameterList() {
        return getStubOrPsiChild(RapidStubElementTypes.PARAMETER_LIST);
    }

    @Override
    public @NotNull List<RapidField> getFields() {
        return List.of(getStubOrPsiChildren(RapidStubElementTypes.FIELD, new RapidField[0]));
    }

    @Override
    public @NotNull RapidStatementList getStatementList() {
        return findNotNullChildByType(RapidElementTypes.STATEMENT_LIST);
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
        return "RapidRoutine:" + getName();
    }
}

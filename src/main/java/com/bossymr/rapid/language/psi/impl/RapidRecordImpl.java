package com.bossymr.rapid.language.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.util.IncorrectOperationException;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.stubs.RapidRecordStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class RapidRecordImpl extends RapidStubElement<RapidRecordStub> implements RapidRecord {

    public RapidRecordImpl(@NotNull RapidRecordStub stub) {
        super(stub, RapidStubElementTypes.RECORD);
    }

    public RapidRecordImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitRecord(this);
    }

    @Override
    public @NotNull List<RapidComponent> getComponents() {
        return List.of(getStubOrPsiChildren(RapidStubElementTypes.COMPONENT, new RapidComponent[0]));
    }

    @Override
    public boolean isLocal() {
        RapidRecordStub stub = getGreenStub();
        if (stub != null) {
            return stub.isLocal();
        } else {
            return findChildByType(RapidTokenTypes.LOCAL_KEYWORD) != null;
        }
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
        return "RapidRecord:" + getName();
    }
}

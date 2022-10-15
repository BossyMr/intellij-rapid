package com.bossymr.rapid.language.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.IncorrectOperationException;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.stubs.RapidModuleStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RapidModuleImpl extends RapidStubElement<RapidModuleStub> implements RapidModule {

    public RapidModuleImpl(@NotNull RapidModuleStub stub) {
        super(stub, RapidStubElementTypes.MODULE);
    }

    public RapidModuleImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitModule(this);
    }

    @Override
    public @NotNull RapidAttributeList getAttributeList() {
        return getRequiredStubOrPsiChild(RapidStubElementTypes.ATTRIBUTE_LIST);
    }

    @Override
    public @NotNull Set<ModuleAttribute> getAttributes() {
        return getAttributeList().getAttributes();
    }

    @Override
    public boolean hasAttribute(ModuleAttribute attribute) {
        return getAttributeList().hasAttribute(attribute);
    }

    @Override
    public void setAttribute(ModuleAttribute attribute, boolean value) throws UnsupportedOperationException {
        getAttributeList().setAttribute(attribute, value);
    }

    @Override
    public @NotNull List<RapidStructure> getStructures() {
        return List.of(getStubOrPsiChildren(TokenSet.create(RapidStubElementTypes.ALIAS, RapidStubElementTypes.RECORD), new RapidStructure[0]));
    }

    @Override
    public @NotNull List<RapidField> getFields() {
        return List.of(getStubOrPsiChildren(RapidStubElementTypes.FIELD, new RapidField[0]));
    }

    @Override
    public @NotNull List<RapidRoutine> getRoutines() {
        return List.of(getStubOrPsiChildren(RapidStubElementTypes.ROUTINE, new RapidRoutine[0]));
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return findChildByType(RapidTokenTypes.IDENTIFIER);
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        boolean renameFile = renameFile();
        RapidElementUtil.setName(Objects.requireNonNull(getNameIdentifier()), name);
        if (renameFile) {
            PsiFile file = getContainingFile();
            String fileName = file.getName();
            int index = fileName.lastIndexOf('.');
            file.setName(index >= 0 ? name + "." + fileName.substring(index + 1) : name);
        }
        return this;
    }

    private boolean renameFile() {
        PsiFile file = getContainingFile();
        String name = file.getName();
        int index = name.lastIndexOf('.');
        if (index >= 0) name = name.substring(0, index);
        return name.equals(getName());
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
        return "RapidModule:" + getName();
    }
}

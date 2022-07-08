package io.github.bossymr.language.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.github.bossymr.language.psi.*;
import io.github.bossymr.language.psi.stubs.RapidModuleStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class RapidModuleImpl extends RapidStubElementImpl<RapidModuleStub> implements RapidModule {

    public RapidModuleImpl(@NotNull RapidModuleStub stub) {
        super(stub, RapidStubElementTypes.MODULE);
    }

    public RapidModuleImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull RapidAttributeList getAttributeList() {
        return findNotNullChildByClass(RapidAttributeList.class);
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
        return Arrays.asList(findChildrenByClass(RapidStructure.class));
    }

    @Override
    public @NotNull List<RapidField> getFields() {
        return Arrays.asList(findChildrenByClass(RapidField.class));
    }

    @Override
    public @NotNull List<RapidRoutine> getRoutines() {
        return Arrays.asList(findChildrenByClass(RapidRoutine.class));
    }

    @Override
    public int getTextOffset() {
        PsiElement identifier = getNameIdentifier();
        return identifier != null ? identifier.getTextOffset() : super.getTextOffset();
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return findChildByType(RapidTokenTypes.IDENTIFIER);
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        return null; // TODO: 2022-07-07
    }

    @Override
    public String getName() {
        final RapidModuleStub stub = getGreenStub();
        if(stub != null) {
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

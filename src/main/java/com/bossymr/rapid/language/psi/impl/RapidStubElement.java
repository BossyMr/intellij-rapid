package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.RapidLanguage;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;

public abstract class RapidStubElement<T extends StubElement<?>> extends StubBasedPsiElementBase<T> implements StubBasedPsiElement<T> {

    public RapidStubElement(@NotNull T stub, @NotNull IStubElementType<?, ?> nodeType) {
        super(stub, nodeType);
    }

    public RapidStubElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public int getTextOffset() {
        return ((CompositeElement) getNode()).getTextOffset();
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof RapidElementVisitor) {
            accept(((RapidElementVisitor) visitor));
        } else {
            visitor.visitElement(this);
        }
    }

    public abstract void accept(@NotNull RapidElementVisitor visitor);

    @Override
    public @NotNull Language getLanguage() {
        return RapidLanguage.INSTANCE;
    }
}

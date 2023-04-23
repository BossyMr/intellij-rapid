package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.RapidLanguage;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;

public abstract class RapidStubElement<T extends StubElement<?>> extends StubBasedPsiElementBase<T> implements StubBasedPsiElement<T> {

    protected RapidStubElement(@NotNull T stub, @NotNull IStubElementType<?, ?> nodeType) {
        super(stub, nodeType);
    }

    protected RapidStubElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public int getTextOffset() {
        PsiElement element = findChildByType(RapidTokenTypes.IDENTIFIER);
        return element != null ? element.getTextOffset() : super.getTextOffset();
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof RapidElementVisitor) {
            accept(((RapidElementVisitor) visitor));
        } else {
            visitor.visitElement(this);
        }
    }

    protected abstract void accept(@NotNull RapidElementVisitor visitor);

    @Override
    public @NotNull Language getLanguage() {
        return RapidLanguage.INSTANCE;
    }
}

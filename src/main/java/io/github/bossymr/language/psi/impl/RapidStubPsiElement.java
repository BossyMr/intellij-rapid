package io.github.bossymr.language.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import io.github.bossymr.language.RapidLanguage;
import org.jetbrains.annotations.NotNull;

public abstract class RapidStubPsiElement<T extends StubElement<?>> extends StubBasedPsiElementBase<T> implements StubBasedPsiElement<T> {

    public RapidStubPsiElement(@NotNull T stub, @NotNull IStubElementType<?, ?> nodeType) {
        super(stub, nodeType);
    }

    public RapidStubPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Language getLanguage() {
        return RapidLanguage.INSTANCE;
    }
}

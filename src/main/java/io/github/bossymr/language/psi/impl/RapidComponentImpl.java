package io.github.bossymr.language.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.github.bossymr.language.psi.*;
import io.github.bossymr.language.psi.stubs.RapidComponentStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidComponentImpl extends RapidStubElement<RapidComponentStub> implements RapidComponent {

    public RapidComponentImpl(@NotNull RapidComponentStub stub) {
        super(stub, RapidStubElementTypes.COMPONENT);
    }

    public RapidComponentImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitComponent(this);
    }

    @Override
    public @Nullable RapidType getType() {
        return getTypeElement().getType();
    }

    @Override
    public @NotNull RapidTypeElement getTypeElement() {
        return findNotNullChildByType(RapidElementTypes.TYPE_ELEMENT);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return findChildByType(RapidTokenTypes.IDENTIFIER);
    }

    @Override
    public String getName() {
        return RapidElementUtil.getName(this);
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        RapidElementUtil.setName(Objects.requireNonNull(getNameIdentifier()), name);
        return this;
    }

    @Override
    public String toString() {
        return "RapidComponent:" + getName();
    }
}

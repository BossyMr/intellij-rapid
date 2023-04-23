package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.bossymr.rapid.language.psi.impl.RapidStubElement;
import com.bossymr.rapid.language.psi.stubs.RapidParameterStub;
import com.bossymr.rapid.language.symbol.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PhysicalParameter extends RapidStubElement<RapidParameterStub> implements RapidParameter, PhysicalVariable {

    public PhysicalParameter(@NotNull RapidParameterStub stub) {
        super(stub, RapidStubElementTypes.PARAMETER);
    }

    public PhysicalParameter(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull RapidParameterGroup getParameterGroup() {
        return Objects.requireNonNull(getStubOrPsiParentOfType(PhysicalParameterGroup.class));
    }

    @Override
    public @NotNull String getCanonicalName() {
        return RapidParameter.super.getCanonicalName();
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitParameter(this);
    }

    @Override
    public @NotNull ParameterType getParameterType() {
        RapidParameterStub stub = getGreenStub();
        if (stub != null) {
            return stub.getAttribute();
        } else {
            return ParameterType.getAttribute(this);
        }
    }

    public @Nullable RapidTypeElement getTypeElement() {
        return findChildByType(RapidElementTypes.TYPE_ELEMENT);
    }

    @Override
    public @Nullable RapidType getType() {
        int dimensions = findChildrenByType(RapidTokenTypes.ASTERISK).size();
        return SymbolUtil.getType(this, dimensions);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return findChildByType(RapidTokenTypes.IDENTIFIER);
    }

    @Override
    public String getName() {
        return SymbolUtil.getName(this);
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        RapidElementUtil.setName(Objects.requireNonNull(getNameIdentifier()), name);
        return this;
    }

    @Override
    public @NotNull PhysicalPointer<PhysicalParameter> createPointer() {
        return new PhysicalPointer<>(this);
    }

    @Override
    public String toString() {
        return "PhysicalParameter{" +
                "parameterType=" + getParameterType() +
                ", type=" + getType() +
                ", name='" + getName() + '\'' +
                '}';
    }
}

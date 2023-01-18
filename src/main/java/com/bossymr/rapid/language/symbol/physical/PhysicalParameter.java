package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.bossymr.rapid.language.psi.impl.RapidStubElement;
import com.bossymr.rapid.language.psi.stubs.RapidParameterStub;
import com.bossymr.rapid.language.symbol.RapidParameter;
import com.bossymr.rapid.language.symbol.RapidParameterGroup;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.SymbolUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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
    public @Nullable Icon getIcon(int flags) {
        return getIcon();
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitParameter(this);
    }

    @Override
    public @NotNull Attribute getAttribute() {
        RapidParameterStub stub = getGreenStub();
        if (stub != null) {
            return stub.getAttribute();
        } else {
            return Attribute.getAttribute(this);
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
    public String toString() {
        return "PhysicalParameter:" + this.getName();
    }
}

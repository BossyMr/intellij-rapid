package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.bossymr.rapid.language.psi.impl.RapidStubElement;
import com.bossymr.rapid.language.psi.stubs.RapidComponentStub;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.SymbolUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PhysicalComponent extends RapidStubElement<RapidComponentStub> implements RapidComponent, PhysicalSymbol {

    public PhysicalComponent(@NotNull RapidComponentStub stub) {
        super(stub, RapidStubElementTypes.COMPONENT);
    }

    public PhysicalComponent(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitComponent(this);
    }

    @Override
    public @Nullable RapidType getType() {
        return SymbolUtil.getType(this);
    }

    @Override
    public @NotNull PhysicalRecord getRecord() {
        return Objects.requireNonNull(getStubOrPsiParentOfType(PhysicalRecord.class));
    }

    @Override
    public @NotNull String getCanonicalName() {
        return RapidComponent.super.getCanonicalName();
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return findChildByType(RapidTokenTypes.IDENTIFIER);
    }

    @Override
    public @NotNull PhysicalPointer<PhysicalComponent> createPointer() {
        return new PhysicalPointer<>(this);
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
        return "PhysicalComponent{" +
                "name='" + getName() + '\'' +
                '}';
    }
}

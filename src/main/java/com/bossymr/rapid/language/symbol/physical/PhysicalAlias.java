package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.bossymr.rapid.language.psi.impl.RapidStubElement;
import com.bossymr.rapid.language.psi.stubs.RapidAliasStub;
import com.bossymr.rapid.language.symbol.RapidAlias;
import com.bossymr.rapid.language.type.RapidType;
import com.bossymr.rapid.language.symbol.SymbolUtil;
import com.bossymr.rapid.language.symbol.Visibility;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PhysicalAlias extends RapidStubElement<RapidAliasStub> implements RapidAlias, PhysicalStructure {

    public PhysicalAlias(@NotNull RapidAliasStub stub) {
        super(stub, RapidStubElementTypes.ALIAS);
    }

    public PhysicalAlias(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    protected void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitAlias(this);
    }

    public @Nullable RapidTypeElement getTypeElement() {
        return findChildByType(RapidElementTypes.TYPE_ELEMENT);
    }

    @Override
    public @NotNull Visibility getVisibility() {
        return SymbolUtil.getVisibility(this);
    }

    @Override
    public @Nullable RapidType getType() {
        return SymbolUtil.getType(this);
    }

    @Override
    public @Nullable String getName() {
        return SymbolUtil.getName(this);
    }

    public @Nullable PsiElement getNameIdentifier() {
        return findChildByType(RapidTokenTypes.IDENTIFIER);
    }

    @Override
    public @NotNull PhysicalPointer<PhysicalAlias> createPointer() {
        return new PhysicalPointer<>(this);
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        RapidElementUtil.setName(Objects.requireNonNull(getNameIdentifier()), name);
        return this;
    }

    @Override
    public String toString() {
        return "PhysicalAlias{" +
                "name='" + getName() + '\'' +
                '}';
    }
}

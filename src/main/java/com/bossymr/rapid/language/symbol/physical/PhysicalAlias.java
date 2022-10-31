package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.bossymr.rapid.language.psi.impl.RapidStubElement;
import com.bossymr.rapid.language.psi.stubs.RapidAliasStub;
import com.bossymr.rapid.language.symbol.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PhysicalAlias extends RapidStubElement<RapidAliasStub> implements RapidAlias, PhysicalSymbol {

    public PhysicalAlias(@NotNull RapidAliasStub stub) {
        super(stub, RapidStubElementTypes.ALIAS);
    }

    public PhysicalAlias(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitAlias(this);
    }

    @Override
    public @NotNull Visibility getVisibility() {
        RapidAliasStub stub = getGreenStub();
        if (stub != null) {
            return stub.getVisibility();
        } else {
            return Visibility.getVisibility(this);
        }
    }

    public @Nullable RapidTypeElement getTypeElement() {
        return findChildByType(RapidElementTypes.TYPE_ELEMENT);
    }

    @Override
    public @Nullable RapidType getType() {
        return CachedValuesManager.getProjectPsiDependentCache(this, (ignored) -> {
            RapidAliasStub stub = getGreenStub();
            if (stub != null) {
                String typeName = stub.getType();
                if (typeName == null) return null;
                RapidStructure structure = ResolveUtil.getStructure(this, typeName);
                return new RapidType(structure, typeName);
            } else {
                return getTypeElement() != null ? getTypeElement().getType() : null;
            }
        });
    }

    @Override
    public String getName() {
        RapidAliasStub stub = getGreenStub();
        if (stub != null) {
            return stub.getName();
        } else {
            PsiElement identifier = getNameIdentifier();
            return identifier != null ? identifier.getText() : null;
        }
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return findChildByType(RapidTokenTypes.IDENTIFIER);
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        RapidElementUtil.setName(Objects.requireNonNull(getNameIdentifier()), name);
        return this;
    }
}

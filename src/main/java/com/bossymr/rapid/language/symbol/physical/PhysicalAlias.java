package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.bossymr.rapid.language.psi.impl.RapidStubElement;
import com.bossymr.rapid.language.psi.stubs.RapidAliasStub;
import com.bossymr.rapid.language.symbol.RapidAlias;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.SymbolUtil;
import com.bossymr.rapid.language.symbol.Visibility;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ColoredItemPresentation;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return findChildByType(RapidTokenTypes.IDENTIFIER);
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        RapidElementUtil.setName(Objects.requireNonNull(getNameIdentifier()), name);
        return this;
    }

    @Override
    public @Nullable ItemPresentation getPresentation() {
        return new ColoredItemPresentation() {
            @Override
            public @Nullable TextAttributesKey getTextAttributesKey() {
                return null;
            }

            @Override
            public @Nullable String getPresentableText() {
                return getName();
            }

            @Override
            public @Nullable Icon getIcon(boolean unused) {
                return null;
            }
        };
    }
}

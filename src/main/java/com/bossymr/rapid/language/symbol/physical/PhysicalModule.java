package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.bossymr.rapid.language.psi.impl.RapidStubElement;
import com.bossymr.rapid.language.psi.stubs.RapidModuleStub;
import com.bossymr.rapid.language.symbol.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class PhysicalModule extends RapidStubElement<RapidModuleStub> implements RapidModule, PhysicalSymbol {

    public PhysicalModule(@NotNull RapidModuleStub stub) {
        super(stub, RapidStubElementTypes.MODULE);
    }

    public PhysicalModule(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitModule(this);
    }

    public @Nullable RapidAttributeList getAttributeList() {
        return getStubOrPsiChild(RapidStubElementTypes.ATTRIBUTE_LIST);
    }

    @Override
    public @NotNull Set<Attribute> getAttributes() {
        RapidAttributeList attributeList = getAttributeList();
        return attributeList != null ? attributeList.getAttributes() : Collections.emptySet();
    }

    @Override
    public boolean hasAttribute(@NotNull Attribute attribute) {
        RapidAttributeList attributeList = getAttributeList();
        return attributeList != null && attributeList.hasAttribute(attribute);
    }

    @Override
    public @NotNull List<RapidAccessibleSymbol> getSymbols() {
        return Stream.of(getStructures(), getFields(), getRoutines())
                .flatMap(Collection::stream)
                .map(symbol -> (RapidAccessibleSymbol) symbol)
                .toList();
    }

    @Override
    public @NotNull List<RapidStructure> getStructures() {
        PsiElement[] elements = getStubOrPsiChildren(TokenSet.create(RapidElementTypes.ALIAS, RapidElementTypes.RECORD), new PsiElement[0]);
        return Arrays.stream(elements)
                .map(symbol -> (RapidStructure) symbol)
                .toList();
    }

    @Override
    public @NotNull List<RapidField> getFields() {
        PsiElement[] elements = getStubOrPsiChildren(TokenSet.create(RapidElementTypes.FIELD), new PsiElement[0]);
        return Arrays.stream(elements)
                .map(symbol -> (RapidField) symbol)
                .toList();
    }

    @Override
    public @NotNull List<RapidRoutine> getRoutines() {
        PsiElement[] elements = getStubOrPsiChildren(TokenSet.create(RapidElementTypes.ROUTINE), new PsiElement[0]);
        return Arrays.stream(elements)
                .map(symbol -> (RapidRoutine) symbol)
                .toList();
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return findChildByType(RapidTokenTypes.IDENTIFIER);
    }

    @Override
    public String getName() {
        RapidModuleStub stub = getGreenStub();
        if (stub != null) {
            return stub.getName();
        } else {
            PsiElement identifier = getNameIdentifier();
            return identifier != null ? identifier.getText() : null;
        }
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        RapidElementUtil.setName(Objects.requireNonNull(getNameIdentifier()), name);
        return this;
    }

    @Override
    public String toString() {
        return "PhysicalModule:" + getName();
    }
}

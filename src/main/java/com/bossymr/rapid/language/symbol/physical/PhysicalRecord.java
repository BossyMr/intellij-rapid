package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.bossymr.rapid.language.psi.impl.RapidStubElement;
import com.bossymr.rapid.language.psi.stubs.RapidRecordStub;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.symbol.SymbolUtil;
import com.bossymr.rapid.language.symbol.Visibility;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class PhysicalRecord extends RapidStubElement<RapidRecordStub> implements RapidRecord, PhysicalStructure {

    public PhysicalRecord(@NotNull RapidRecordStub stub) {
        super(stub, RapidStubElementTypes.RECORD);
    }

    public PhysicalRecord(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitRecord(this);
    }

    @Override
    public @NotNull Visibility getVisibility() {
        RapidRecordStub stub = getGreenStub();
        if (stub != null) {
            return stub.getVisibility();
        } else {
            return Visibility.getVisibility(this);
        }
    }

    @Override
    public @NotNull List<RapidComponent> getComponents() {
        return List.of(getStubOrPsiChildren(RapidStubElementTypes.COMPONENT, new PhysicalComponent[0]));
    }

    @Override
    public @NotNull PhysicalPointer<PhysicalRecord> createPointer() {
        return new PhysicalPointer<>(this);
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
    public @Nullable ASTNode addInternal(@Nullable ASTNode first, @Nullable ASTNode last, @Nullable ASTNode anchor, @Nullable Boolean before) {
        if (!(first instanceof TreeElement)) return null;
        if (anchor == null) {
            if (before == null || before) {
                anchor = findChildByType(RapidTokenTypes.ENDRECORD_KEYWORD);
                before = true;
            } else {
                anchor = findChildByType(RapidTokenTypes.IDENTIFIER);
                before = false;
            }
        }
        return super.addInternal(first, last, anchor, before);
    }

    @Override
    public String toString() {
        return "PhysicalRecord{" +
                "visibility=" + getVisibility() +
                ", name='" + getName() + '\'' +
                '}';
    }
}

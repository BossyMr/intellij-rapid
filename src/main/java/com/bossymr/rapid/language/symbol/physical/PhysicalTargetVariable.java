package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.PhysicalElement;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidTargetVariable;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.SymbolUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PhysicalTargetVariable extends PhysicalElement implements RapidTargetVariable, PhysicalVariable {

    public PhysicalTargetVariable(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return findChildByType(RapidTokenTypes.IDENTIFIER);
    }

    @Override
    public boolean isModifiable() {
        return false;
    }

    @Override
    public int getTextOffset() {
        ASTNode name = findChildByType(RapidTokenTypes.IDENTIFIER);
        return name != null ? name.getStartOffset() : super.getTextOffset();
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
    public @Nullable RapidType getType() {
        return RapidType.NUMBER;
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitTargetVariable(this);
    }

    @Override
    public @NotNull PhysicalPointer<PhysicalTargetVariable> createPointer() {
        return new PhysicalPointer<>(this);
    }

    @Override
    public String toString() {
        return "PhysicalTargetVariable{" +
                "name='" + getName() + '\'' +
                ", type=" + getType() +
                '}';
    }
}

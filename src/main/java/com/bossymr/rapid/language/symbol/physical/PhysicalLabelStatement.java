package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.PhysicalElement;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidElementUtil;
import com.bossymr.rapid.language.symbol.RapidLabelStatement;
import com.bossymr.rapid.language.symbol.SymbolUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PhysicalLabelStatement extends PhysicalElement implements RapidLabelStatement, PhysicalSymbol {

    public PhysicalLabelStatement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitLabel(this);
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
    public @NotNull PhysicalPointer<PhysicalLabelStatement> createPointer() {
        return new PhysicalPointer<>(this);
    }

    @Override
    public String toString() {
        return "PhysicalLabelStatement{" +
                "name='" + getName() + '\'' +
                '}';
    }
}

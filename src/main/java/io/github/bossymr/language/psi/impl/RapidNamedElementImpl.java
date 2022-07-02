package io.github.bossymr.language.psi.impl;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import io.github.bossymr.language.psi.RapidNamedElement;
import io.github.bossymr.language.psi.node.RapidElementTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidNamedElementImpl extends CompositePsiElement implements RapidNamedElement {

    protected RapidNamedElementImpl(IElementType type) {
        super(type);
    }

    @Override
    public int getTextOffset() {
        PsiElement identifier = getNameIdentifier();
        return identifier != null ? identifier.getTextOffset() : super.getTextOffset();
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return findPsiChildByType(RapidElementTypes.IDENTIFIER);
    }

    @Override
    public String getName() {
        PsiElement identifier = getNameIdentifier();
        return identifier != null ? identifier.getText() : null;
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        return null; // TODO: 2022-07-01 Create RapidElementFactory
    }
}

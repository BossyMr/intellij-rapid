package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.RapidLanguage;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LightTreeUtil;
import com.intellij.psi.stubs.ILightStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.ICompositeElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class RapidStubElementType<StubT extends StubElement<?>, PsiT extends PsiElement>
        extends ILightStubElementType<StubT, PsiT> implements ICompositeElementType {

    public RapidStubElementType(@NotNull @NonNls String debugName) {
        super(debugName, RapidLanguage.INSTANCE);
    }

    public abstract @NotNull PsiElement createPsi(@NotNull final ASTNode node);

    @Override
    public @NotNull StubT createStub(@NotNull PsiT psi, StubElement<? extends PsiElement> parentStub) {
        throw new UnsupportedOperationException("Cannot create stub for '" + psi.getNode().getElementType() + "'");
    }

    protected @Nullable String getText(LighterAST tree, LighterASTNode node, IElementType elementType) {
        return getText(tree, node, TokenSet.create(elementType));
    }

    protected @Nullable String getText(LighterAST tree, LighterASTNode node, TokenSet tokenSet) {
        LighterASTNode element = LightTreeUtil.firstChildOfType(tree, node, tokenSet);
        return element != null ? LightTreeUtil.toFilteredString(tree, element, null) : null;
    }

    protected boolean hasChild(LighterAST tree, LighterASTNode node, IElementType elementType) {
        return hasChild(tree, node, TokenSet.create(elementType));
    }

    protected boolean hasChild(LighterAST tree, LighterASTNode node, TokenSet tokenSet) {
        return LightTreeUtil.firstChildOfType(tree, node, tokenSet) != null;
    }

    @Override
    public @NotNull String getExternalId() {
        return "rapid." + this;
    }
}

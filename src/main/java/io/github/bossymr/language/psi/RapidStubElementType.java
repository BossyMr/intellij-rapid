package io.github.bossymr.language.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.ILightStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.ICompositeElementType;
import io.github.bossymr.language.RapidLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

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

    @Override
    public @NotNull String getExternalId() {
        return "rapid." + super.toString();
    }
}

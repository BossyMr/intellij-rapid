package io.github.bossymr.language.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import io.github.bossymr.language.RapidLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public abstract class RapidStubElementType<S extends StubElement<?>, P extends PsiElement> extends IStubElementType<S, P> {

    public RapidStubElementType(@NotNull @NonNls String debugName) {
        super(debugName, RapidLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "Rapid:" + super.toString();
    }

    public abstract @NotNull PsiElement createElement(@NotNull final ASTNode node);

    @Override
    public void indexStub(@NotNull S stub, @NotNull IndexSink sink) {}

    @Override
    public @NotNull String getExternalId() {
        return "rapid." + super.toString();
    }
}

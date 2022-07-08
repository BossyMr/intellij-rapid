package io.github.bossymr.language.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import io.github.bossymr.language.RapidLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class RapidElementType extends IElementType {

    private final @NotNull Function<? super ASTNode, ? extends PsiElement> factory;

    public RapidElementType(@NotNull @NonNls String debugName) {
        super(debugName, RapidLanguage.INSTANCE);
        factory = node -> {
            throw new IllegalStateException("Cannot create element for '" + node.getElementType() + "'");
        };
    }

    public RapidElementType(@NotNull @NonNls String debugName, @NotNull Function<? super ASTNode, ? extends PsiElement> factory) {
        super(debugName, RapidLanguage.INSTANCE);
        this.factory = factory;
    }

    public @NotNull PsiElement createElement(@NotNull ASTNode node) {
        return factory.apply(node);
    }

    @Override
    public String toString() {
        return "Rapid:" + super.toString();
    }
}

package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.RapidLanguage;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.ICompositeElementType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class RapidElementType extends IElementType implements ICompositeElementType {

    private final @NotNull Supplier<? extends ASTNode> factory;

    public RapidElementType(@NotNull @NonNls String debugName) {
        super(debugName, RapidLanguage.INSTANCE);
        factory = () -> {
            throw new IllegalStateException("Cannot create element for '" + debugName + "'");
        };
    }

    public RapidElementType(@NotNull @NonNls String debugName, @NotNull Supplier<? extends ASTNode> factory) {
        super(debugName, RapidLanguage.INSTANCE);
        this.factory = factory;
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
        return factory.get();
    }

    @Override
    public String toString() {
        return "Rapid:" + super.toString();
    }
}

package io.github.bossymr.language.psi.node;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.ICompositeElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class RapidCompositeElementType extends RapidElementType implements ICompositeElementType {

    private final Supplier<? extends ASTNode> supplier;

    public RapidCompositeElementType(@NotNull String debugName, @NotNull Supplier<? extends ASTNode> supplier) {
        this(debugName, supplier, false);

    }

    public RapidCompositeElementType(@NonNls String debugName, Supplier<? extends ASTNode> supplier, boolean isLeft) {
        super(debugName, isLeft);
        this.supplier = supplier;
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
        return supplier.get();
    }
}

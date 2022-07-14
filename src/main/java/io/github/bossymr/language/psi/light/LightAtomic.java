package io.github.bossymr.language.psi.light;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.util.IncorrectOperationException;
import io.github.bossymr.language.psi.RapidAtomic;
import io.github.bossymr.language.psi.RapidStructure;
import io.github.bossymr.language.psi.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LightAtomic extends FakePsiElement implements RapidAtomic {

    private final String name;
    private final RapidStructure structure;

    public LightAtomic(String name) {
        this(name, null);
    }

    public LightAtomic(String name, @Nullable RapidStructure structure) {
        this.name = name;
        this.structure = structure;
    }

    @Override
    public PsiElement getParent() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Nullable RapidType getType() {
        return structure != null ? RapidType.create(structure) : null;
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return null;
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    @Override
    public String toString() {
        return "RapidAtomic:" + getName();
    }
}

package io.github.bossymr.language.psi.impl;

import com.intellij.util.IncorrectOperationException;
import io.github.bossymr.language.psi.*;
import io.github.bossymr.language.psi.node.RapidElementTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class RapidModuleImpl extends RapidNamedElementImpl implements RapidModule {

    public RapidModuleImpl() {
        super(RapidElementTypes.MODULE);
    }

    @Override
    public @NotNull Set<ModuleAttribute> getAttributes() {
        return null;
    }

    @Override
    public boolean hasAttribute(@NotNull ModuleAttribute attribute) {
        return false;
    }

    @Override
    public void setAttribute(@NotNull ModuleAttribute attribute, boolean value) throws IncorrectOperationException {

    }

    @Override
    public @NotNull List<RapidStructure> getStructures() {
        return null;
    }

    @Override
    public @NotNull List<RapidField> getFields() {
        return null;
    }

    @Override
    public @NotNull List<RapidRoutine> getRoutines() {
        return null;
    }
}

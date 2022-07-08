package io.github.bossymr.language.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import io.github.bossymr.language.psi.RapidModule;
import io.github.bossymr.language.psi.RapidStubElementTypes;
import io.github.bossymr.language.psi.stubs.RapidModuleStub;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

public class RapidModuleStubImpl extends StubBase<RapidModule> implements RapidModuleStub {

    private final @Nullable String name;

    protected RapidModuleStubImpl(@Nullable StubElement<?> parent, @Nullable String name) {
        super(parent, RapidStubElementTypes.MODULE);
        this.name = name;
    }

    @Override
    public @NonNls @Nullable String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "RapidModuleStub[" + name + "]";
    }
}

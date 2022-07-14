package io.github.bossymr.language.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import io.github.bossymr.language.psi.RapidComponent;
import io.github.bossymr.language.psi.RapidStubElementTypes;
import io.github.bossymr.language.psi.stubs.RapidComponentStub;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

public class RapidComponentStubImpl extends StubBase<RapidComponent> implements RapidComponentStub {

    private final String name, type;

    public RapidComponentStubImpl(@Nullable StubElement<?> parent, @Nullable String name, @Nullable String type) {
        super(parent, RapidStubElementTypes.COMPONENT);
        this.name = name;
        this.type = type;
    }

    @Override
    public @Nullable String getType() {
        return type;
    }

    @Override
    public @NonNls @Nullable String getName() {
        return name;
    }
}

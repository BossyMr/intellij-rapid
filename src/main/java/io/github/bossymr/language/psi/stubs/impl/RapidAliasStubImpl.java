package io.github.bossymr.language.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import io.github.bossymr.language.psi.RapidAlias;
import io.github.bossymr.language.psi.RapidStubElementTypes;
import io.github.bossymr.language.psi.stubs.RapidAliasStub;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

public class RapidAliasStubImpl extends StubBase<RapidAlias> implements RapidAliasStub {

    private final String name, type;
    private final boolean isLocal;

    public RapidAliasStubImpl(@Nullable StubElement<?> parent, @Nullable String name, @Nullable String type, boolean isLocal) {
        super(parent, RapidStubElementTypes.ALIAS);
        this.name = name;
        this.type = type;
        this.isLocal = isLocal;
    }

    @Override
    public @Nullable String getType() {
        return type;
    }

    @Override
    public @NonNls @Nullable String getName() {
        return name;
    }

    @Override
    public boolean isLocal() {
        return isLocal;
    }

    @Override
    public String toString() {
        return "RapidAliasStub[" + (isLocal() ? "LOCAL" + " " : "") + getType() + " " + getName() + "]";
    }
}

package com.bossymr.rapid.language.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.bossymr.rapid.language.psi.RapidModule;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.stubs.RapidModuleStub;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidModuleStubImpl extends StubBase<RapidModule> implements RapidModuleStub {

    private final @Nullable String name;

    public RapidModuleStubImpl(@Nullable StubElement<?> parent, @Nullable String name) {
        super(parent, RapidStubElementTypes.MODULE);
        this.name = name;
    }

    @Override
    public @NonNls @Nullable String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidModuleStubImpl that = (RapidModuleStubImpl) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    @Override
    public String toString() {
        return "RapidModuleStub{" +
                "name='" + name + '\'' +
                '}';
    }
}

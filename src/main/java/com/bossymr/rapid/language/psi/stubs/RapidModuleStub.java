package com.bossymr.rapid.language.psi.stubs;

import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidModuleStub extends NamedStubBase<PhysicalModule> {
    public RapidModuleStub(@Nullable StubElement<?> parent, @Nullable String name) {
        super(parent, RapidStubElementTypes.MODULE, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidModuleStub stub = (RapidModuleStub) o;
        return Objects.equals(getName(), stub.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    @Override
    public String toString() {
        return "RapidModuleStub:" + getName();
    }
}

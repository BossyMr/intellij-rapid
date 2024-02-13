package com.bossymr.rapid.language.psi.stubs;

import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.RapidTypeStub;
import com.bossymr.rapid.language.symbol.physical.PhysicalComponent;
import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidComponentStub extends NamedStubBase<PhysicalComponent> implements RapidTypeStub {

    private final String type;

    public RapidComponentStub(@Nullable StubElement<?> parent, @Nullable String name, @Nullable String type) {
        super(parent, RapidStubElementTypes.COMPONENT, name);
        this.type = type;
    }

    @Override
    public @Nullable String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidComponentStub that = (RapidComponentStub) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getType());
    }

    @Override
    public String toString() {
        return "RapidComponentStub:" + getName();
    }
}

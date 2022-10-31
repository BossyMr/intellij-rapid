package com.bossymr.rapid.language.psi.stubs;

import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.symbol.Visibility;
import com.bossymr.rapid.language.symbol.physical.PhysicalRecord;
import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidRecordStub extends NamedStubBase<PhysicalRecord> {

    private final Visibility visibility;

    public RapidRecordStub(@Nullable StubElement<?> parent, @NotNull Visibility visibility, @Nullable String name) {
        super(parent, RapidStubElementTypes.RECORD, name);
        this.visibility = visibility;
    }

    public @NotNull Visibility getVisibility() {
        return visibility;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidRecordStub that = (RapidRecordStub) o;
        return getVisibility() == that.getVisibility();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVisibility());
    }

    @Override
    public String toString() {
        return "RapidRecordStub:" + getName();
    }
}

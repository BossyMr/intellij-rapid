package com.bossymr.rapid.language.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.bossymr.rapid.language.psi.RapidRecord;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.stubs.RapidRecordStub;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidRecordStubImpl extends StubBase<RapidRecord> implements RapidRecordStub {

    private final String name;
    private final boolean isLocal;

    public RapidRecordStubImpl(@Nullable StubElement<?> parent, @Nullable String name, boolean isLocal) {
        super(parent, RapidStubElementTypes.RECORD);
        this.name = name;
        this.isLocal = isLocal;
    }

    @Override
    public boolean isLocal() {
        return isLocal;
    }

    @Override
    public @NonNls @Nullable String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidRecordStubImpl that = (RapidRecordStubImpl) o;
        return isLocal() == that.isLocal() && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), isLocal());
    }

    @Override
    public String toString() {
        return "RapidRecordStub{" +
                "name='" + name + '\'' +
                ", isLocal=" + isLocal +
                '}';
    }

}

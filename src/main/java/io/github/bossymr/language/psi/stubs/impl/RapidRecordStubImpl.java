package io.github.bossymr.language.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import io.github.bossymr.language.psi.RapidRecord;
import io.github.bossymr.language.psi.RapidStubElementTypes;
import io.github.bossymr.language.psi.stubs.RapidRecordStub;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

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
    public String toString() {
        return "RapidRecordStub[" + (isLocal() ? "LOCAL" + " " : "") + getName() + "]";
    }
}

package io.github.bossymr.language.psi.stubs.impl;

import com.intellij.psi.StubBuilder;
import com.intellij.psi.tree.IStubFileElementType;
import io.github.bossymr.language.RapidLanguage;
import io.github.bossymr.language.psi.stubs.RapidFileStub;

public class RapidFileElementType extends IStubFileElementType<RapidFileStub> {
    public static final int STUB_VERSION = 0;

    public RapidFileElementType() {
        super("rapid.FILE", RapidLanguage.INSTANCE);
    }

    @Override
    public StubBuilder getBuilder() {
        return new RapidStubBuilder();
    }

    @Override
    public int getStubVersion() {
        return STUB_VERSION;
    }
}

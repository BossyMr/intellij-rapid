package com.bossymr.rapid.language.psi.stubs;

import com.bossymr.rapid.language.psi.RapidParameterList;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.Nullable;

public class RapidParameterListStub extends StubBase<RapidParameterList> {

    public RapidParameterListStub(@Nullable StubElement parent) {
        super(parent, RapidStubElementTypes.PARAMETER_LIST);
    }
}

package com.bossymr.rapid.language.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.bossymr.rapid.language.psi.RapidParameterList;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.stubs.RapidParameterListStub;
import org.jetbrains.annotations.Nullable;

public class RapidParameterListStubImpl extends StubBase<RapidParameterList> implements RapidParameterListStub {

    public RapidParameterListStubImpl(@Nullable StubElement<?> parent) {
        super(parent, RapidStubElementTypes.PARAMETER_LIST);
    }

    @Override
    public String toString() {
        return "RapidParameterListStub{}";
    }
}

package io.github.bossymr.language.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import io.github.bossymr.language.psi.RapidAttributeList;
import io.github.bossymr.language.psi.RapidStubElementTypes;
import io.github.bossymr.language.psi.stubs.RapidAttributeListStub;
import org.jetbrains.annotations.Nullable;

public class RapidAttributeListStubImpl extends StubBase<RapidAttributeList> implements RapidAttributeListStub {

    private final int mask;

    protected RapidAttributeListStubImpl(@Nullable StubElement<?> parent, int mask) {
        super(parent, RapidStubElementTypes.ATTRIBUTE_LIST);
        this.mask = mask;
    }

    @Override
    public int getMask() {
        return mask;
    }

    @Override
    public String toString() {
        return "RapidAttributeListStub[" + getMask() + "]";
    }
}

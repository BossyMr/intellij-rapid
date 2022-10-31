package com.bossymr.rapid.language.psi.stubs;

import com.bossymr.rapid.language.psi.RapidAttributeList;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.symbol.RapidModule.Attribute;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.BitUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class RapidAttributeListStub extends StubBase<RapidAttributeList> {

    private final int mask;

    public RapidAttributeListStub(@Nullable StubElement<?> parent, int mask) {
        super(parent, RapidStubElementTypes.ATTRIBUTE_LIST);
        this.mask = mask;
    }

    public RapidAttributeListStub(@Nullable StubElement<?> parent, @NotNull Set<Attribute> attributes) {
        super(parent, RapidStubElementTypes.ATTRIBUTE_LIST);
        int mask = 0;
        for (Attribute attribute : attributes) {
            mask = BitUtil.set(mask, getMask(attribute), true);
        }
        this.mask = mask;
    }

    private static int getMask(@NotNull Attribute attribute) {
        return 1 << attribute.ordinal();
    }

    public int getMask() {
        return mask;
    }

    public @NotNull Set<Attribute> getAttributes() {
        Set<Attribute> attributes = EnumSet.noneOf(Attribute.class);
        for (Attribute attribute : Attribute.values()) {
            if (hasAttribute(attribute)) {
                attributes.add(attribute);
            }
        }
        return attributes;
    }

    public boolean hasAttribute(@NotNull Attribute attribute) {
        return BitUtil.isSet(getMask(), getMask(attribute));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidAttributeListStub that = (RapidAttributeListStub) o;
        return getMask() == that.getMask();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMask());
    }

    @Override
    public String toString() {
        return "RapidAttributeListStub:" + getAttributes();
    }
}

package com.bossymr.rapid.language.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.BitUtil;
import com.bossymr.rapid.language.psi.ModuleAttribute;
import com.bossymr.rapid.language.psi.RapidAttributeList;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.stubs.RapidAttributeListStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class RapidAttributeListStubImpl extends StubBase<RapidAttributeList> implements RapidAttributeListStub {

    private final int mask;

    public RapidAttributeListStubImpl(@Nullable StubElement<?> parent, int mask) {
        super(parent, RapidStubElementTypes.ATTRIBUTE_LIST);
        this.mask = mask;
    }

    public RapidAttributeListStubImpl(@Nullable StubElement<?> parent, @NotNull Set<ModuleAttribute> attributes) {
        super(parent, RapidStubElementTypes.ATTRIBUTE_LIST);
        int value = 0;
        for (ModuleAttribute attribute : attributes) {
            value = BitUtil.set(value, Mask.getMask(attribute), true);
        }
        this.mask = value;
    }

    @Override
    public int getMask() {
        return mask;
    }

    @Override
    public Set<ModuleAttribute> getAttributes() {
        Set<ModuleAttribute> attributes = EnumSet.noneOf(ModuleAttribute.class);
        for (ModuleAttribute attribute : ModuleAttribute.values()) {
            if (BitUtil.isSet(mask, Mask.getMask(attribute))) {
                attributes.add(attribute);
            }
        }
        return attributes;
    }

    @Override
    public boolean hasAttribute(@NotNull ModuleAttribute attribute) {
        return BitUtil.isSet(mask, Mask.getMask(attribute));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidAttributeListStubImpl that = (RapidAttributeListStubImpl) o;
        return getMask() == that.getMask();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMask());
    }

    @Override
    public String toString() {
        return "RapidAttributeListStub{" +
                "mask=" + mask +
                '}';
    }

    private enum Mask {
        SYSTEM_MODULE(1), NO_VIEW(2), NO_STEP_IN(3), VIEW_ONLY(4), READ_ONLY(5);

        private final int mask;

        Mask(int index) {
            this.mask = 1 << index;
        }

        public static int getMask(@NotNull ModuleAttribute attribute) {
            return valueOf(attribute.name()).mask;
        }
    }
}

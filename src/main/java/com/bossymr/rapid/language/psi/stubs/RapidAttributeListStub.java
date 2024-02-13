package com.bossymr.rapid.language.psi.stubs;

import com.bossymr.rapid.language.psi.RapidAttributeList;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.symbol.ModuleType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.BitUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RapidAttributeListStub extends StubBase<RapidAttributeList> {

    private final int mask;

    public RapidAttributeListStub(@Nullable StubElement<?> parent, int mask) {
        super(parent, RapidStubElementTypes.ATTRIBUTE_LIST);
        this.mask = mask;
    }

    public RapidAttributeListStub(@Nullable StubElement<?> parent, @NotNull List<ModuleType> moduleTypes) {
        super(parent, RapidStubElementTypes.ATTRIBUTE_LIST);
        int mask = 0;
        for (ModuleType moduleType : moduleTypes) {
            mask = BitUtil.set(mask, getMask(moduleType), true);
        }
        this.mask = mask;
    }

    private static int getMask(@NotNull ModuleType moduleType) {
        return 1 << moduleType.ordinal();
    }

    public int getMask() {
        return mask;
    }

    public @NotNull List<ModuleType> getAttributes() {
        List<ModuleType> moduleTypes = new ArrayList<>();
        for (ModuleType moduleType : ModuleType.values()) {
            if (hasAttribute(moduleType)) {
                moduleTypes.add(moduleType);
            }
        }
        return moduleTypes;
    }

    public boolean hasAttribute(@NotNull ModuleType moduleType) {
        return BitUtil.isSet(getMask(), getMask(moduleType));
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

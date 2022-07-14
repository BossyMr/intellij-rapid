package io.github.bossymr.language.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.BitUtil;
import io.github.bossymr.language.psi.ModuleAttribute;
import io.github.bossymr.language.psi.RapidAttributeList;
import io.github.bossymr.language.psi.RapidStubElementTypes;
import io.github.bossymr.language.psi.stubs.RapidAttributeListStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
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
    public String toString() {
        return "RapidAttributeListStub[" + getMask() + "]";
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

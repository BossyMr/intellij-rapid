package io.github.bossymr.language.psi.stubs;

import com.intellij.psi.stubs.StubElement;
import io.github.bossymr.language.psi.ModuleAttribute;
import io.github.bossymr.language.psi.RapidAttributeList;

/**
 * Represents an attribute list stub, containing its attributes.
 */
public interface RapidAttributeListStub extends StubElement<RapidAttributeList> {

    /**
     * Returns the mask of attributes in this attribute list, each attribute is masked to the corresponding mask in
     * {@link Mask}.
     *
     * @return a mask of attributes in this attribute list.
     */
    int getMask();

    enum Mask {
        SYSTEM_MODULE(1), NO_VIEW(2), NO_STEP_IN(3), VIEW_ONLY(4), READ_ONLY(5);

        private final int mask;

        Mask(int index) {
            this.mask = 1 << index;
        }

        public int getMask() {
            return mask;
        }

        public static int getMask(ModuleAttribute attribute) {
            return valueOf(attribute.name()).getMask();
        }
    }
}

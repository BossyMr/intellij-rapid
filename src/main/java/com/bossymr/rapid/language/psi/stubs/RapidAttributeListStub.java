package com.bossymr.rapid.language.psi.stubs;

import com.intellij.psi.stubs.StubElement;
import com.bossymr.rapid.language.psi.ModuleAttribute;
import com.bossymr.rapid.language.psi.RapidAttributeList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Represents an attribute list stub, containing its attributes.
 */
public interface RapidAttributeListStub extends StubElement<RapidAttributeList> {

    /**
     * Returns the mask of attributes in this attribute list.
     *
     * @return a mask of attributes in this attribute list.
     */
    int getMask();

    /**
     * Returns the attributes in this attribute list.
     *
     * @return a set of the attributes in this attribute list.
     */
    Set<ModuleAttribute> getAttributes();

    /**
     * Checks if this attribute list contains the specified attribute.
     *
     * @param attribute the attribute to check.
     * @return if the attribute list contains the specified attribute.
     */
    boolean hasAttribute(@NotNull ModuleAttribute attribute);
}

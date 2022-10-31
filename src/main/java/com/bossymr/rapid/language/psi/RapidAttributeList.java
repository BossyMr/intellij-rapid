package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.symbol.RapidModule.Attribute;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface RapidAttributeList extends RapidElement {

    @NotNull Set<Attribute> getAttributes();

    boolean hasAttribute(@NotNull Attribute attribute);

    void setAttribute(@NotNull Attribute attribute, boolean setAttribute) throws UnsupportedOperationException;

}

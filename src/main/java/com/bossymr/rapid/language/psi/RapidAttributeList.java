package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.symbol.RapidModule.Attribute;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface RapidAttributeList extends RapidElement {

    @NotNull List<Attribute> getAttributes();

    boolean hasAttribute(@NotNull Attribute attribute);

    void setAttribute(@NotNull Attribute attribute, boolean setAttribute) throws UnsupportedOperationException;

}

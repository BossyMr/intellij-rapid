package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.symbol.ModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface RapidAttributeList extends RapidElement {

    @NotNull List<ModuleType> getAttributes();

    boolean hasAttribute(@NotNull ModuleType moduleType);

    void setAttribute(@NotNull ModuleType moduleType, boolean setAttribute) throws UnsupportedOperationException;

}

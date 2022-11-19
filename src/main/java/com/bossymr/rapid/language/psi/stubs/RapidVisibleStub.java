package com.bossymr.rapid.language.psi.stubs;

import com.bossymr.rapid.language.symbol.Visibility;
import org.jetbrains.annotations.NotNull;

public interface RapidVisibleStub {
    @NotNull Visibility getVisibility();
}

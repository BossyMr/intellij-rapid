package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

public interface RapidTypeStub {

    default int getDimensions() {
        return 0;
    }

    @Nullable String getType();
}

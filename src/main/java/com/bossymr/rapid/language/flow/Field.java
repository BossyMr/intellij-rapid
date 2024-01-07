package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Field {

    int getIndex();

    @NotNull RapidType getType();

    @Nullable String getName();

}

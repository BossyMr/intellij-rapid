package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface RapidAtomic extends RapidStructure {

    @Nullable RapidType getType();

    @Override
    default @NotNull Icon getIcon() {
        return RapidIcons.ATOMIC;
    }
}

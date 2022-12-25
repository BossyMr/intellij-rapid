package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface RapidAlias extends RapidStructure {

    @Nullable RapidType getType();

    @Override
    default @NotNull Icon getIcon() {
        return RapidIcons.ALIAS;
    }
}

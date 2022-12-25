package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public interface RapidRecord extends RapidStructure {

    @NotNull List<RapidComponent> getComponents();

    @Override
    default @NotNull Icon getIcon() {
        return RapidIcons.RECORD;
    }
}

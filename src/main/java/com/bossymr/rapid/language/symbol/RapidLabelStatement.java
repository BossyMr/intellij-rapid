package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.psi.RapidStatement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public interface RapidLabelStatement extends RapidStatement, RapidSymbol {

    @Override
    default @NotNull Icon getIcon() {
        return RapidIcons.LABEL;
    }
}

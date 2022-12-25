package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.symbol.RapidVariable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Represents the target variable which represents the index of a for statement.
 */
public interface RapidTargetVariable extends RapidVariable {

    @Override
    default @NotNull Icon getIcon() {
        return RapidIcons.VARIABLE;
    }

}

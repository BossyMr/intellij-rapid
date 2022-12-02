package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.symbol.RapidVariable;
import com.intellij.navigation.TargetPresentation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents the target variable which represents the index of a for statement.
 */
public interface RapidTargetVariable extends RapidVariable {

    @Override
    default @NotNull TargetPresentation getTargetPresentation() {
        return TargetPresentation.builder(Objects.requireNonNullElse(getName(), ""))
                .icon(RapidIcons.VARIABLE)
                .presentation();
    }
}

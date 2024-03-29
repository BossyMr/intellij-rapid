package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.symbol.physical.PhysicalVariable;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.platform.backend.presentation.TargetPresentation;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the target variable which represents the index of a for statement.
 */
@SuppressWarnings("UnstableApiUsage")
public interface RapidTargetVariable extends PhysicalVariable {

    @Override
    @NotNull RapidType getType();

    @Override
    default @NotNull TargetPresentation getTargetPresentation() {
        return TargetPresentation.builder(getPresentableName())
                .icon(RapidIcons.VARIABLE)
                .presentation();
    }
}

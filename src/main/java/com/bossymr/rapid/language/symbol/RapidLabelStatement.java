package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.language.psi.RapidStatement;
import com.intellij.platform.backend.presentation.TargetPresentation;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code RapidLabelStatement} is a statement which declares a label.
 */
@SuppressWarnings("UnstableApiUsage")
public interface RapidLabelStatement extends RapidStatement, RapidSymbol {

    @Override
    default @NotNull TargetPresentation getTargetPresentation() {
        return TargetPresentation.builder(getPresentableName())
                .presentation();
    }
}

package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.psi.RapidStatement;
import com.intellij.navigation.TargetPresentation;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code RapidLabelStatement} is a statement which declares a label.
 */
@SuppressWarnings("UnstableApiUsage")
public interface RapidLabelStatement extends RapidStatement, RapidSymbol {

    @Override
    default @NotNull TargetPresentation getTargetPresentation() {
        return TargetPresentation.builder(this.getName())
                .icon(RapidIcons.LABEL)
                .presentation();
    }
}

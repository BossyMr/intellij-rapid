package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import com.intellij.navigation.TargetPresentation;
import org.jetbrains.annotations.NotNull;


/**
 * A {@code RapidComponent} is a structure which represents a field in a {@link RapidRecord}.
 *
 * @see RapidRecord#getComponents()
 */
@SuppressWarnings("UnstableApiUsage")
public interface RapidComponent extends RapidSymbol, RapidVariable {


    /**
     * Returns the record in which this component is declared.
     *
     * @return the record which contains this component.
     */
    @NotNull RapidRecord getRecord();

    @Override
    default @NotNull String getCanonicalName() {
        return getRecord().getCanonicalName() + "/" + getName();
    }

    @Override
    default @NotNull TargetPresentation getTargetPresentation() {
        return TargetPresentation.builder(getPresentableName())
                .icon(RapidIcons.COMPONENT)
                .presentation();
    }
}

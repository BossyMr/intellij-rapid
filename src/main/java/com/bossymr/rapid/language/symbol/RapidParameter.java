package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import com.intellij.platform.backend.presentation.TargetPresentation;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code RapidParameter} represents a parameter.
 *
 * @see RapidRoutine#getParameters()
 */
@SuppressWarnings("UnstableApiUsage")
public interface RapidParameter extends RapidVariable {

    /**
     * Returns the parameter type.
     *
     * @return the parameter type.
     */
    @NotNull ParameterType getParameterType();

    /**
     * Returns the parameter group which contains this parameter.
     *
     * @return the parameter group which contains this parameter.
     */
    @NotNull RapidParameterGroup getParameterGroup();

    @Override
    default @NotNull String getCanonicalName() {
        return getParameterGroup().getRoutine().getCanonicalName() + "/" + getName();
    }

    @Override
    default @NotNull TargetPresentation getTargetPresentation() {
        return TargetPresentation.builder(getPresentableName())
                .icon(RapidIcons.COMPONENT)
                .presentation();
    }
}

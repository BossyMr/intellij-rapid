package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import com.intellij.navigation.TargetPresentation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public interface RapidRecord extends RapidStructure {

    @NotNull List<RapidComponent> getComponents();

    @Override
    default @NotNull TargetPresentation getTargetPresentation() {
        return TargetPresentation.builder(Objects.requireNonNullElse(getName(), ""))
                .icon(RapidIcons.RECORD)
                .presentation();
    }
}

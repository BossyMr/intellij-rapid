package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import com.intellij.navigation.TargetPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface RapidComponent extends RapidSymbol {

    @Nullable RapidType getType();

    @Override
    default @NotNull TargetPresentation getTargetPresentation() {
        return TargetPresentation.builder(Objects.requireNonNullElse(getName(), ""))
                .icon(RapidIcons.COMPONENT)
                .presentation();
    }

}

package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.language.psi.RapidStatement;
import com.intellij.navigation.TargetPresentation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface RapidLabelStatement extends RapidStatement, RapidSymbol {

    @Override
    default @NotNull TargetPresentation getTargetPresentation() {
        return TargetPresentation.builder(Objects.requireNonNullElse(getName(), ""))
                .presentation();
    }

}

package com.bossymr.rapid.ide.execution.debugger.frame;

import com.bossymr.rapid.language.symbol.RapidType;
import com.intellij.xdebugger.frame.presentation.XValuePresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidValuePresentation extends XValuePresentation {

    private final @Nullable RapidType dataType;
    private final @NotNull String value;

    public RapidValuePresentation(@Nullable RapidType dataType, @NotNull String value) {
        this.dataType = dataType;
        this.value = value;
    }

    @Override
    public @Nullable String getType() {
        return dataType != null ? dataType.getPresentableText() : null;
    }

    @Override
    public void renderValue(@NotNull XValueTextRenderer renderer) {
        if (dataType != null) {
            if (dataType.isAssignable(RapidType.NUMBER)) {
                renderer.renderNumericValue(value);
                return;
            }
            if (dataType.isAssignable(RapidType.STRING)) {
                renderer.renderStringValue(value);
                return;
            }
        }
        renderer.renderValue(value);
    }
}

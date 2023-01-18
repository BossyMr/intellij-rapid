package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidParameterGroup;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record VirtualParameterGroup(boolean isOptional,
                                    @NotNull List<VirtualParameter> parameters
) implements RapidParameterGroup {

    @Override
    public @NotNull List<VirtualParameter> getParameters() {
        return parameters();
    }
}

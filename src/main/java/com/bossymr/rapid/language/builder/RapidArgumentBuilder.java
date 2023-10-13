package com.bossymr.rapid.language.builder;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.value.Expression;
import org.jetbrains.annotations.NotNull;

public interface RapidArgumentBuilder {

    @NotNull RapidArgumentBuilder withRequiredArgument(@NotNull Expression expression);

    @NotNull RapidArgumentBuilder withOptionalArgument(@NotNull String name, @NotNull Expression expression);

    @NotNull RapidArgumentBuilder withConditionalArgument(@NotNull Argument argument, @NotNull Expression expression);

}

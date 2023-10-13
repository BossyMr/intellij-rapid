package com.bossymr.rapid.language.builder;

import com.bossymr.rapid.language.flow.value.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface RapidTestBlockBuilder {

    @NotNull RapidTestBlockBuilder withCase(@NotNull Expression expression,
                                            @NotNull Consumer<RapidCodeBlockBuilder> consumer);

    @NotNull RapidTestBlockBuilder withDefaultCase(@NotNull Consumer<RapidCodeBlockBuilder> consumer);

}

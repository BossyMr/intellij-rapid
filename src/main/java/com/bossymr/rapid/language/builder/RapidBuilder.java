package com.bossymr.rapid.language.builder;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface RapidBuilder {

    @NotNull RapidBuilder withModule(@NotNull String name,
                                     @NotNull Consumer<RapidModuleBuilder> consumer);

}

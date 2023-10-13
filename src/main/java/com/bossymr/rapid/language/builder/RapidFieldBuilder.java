package com.bossymr.rapid.language.builder;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface RapidFieldBuilder {

    @NotNull RapidFieldBuilder withInitializer(@NotNull Consumer<RapidCodeBuilder> consumer);

}

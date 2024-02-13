package com.bossymr.rapid.language.builder;

import com.bossymr.rapid.language.symbol.RapidModule;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A builder for {@code Rapid} models.
 */
public interface RapidBuilder {

    /**
     * Adds a module to this model.
     *
     * @param name the name of the module.
     * @param consumer the handler which can define the module.
     * @return this builder.
     */
    @NotNull RapidBuilder withModule(@NotNull String name,
                                     @NotNull Consumer<RapidModuleBuilder> consumer);

    /**
     * Adds the specified module to this model.
     *
     * @param module the module.
     * @return this builder.
     */
    @NotNull RapidBuilder withModule(@NotNull RapidModule module);

}

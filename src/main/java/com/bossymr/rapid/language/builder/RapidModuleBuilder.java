package com.bossymr.rapid.language.builder;

import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.RapidField;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A builder for {@code Rapid} modules.
 */
public interface RapidModuleBuilder {

    /**
     * Adds a field to this module.
     *
     * @param name the name of the field.
     * @param fieldType the type of the field.
     * @param valueType the value type of the field.
     * @param consumer the handler which can initialize the field.
     * @return this builder.
     */
    @NotNull RapidModuleBuilder withField(@NotNull String name,
                                          @NotNull FieldType fieldType,
                                          @NotNull RapidType valueType,
                                          @NotNull Consumer<RapidCodeBlockBuilder> consumer);

    /**
     * Adds the specified field to this module.
     *
     * @param field the field.
     * @return this builder.
     */
    @NotNull RapidModuleBuilder withField(@NotNull RapidField field);

    /**
     * Adds a function to this module.
     *
     * @param name the name of the function.
     * @param returnType the return type of the function.
     * @param consumer the handler which can define the routine.
     * @return this builder.
     */
    default @NotNull RapidModuleBuilder withFunction(@NotNull String name,
                                                     @NotNull RapidType returnType,
                                                     @NotNull Consumer<RapidRoutineBuilder> consumer) {
        return withRoutine(name, RoutineType.FUNCTION, returnType, consumer);
    }

    /**
     * Adds a procedure to this module.
     *
     * @param name the name of the procedure.
     * @param consumer the handler which can define the procedure.
     * @return this builder.
     */
    default @NotNull RapidModuleBuilder withProcedure(@NotNull String name,
                                                      @NotNull Consumer<RapidRoutineBuilder> consumer) {
        return withRoutine(name, RoutineType.PROCEDURE, null, consumer);
    }

    /**
     * Adds a trap routine to this module.
     *
     * @param name the name of the trap routine.
     * @param consumer the handler which can define the trap routine.
     * @return this builder.
     */
    default @NotNull RapidModuleBuilder withTrap(@NotNull String name,
                                                 @NotNull Consumer<RapidRoutineBuilder> consumer) {
        return withRoutine(name, RoutineType.PROCEDURE, null, consumer);
    }

    /**
     * Adds a routine to this module.
     *
     * @param name the name of the routine.
     * @param routineType the type of the routine.
     * @param returnType the return type of the routine.
     * @param consumer the handler which can define the routine.
     * @return this builder.
     */
    @NotNull RapidModuleBuilder withRoutine(@NotNull String name,
                                            @NotNull RoutineType routineType,
                                            @Nullable RapidType returnType,
                                            @NotNull Consumer<RapidRoutineBuilder> consumer);

    /**
     * Adds the specified routine to this module.
     *
     * @param routine the routine.
     * @return this builder.
     */
    @NotNull RapidModuleBuilder withRoutine(@NotNull RapidRoutine routine);
}

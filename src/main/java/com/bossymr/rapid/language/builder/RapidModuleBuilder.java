package com.bossymr.rapid.language.builder;

import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.RapidField;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface RapidModuleBuilder {

    @NotNull
    default RapidModuleBuilder withField(@NotNull String name,
                                         @NotNull FieldType fieldType,
                                         @NotNull RapidType valueType,
                                         @NotNull Consumer<RapidFieldBuilder> consumer) {
        return withField(null, name, fieldType, valueType, consumer);
    }

    @NotNull RapidModuleBuilder withField(@Nullable RapidField element,
                                          @NotNull String name,
                                          @NotNull FieldType fieldType,
                                          @NotNull RapidType valueType,
                                          @NotNull Consumer<RapidFieldBuilder> consumer);

    @NotNull
    default RapidModuleBuilder withRoutine(@NotNull String name,
                                           @NotNull RoutineType routineType,
                                           @Nullable RapidType returnType,
                                           @NotNull Consumer<RapidRoutineBuilder> consumer) {
        return withRoutine(null, name, routineType, returnType, consumer);
    }

    @NotNull RapidModuleBuilder withRoutine(@Nullable RapidRoutine element,
                                            @NotNull String name,
                                            @NotNull RoutineType routineType,
                                            @Nullable RapidType returnType,
                                            @NotNull Consumer<RapidRoutineBuilder> consumer);
}

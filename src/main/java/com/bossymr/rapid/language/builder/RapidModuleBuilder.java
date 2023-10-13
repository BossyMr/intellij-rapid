package com.bossymr.rapid.language.builder;

import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.RapidField;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface RapidModuleBuilder {

    default @NotNull RapidModuleBuilder withPersistentField(@NotNull RapidField element,
                                                            @NotNull String name,
                                                            @NotNull RapidType valueType,
                                                            @NotNull Consumer<RapidFieldBuilder> consumer) {
        return withField(element, name, FieldType.PERSISTENT, valueType, consumer);
    }

    default @NotNull RapidModuleBuilder withVariableField(@NotNull RapidField element,
                                                          @NotNull String name,
                                                          @NotNull RapidType valueType,
                                                          @NotNull Consumer<RapidFieldBuilder> consumer) {
        return withField(element, name, FieldType.VARIABLE, valueType, consumer);
    }

    default @NotNull RapidModuleBuilder withConstantField(@NotNull RapidField element,
                                                          @NotNull String name,
                                                          @NotNull RapidType valueType,
                                                          @NotNull Consumer<RapidFieldBuilder> consumer) {
        return withField(element, name, FieldType.CONSTANT, valueType, consumer);
    }

    @NotNull RapidModuleBuilder withField(@NotNull RapidField element,
                                          @NotNull String name,
                                          @NotNull FieldType fieldType,
                                          @NotNull RapidType valueType,
                                          @NotNull Consumer<RapidFieldBuilder> consumer);


    @NotNull
    default RapidModuleBuilder withProcedure(@NotNull RapidField element,
                                             @NotNull String name,
                                             @NotNull Consumer<RapidRoutineBuilder> consumer) {
        return withRoutine(element, name, RoutineType.PROCEDURE, null, consumer);
    }

    @NotNull
    default RapidModuleBuilder withTrap(@NotNull RapidField element,
                                        @NotNull String name,
                                        @NotNull Consumer<RapidRoutineBuilder> consumer) {
        return withRoutine(element, name, RoutineType.TRAP, null, consumer);
    }

    @NotNull
    default RapidModuleBuilder withFunction(@NotNull RapidField element,
                                            @NotNull String name,
                                            @NotNull RapidType returnType,
                                            @NotNull Consumer<RapidRoutineBuilder> consumer) {
        return withRoutine(element, name, RoutineType.PROCEDURE, returnType, consumer);
    }

    @NotNull RapidModuleBuilder withRoutine(@NotNull RapidField element,
                                            @NotNull String name,
                                            @NotNull RoutineType routineType,
                                            @Nullable RapidType returnType,
                                            @NotNull Consumer<RapidRoutineBuilder> consumer);
}

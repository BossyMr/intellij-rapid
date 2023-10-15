package com.bossymr.rapid.language.builder;

import com.bossymr.rapid.language.psi.StatementListType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface RapidRoutineBuilder {

    @NotNull RapidRoutineBuilder withParameterGroup(boolean isOptional, @NotNull Consumer<RapidParameterGroupBuilder> consumer);

    default @NotNull RapidRoutineBuilder withCode(@NotNull Consumer<RapidCodeBlockBuilder> consumer) {
        return withCode(StatementListType.STATEMENT_LIST, consumer);
    }

    @NotNull RapidRoutineBuilder withCode(@NotNull StatementListType codeType,
                                          @NotNull Consumer<RapidCodeBlockBuilder> consumer);

    @NotNull RapidRoutineBuilder withCode(@Nullable List<Integer> exceptions,
                                          @NotNull Consumer<RapidCodeBlockBuilder> consumer);
}

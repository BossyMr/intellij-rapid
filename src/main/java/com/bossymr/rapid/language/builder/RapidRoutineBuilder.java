package com.bossymr.rapid.language.builder;

import com.bossymr.rapid.language.psi.StatementListType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface RapidRoutineBuilder {

    @NotNull RapidRoutineBuilder withParameterGroup(boolean isOptional, @NotNull Consumer<RapidParameterGroupBuilder> consumer);

    default @NotNull RapidRoutineBuilder withCode(@NotNull Consumer<RapidCodeBlockBuilder> consumer) {
        return withCode(StatementListType.STATEMENT_LIST, consumer);
    }

    @NotNull RapidRoutineBuilder withCode(@NotNull StatementListType codeType,
                                          @NotNull Consumer<RapidCodeBlockBuilder> consumer);
}

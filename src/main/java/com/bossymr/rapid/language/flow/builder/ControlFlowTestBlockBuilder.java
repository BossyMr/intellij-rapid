package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.RapidCodeBlockBuilder;
import com.bossymr.rapid.language.builder.RapidTestBlockBuilder;
import com.bossymr.rapid.language.flow.expression.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ControlFlowTestBlockBuilder implements RapidTestBlockBuilder {

    private final @NotNull Expression condition;
    private final @NotNull List<Map.Entry<List<Expression>, Consumer<RapidCodeBlockBuilder>>> cases;

    public ControlFlowTestBlockBuilder(@NotNull Expression condition, @NotNull List<Map.Entry<List<Expression>, Consumer<RapidCodeBlockBuilder>>> cases) {
        this.condition = condition;
        this.cases = cases;
    }

    @Override
    public @NotNull RapidTestBlockBuilder withCase(@NotNull List<Expression> conditions, @NotNull Consumer<RapidCodeBlockBuilder> consumer) {
        if (conditions.isEmpty()) {
            return this;
        }
        if (conditions.stream().anyMatch(expression -> !(condition.getType().isAssignable(expression.getType())))) {
            return this;
        }
        cases.add(new AbstractMap.SimpleImmutableEntry<>(conditions, consumer));
        return this;
    }

    @Override
    public @NotNull RapidTestBlockBuilder withDefaultCase(@NotNull Consumer<RapidCodeBlockBuilder> consumer) {
        cases.add(new AbstractMap.SimpleImmutableEntry<>(null, consumer));
        return this;
    }
}

package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.RapidCodeBlockBuilder;
import com.bossymr.rapid.language.builder.RapidTestBlockBuilder;
import com.bossymr.rapid.language.flow.value.BinaryOperator;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.psi.RapidTestStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ControlFlowTestBlockBuilder implements RapidTestBlockBuilder {

    private final @Nullable RapidTestStatement statement;
    private final @NotNull Expression condition;
    private @Nullable ControlFlowCodeBlockBuilder builder;

    public ControlFlowTestBlockBuilder(@Nullable RapidTestStatement statement, @NotNull ControlFlowCodeBlockBuilder builder, @NotNull Expression condition) {
        this.statement = statement;
        this.builder = builder;
        this.condition = condition;
    }

    @Override
    public @NotNull RapidTestBlockBuilder withCase(@NotNull List<Expression> conditions, @NotNull Consumer<RapidCodeBlockBuilder> consumer) {
        if (conditions.isEmpty() || builder == null) {
            return this;
        }
        List<Expression> equality = conditions.stream().map(expr -> builder.binary(BinaryOperator.EQUAL_TO, condition, expr)).toList();
        Expression expression = equality.get(0);
        for (int i = 1; i < equality.size(); i++) {
            expression = builder.binary(BinaryOperator.OR, expression, equality.get(i));
        }
        builder.ifThenElse(statement, expression, consumer, elseBuilder -> builder = (ControlFlowCodeBlockBuilder) elseBuilder);
        return this;
    }

    @Override
    public @NotNull RapidTestBlockBuilder withDefaultCase(@NotNull Consumer<RapidCodeBlockBuilder> consumer) {
        consumer.accept(builder);
        builder = null;
        return this;
    }
}

package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.ArgumentDescriptor;
import com.bossymr.rapid.language.builder.RapidArgumentBuilder;
import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.expression.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ControlFlowArgumentBuilder implements RapidArgumentBuilder {

    private final @NotNull Map<ArgumentDescriptor, Expression> arguments;
    private final @NotNull ControlFlowCodeBlockBuilder builder;
    private final @NotNull AtomicInteger index = new AtomicInteger();

    public ControlFlowArgumentBuilder(@NotNull Map<ArgumentDescriptor, Expression> arguments, @NotNull ControlFlowCodeBlockBuilder builder) {
        this.arguments = arguments;
        this.builder = builder;
    }

    @Override
    public @NotNull RapidArgumentBuilder withRequiredArgument(@NotNull Expression expression) {
        arguments.put(new ArgumentDescriptor.Required(index.getAndIncrement()), expression);
        return this;
    }

    @Override
    public @NotNull RapidArgumentBuilder withOptionalArgument(@NotNull String name, @NotNull Expression expression) {
        arguments.put(new ArgumentDescriptor.Optional(name), expression);
        return this;
    }

    @Override
    public @NotNull RapidArgumentBuilder withConditionalArgument(@NotNull String name, @NotNull Argument argument) {
        arguments.put(new ArgumentDescriptor.Conditional(name), builder.getReference(argument));
        return this;
    }
}

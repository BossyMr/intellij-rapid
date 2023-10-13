package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.ArgumentDescriptor;
import com.bossymr.rapid.language.builder.RapidArgumentBuilder;
import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.value.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ControlFlowArgumentBuilder implements RapidArgumentBuilder {

    private final @NotNull Map<ArgumentDescriptor, Expression> arguments;
    private int index = 0;

    public ControlFlowArgumentBuilder(@NotNull Map<ArgumentDescriptor, Expression> arguments) {
        this.arguments = arguments;
    }

    @Override
    public @NotNull RapidArgumentBuilder withRequiredArgument(@NotNull Expression expression) {
        arguments.put(new ArgumentDescriptor.Required(index), expression);
        index += 1;
        return this;
    }

    @Override
    public @NotNull RapidArgumentBuilder withOptionalArgument(@NotNull String name, @NotNull Expression expression) {
        arguments.put(new ArgumentDescriptor.Optional(name), expression);
        return this;
    }

    @Override
    public @NotNull RapidArgumentBuilder withConditionalArgument(@NotNull Argument argument, @NotNull Expression expression) {
        arguments.put(new ArgumentDescriptor.Conditional(argument.getName()), expression);
        return this;
    }
}

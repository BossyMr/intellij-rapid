package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.ArgumentDescriptor;
import com.bossymr.rapid.language.builder.Label;
import com.bossymr.rapid.language.builder.RapidArgumentBuilder;
import com.bossymr.rapid.language.builder.RapidCodeBlockBuilder;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.instruction.*;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.psi.RapidElement;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ControlFlowCodeBlockBuilder extends ControlFlowCodeBuilder implements RapidCodeBlockBuilder {

    private final @NotNull Map<String, ControlFlowLabel> labels = new HashMap<>();

    public ControlFlowCodeBlockBuilder(@NotNull Block block, @NotNull ControlFlowBlockBuilder builder) {
        super(block, builder);
    }

    @Override
    public @NotNull Label createLabel(@Nullable String name) {
        if (name != null && labels.containsKey(name)) {
            ControlFlowLabel label = labels.get(name);
            builder.getLabels().add(label);
            return label;
        }
        ControlFlowLabel instruction = new ControlFlowLabel(name);
        if (name != null) {
            labels.put(name, instruction);
        }
        builder.getLabels().add(instruction);
        return instruction;
    }

    @Override
    public @NotNull Label getLabel(@NotNull String name) {
        if (labels.containsKey(name)) {
            return labels.get(name);
        }
        ControlFlowLabel instruction = new ControlFlowLabel(name);
        labels.put(name, instruction);
        return instruction;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder error(@Nullable RapidElement element) {
        builder.continueScope(new ErrorInstruction(block, element));
        return this;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder ifThen(@Nullable RapidElement element, @NotNull Expression expression, @NotNull Consumer<RapidCodeBlockBuilder> thenConsumer) {
        return ifThenElse(element, expression, thenConsumer, builder -> {});
    }

    @Override
    public @NotNull RapidCodeBlockBuilder ifThenElse(@Nullable RapidElement element, @NotNull Expression expression, @NotNull Consumer<RapidCodeBlockBuilder> thenConsumer, @NotNull Consumer<RapidCodeBlockBuilder> elseConsumer) {
        ConditionalBranchingInstruction instruction = new ConditionalBranchingInstruction(block, element, expression);
        builder.continueScope(instruction);
        builder.exitScope();

        ControlFlowBlockBuilder.Scope thenScope = builder.enterScope();
        thenScope.getPredecessors().add(instruction);
        thenConsumer.accept(this);
        thenScope = builder.exitScope();

        ControlFlowBlockBuilder.Scope elseScope = builder.enterScope();
        elseScope.getPredecessors().add(instruction);
        elseConsumer.accept(this);
        elseScope = builder.exitScope();

        if (thenScope == null || elseScope == null) {
            return this;
        }
        ControlFlowBlockBuilder.Scope nextScope = builder.enterScope();
        if(thenScope.getTail() != null) {
            nextScope.getPredecessors().add(thenScope.getTail());
        } else {
            nextScope.getPredecessors().add(instruction);
        }
        if(elseScope.getTail() != null) {
            nextScope.getPredecessors().add(elseScope.getTail());
        } else {
            nextScope.getPredecessors().add(instruction);
        }
        return this;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder goTo(@Nullable RapidElement element, @NotNull Label label) {
        if (!(label instanceof ControlFlowLabel instruction)) {
            throw new IllegalArgumentException();
        }
        builder.continueScope(new UnconditionalBranchingInstruction(block, element, instruction));
        builder.exitScope();
        return this;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder throwException(@Nullable RapidElement element, @Nullable Expression expression) {
        builder.continueScope(new ThrowInstruction(block, element, expression));
        builder.exitScope();
        return this;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder tryNextInstruction(@Nullable RapidElement element) {
        builder.continueScope(new TryNextInstruction(block, element));
        builder.exitScope();
        return this;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder exit(@Nullable RapidElement element) {
        builder.continueScope(new ExitInstruction(block, element));
        builder.exitScope();
        return this;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder retryInstruction(@Nullable RapidElement element) {
        builder.continueScope(new RetryInstruction(block, element));
        builder.exitScope();
        return this;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder assign(@Nullable RapidElement element, @NotNull ReferenceExpression variable, @NotNull Expression expression) {
        builder.continueScope(new AssignmentInstruction(block, element, variable, expression));
        return this;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder connect(@Nullable RapidElement element, @NotNull ReferenceExpression variable, @NotNull Expression expression) {
        builder.continueScope(new ConnectInstruction(block, element, variable, expression));
        return this;
    }

    @Override
    public @NotNull Expression call(@Nullable RapidElement element, @NotNull Expression routine, @NotNull RapidType returnType, @NotNull Consumer<RapidArgumentBuilder> arguments) {
        Map<ArgumentDescriptor, Expression> result = new HashMap<>();
        ControlFlowArgumentBuilder builder = new ControlFlowArgumentBuilder(result);
        arguments.accept(builder);
        ReferenceExpression variable = createVariable(returnType);
        call(element, routine, variable, result);
        return variable;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder invoke(@Nullable RapidElement element, @NotNull Expression routine, @NotNull Consumer<RapidArgumentBuilder> arguments) {
        Map<ArgumentDescriptor, Expression> result = new HashMap<>();
        ControlFlowArgumentBuilder builder = new ControlFlowArgumentBuilder(result);
        arguments.accept(builder);
        call(element, routine, null, result);
        return this;
    }

    private void call(@Nullable RapidElement element, @NotNull Expression routine, @Nullable ReferenceExpression returnVariable, @NotNull Map<ArgumentDescriptor, Expression> arguments) {
        Optional<ArgumentDescriptor.Conditional> optional = getConditionalArgument(arguments);
        if (optional.isEmpty()) {
            builder.continueScope(new CallInstruction(block, element, routine, returnVariable, getArgumentVariables(arguments)));
            return;
        }
        ArgumentDescriptor.Conditional conditional = optional.orElseThrow();
        ReferenceExpression argument = getArgument(conditional.name());
        Expression isPresent = call(":Present", RapidPrimitiveType.BOOLEAN, builder -> builder.withRequiredArgument(argument));
        ifThenElse(isPresent,
                builder -> {
                    Expression expression = arguments.get(conditional);
                    if (expression == null) {
                        error(null);
                        return;
                    }
                    Map<ArgumentDescriptor, Expression> copy = new HashMap<>(arguments);
                    copy.remove(conditional);
                    copy.put(new ArgumentDescriptor.Optional(conditional.name()), expression);
                    call(element, routine, returnVariable, copy);
                }, builder -> {
                    Map<ArgumentDescriptor, Expression> copy = new HashMap<>(arguments);
                    copy.remove(conditional);
                    call(element, routine, returnVariable, copy);
                });
    }


    private @NotNull Map<ArgumentDescriptor, ReferenceExpression> getArgumentVariables(@NotNull Map<ArgumentDescriptor, Expression> arguments) {
        Map<ArgumentDescriptor, ReferenceExpression> variables = new HashMap<>();
        arguments.forEach((descriptor, expression) -> {
            if (expression instanceof ReferenceExpression referenceExpression) {
                variables.put(descriptor, referenceExpression);
            } else {
                ReferenceExpression variable = createVariable(expression.getType());
                assign(variable, expression);
                variables.put(descriptor, variable);
            }
        });
        return variables;
    }

    private @NotNull Optional<ArgumentDescriptor.Conditional> getConditionalArgument(@NotNull Map<ArgumentDescriptor, ?> arguments) {
        return arguments.keySet().stream()
                .filter(argument -> argument instanceof ArgumentDescriptor.Conditional)
                .map(argument -> (ArgumentDescriptor.Conditional) argument)
                .findFirst();
    }
}

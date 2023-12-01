package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.ArgumentDescriptor;
import com.bossymr.rapid.language.builder.Label;
import com.bossymr.rapid.language.builder.RapidArgumentBuilder;
import com.bossymr.rapid.language.builder.RapidCodeBlockBuilder;
import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.Variable;
import com.bossymr.rapid.language.flow.data.snapshots.ErrorExpression;
import com.bossymr.rapid.language.flow.instruction.*;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class ControlFlowCodeBlockBuilder implements RapidCodeBlockBuilder {

    protected final @NotNull Block block;
    protected final @NotNull ControlFlowBlockBuilder builder;
    private final @NotNull Map<String, ControlFlowLabel> labels = new HashMap<>();

    public ControlFlowCodeBlockBuilder(@NotNull Block block, @NotNull ControlFlowBlockBuilder builder) {
        this.block = block;
        this.builder = builder;
    }

    @Override
    public @NotNull ReferenceExpression createVariable(@Nullable String name,
                                                       @Nullable FieldType fieldType,
                                                       @NotNull RapidType type) {
        Variable variable = block.createVariable(name, fieldType, type);
        return new VariableExpression(variable);
    }

    @Override
    public @NotNull ReferenceExpression getVariable(@Nullable RapidReferenceExpression expression, @NotNull String name) {
        Variable variable = block.findVariable(name);
        if (variable == null) {
            return new ErrorExpression(RapidPrimitiveType.ANYTYPE);
        }
        return builder.getSnapshot(variable);
    }

    @Override
    public @NotNull ReferenceExpression getArgument(@Nullable RapidReferenceExpression expression, @NotNull String name) {
        Argument argument = block.findArgument(name);
        if (argument == null) {
            return new ErrorExpression(RapidPrimitiveType.ANYTYPE);
        }
        return builder.getSnapshot(argument);
    }

    @Override
    public @NotNull ReferenceExpression getField(@Nullable RapidReferenceExpression expression, @NotNull String moduleName, @NotNull String name, @NotNull RapidType valueType) {
        return new FieldExpression(expression, valueType, moduleName, name);
    }

    @Override
    public @NotNull IndexExpression index(@Nullable RapidIndexExpression element, @NotNull ReferenceExpression variable, @NotNull Expression index) {
        if (variable.getType().getDimensions() < 1) {
            throw new IllegalArgumentException();
        }
        if (!(index.getType().isAssignable(RapidPrimitiveType.NUMBER))) {
            throw new IllegalArgumentException();
        }
        return new IndexExpression(element, variable, index);
    }

    @Override
    public @NotNull Expression component(@Nullable RapidReferenceExpression element, @NotNull RapidType type, @NotNull ReferenceExpression variable, @NotNull String name) {
        return new ComponentExpression(element, type, variable, name);
    }

    @Override
    public @NotNull Expression aggregate(@Nullable RapidAggregateExpression element, @NotNull RapidType aggregateType, @NotNull List<? extends Expression> expressions) {
        return new AggregateExpression(element, aggregateType, expressions);
    }

    @Override
    public @NotNull Expression literal(@Nullable RapidLiteralExpression element, @NotNull Object value) {
        return new LiteralExpression(element, value);
    }

    @Override
    public @NotNull Expression binary(@Nullable RapidBinaryExpression element, @NotNull BinaryOperator operator, @NotNull Expression left, @NotNull Expression right) {
        return new BinaryExpression(element, operator, left, right);
    }

    @Override
    public @NotNull Expression unary(@Nullable RapidUnaryExpression element, @NotNull UnaryOperator operator, @NotNull Expression expression) {
        return new UnaryExpression(element, operator, expression);
    }

    @Override
    public @NotNull Expression error(@Nullable RapidElement element, @NotNull RapidType type) {
        return new ErrorExpression(type);
    }

    @Override
    public void returnValue(@Nullable RapidReturnStatement statement, @Nullable Expression expression) {
        builder.continueScope(new ReturnInstruction(block, statement, expression));
        builder.exitScope();
    }


    @Override
    public @NotNull Label createLabel(@Nullable String name) {
        if (!(builder.isInScope())) {
            builder.enterScope();
        }
        if (name != null && labels.containsKey(name)) {
            ControlFlowLabel label = labels.get(name);
            builder.addCommand(label::setInstruction);
            return label;
        }
        ControlFlowLabel label = new ControlFlowLabel(name);
        if (name != null) {
            labels.put(name, label);
        }
        builder.addCommand(label::setInstruction);
        return label;
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
        return ifThenElse(element, expression, thenConsumer, builder -> {
        });
    }

    @Override
    public @NotNull RapidCodeBlockBuilder ifThenElse(@Nullable RapidElement element, @NotNull Expression expression, @NotNull Consumer<RapidCodeBlockBuilder> thenConsumer, @NotNull Consumer<RapidCodeBlockBuilder> elseConsumer) {
        ConditionalBranchingInstruction instruction = new ConditionalBranchingInstruction(block, element, expression);
        builder.continueScope(instruction);
        ControlFlowBlockBuilder.Scope scope = builder.exitScope();
        if (scope == null) {
            return this;
        }

        // Visit the "then" branch
        builder.enterScope(scope.copy());
        thenConsumer.accept(this);
        ControlFlowBlockBuilder.Scope thenScope = builder.exitScope();

        List<Instruction> successors = instruction.getSuccessors();
        if (successors.isEmpty()) {
            successors.add(null);
        }

        // Visit the "else" branch
        builder.enterScope(scope.copy());
        elseConsumer.accept(this);
        ControlFlowBlockBuilder.Scope elseScope = builder.exitScope();

        if (thenScope == null && elseScope == null) {
            return this;
        }

        if (successors.size() == 1 && successors.get(0) == null) {
            // Both the "else" and "then" branch are empty
            successors.remove(0);
        }

        if (successors.size() == 2 && successors.get(0) == null) {
            // The "then" branch is empty
            Objects.requireNonNull(thenScope).commands().addLast(nextInstruction -> {
                successors.remove(successors.size() - 1);
                successors.set(0, nextInstruction);
                return true;
            });
        }

        if (thenScope != null) builder.enterScope(thenScope);
        if (elseScope != null) builder.enterScope(elseScope);
        return this;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder loop(@Nullable RapidElement element, @NotNull Expression expression, @NotNull Consumer<RapidCodeBlockBuilder> consumer) {
        Label label = createLabel();
        ifThen(element, expression, thenConsumer -> {
            consumer.accept(thenConsumer);
            thenConsumer.goTo(label);
        });
        return this;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder goTo(@Nullable RapidElement element, @NotNull Label label) {
        if (!(label instanceof ControlFlowLabel instruction)) {
            throw new IllegalArgumentException();
        }
        builder.goTo(instruction);
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
        builder.continueScope(new AssignmentInstruction(block, element, builder.createSnapshot(variable), expression));
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
        ControlFlowArgumentBuilder builder = new ControlFlowArgumentBuilder(result, this);
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
        Expression isPresent = new UnaryExpression(UnaryOperator.PRESENT, argument);
        ifThenElse(isPresent,
                builder -> {
                    Expression expression = arguments.get(conditional);
                    if (expression == null) {
                        builder.error(null);
                        return;
                    }
                    Map<ArgumentDescriptor, Expression> copy = new HashMap<>(arguments);
                    copy.remove(conditional);
                    copy.put(new ArgumentDescriptor.Optional(conditional.name()), expression);
                    ((ControlFlowCodeBlockBuilder) builder).call(element, routine, returnVariable, copy);
                }, builder -> {
                    Map<ArgumentDescriptor, Expression> copy = new HashMap<>(arguments);
                    copy.remove(conditional);
                    ((ControlFlowCodeBlockBuilder) builder).call(element, routine, returnVariable, copy);
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

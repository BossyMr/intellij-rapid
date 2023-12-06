package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.ArgumentDescriptor;
import com.bossymr.rapid.language.builder.Label;
import com.bossymr.rapid.language.builder.RapidArgumentBuilder;
import com.bossymr.rapid.language.builder.RapidCodeBlockBuilder;
import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.Field;
import com.bossymr.rapid.language.flow.Variable;
import com.bossymr.rapid.language.flow.data.snapshots.ErrorExpression;
import com.bossymr.rapid.language.flow.instruction.*;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ControlFlowCodeBlockBuilder implements RapidCodeBlockBuilder {

    protected final @NotNull Block block;
    protected final @NotNull ControlFlowBlockBuilder builder;
    private final @NotNull Map<String, ControlFlowLabel> labels = new HashMap<>();

    public ControlFlowCodeBlockBuilder(@NotNull Block block, @NotNull ControlFlowBlockBuilder builder) {
        this.block = block;
        this.builder = builder;
    }

    @Override
    public @NotNull Variable createVariable(@NotNull RapidType type) {
        return block.createVariable(null, null, type);
    }

    @Override
    public @NotNull Variable createVariable(@NotNull RapidField field) {
        RapidType type = Objects.requireNonNullElse(field.getType(), RapidPrimitiveType.ANYTYPE);
        return block.createVariable(field.getName(), field.getFieldType(), type);
    }

    @Override
    public @Nullable Argument getArgument(@NotNull String name) {
        return null;
    }

    @Override
    public @NotNull ReferenceExpression getReference(@NotNull Field field) {
        return new VariableExpression(field);
    }

    @Override
    public @Nullable ReferenceExpression getReference(@NotNull RapidReferenceExpression expression) {
        if (expression.getQualifier() != null) {
            return component(expression);
        }
        RapidSymbol symbol = expression.getSymbol();
        if (symbol == null) {
            return null;
        }
        if (symbol instanceof RapidField field) {
            return getFieldReference(expression, field);
        }
        if (symbol instanceof RapidParameter parameter) {
            return getVariableReference(expression, parameter, block::findArgument);
        }
        if (symbol instanceof RapidTargetVariable variable) {
            return getVariableReference(expression, variable, block::findVariable);
        }
        return null;
    }

    private @Nullable ReferenceExpression getVariableReference(@NotNull RapidReferenceExpression expression, @NotNull RapidVariable parameter, @NotNull Function<@NotNull String, @Nullable Field> function) {
        String name = parameter.getName();
        if (name == null) {
            return null;
        }
        Field field = function.apply(name);
        if (field == null) {
            return null;
        }
        return new VariableExpression(expression, field);
    }

    private @Nullable ReferenceExpression getFieldReference(@NotNull RapidReferenceExpression expression, @NotNull RapidField field) {
        String name = field.getName();
        if (name == null) {
            return null;
        }
        RapidType type = Objects.requireNonNullElse(field.getType(), RapidPrimitiveType.ANYTYPE);
        if (field instanceof PhysicalField physicalField) {
            PhysicalRoutine routine = PhysicalRoutine.getRoutine(physicalField);
            if (routine != null) {
                Variable variable = block.findVariable(name);
                if (variable == null) {
                    return null;
                }
                return new VariableExpression(expression, variable);
            }
            PhysicalModule module = PhysicalModule.getModule(physicalField);
            String moduleName;
            if (module == null || (moduleName = module.getName()) == null) {
                return null;
            }
            return new FieldExpression(expression, type, moduleName, name);
        }
        return new FieldExpression(expression, type, "", name);
    }

    @Override
    public @NotNull ReferenceExpression getReference(@NotNull RapidType type, @NotNull String moduleName, @NotNull String name) {
        return new FieldExpression(type, moduleName, name);
    }

    @Override
    public @NotNull ReferenceExpression index(@NotNull ReferenceExpression variable, @NotNull Expression index) {
        if (variable.getType().getDimensions() <= 0) {
            throw new IllegalArgumentException("Cannot create index expression for variable of type: " + variable.getType());
        }
        if (!(index.getType().isAssignable(RapidPrimitiveType.NUMBER))) {
            throw new IllegalArgumentException("Cannot reference index of type: " + index.getType());
        }
        return new IndexExpression(variable, index);
    }

    @Override
    public @Nullable ReferenceExpression index(@NotNull RapidIndexExpression expression) {
        if (!(expression.getExpression() instanceof RapidReferenceExpression referenceExpression)) {
            return null;
        }
        ReferenceExpression variable = getReference(referenceExpression);
        if (variable == null) {
            return null;
        }
        List<RapidExpression> dimensions = expression.getArray().getDimensions();
        if (dimensions.isEmpty()) {
            return null;
        }
        RapidType type = Objects.requireNonNullElseGet(variable.getType(), () -> RapidPrimitiveType.ANYTYPE.createArrayType(dimensions.size()));
        if (type.getDimensions() < dimensions.size()) {
            return null;
        }
        List<Expression> expressions = dimensions.stream()
                .map(this::expression)
                .toList();
        if (expressions.contains(null)) {
            return null;
        }
        IndexExpression indexExpression = new IndexExpression(expression, variable, expressions.get(0));
        for (int i = 1; i < expressions.size(); i++) {
            indexExpression = new IndexExpression(expression, indexExpression, expressions.get(i));
        }
        return indexExpression;
    }

    @Override
    public @NotNull ReferenceExpression component(@NotNull ReferenceExpression variable, @NotNull String name) {
        RapidType type = variable.getType();
        if (!(type.getRootStructure() instanceof RapidRecord record)) {
            throw new IllegalArgumentException("Cannot create component expression for variable of type: " + type);
        }
        for (RapidComponent component : record.getComponents()) {
            if (name.equals(component.getName())) {
                return new ComponentExpression(Objects.requireNonNullElse(component.getType(), RapidPrimitiveType.ANYTYPE), variable, name);
            }
        }
        throw new IllegalArgumentException("Record: " + record.getName() + " does not have a component: " + name);
    }

    @Override
    public @Nullable ReferenceExpression component(@NotNull RapidReferenceExpression expression) {
        RapidExpression qualifier = expression.getQualifier();
        if (!(qualifier instanceof RapidReferenceExpression referenceExpression)) {
            return null;
        }
        ReferenceExpression variable = getReference(referenceExpression);
        if (variable == null) {
            return null;
        }
        RapidSymbol symbol = expression.getSymbol();
        if (!(symbol instanceof RapidComponent component)) {
            return null;
        }
        String name = symbol.getName();
        if (name == null) {
            return null;
        }
        RapidType type = Objects.requireNonNullElse(component.getType(), RapidPrimitiveType.ANYTYPE);
        return new ComponentExpression(expression, type, variable, name);
    }

    @Override
    public @NotNull Expression aggregate(@NotNull RapidType aggregateType, @NotNull List<? extends Expression> expressions) {
        return new AggregateExpression(aggregateType, expressions);
    }

    @Override
    public @Nullable Expression aggregate(@NotNull RapidAggregateExpression expression) {
        RapidType type = expression.getType();
        if (type == null) {
            return null;
        }
        List<Expression> expressions = expression.getExpressions().stream()
                .map(this::expression)
                .toList();
        if (type.getDimensions() > 0) {
            if (expressions.stream().anyMatch(component -> !(type.isAssignable(component.getType().createArrayType(1))))) {
                return null;
            }
        } else if (type.getRootStructure() instanceof RapidRecord record) {
            List<RapidComponent> components = record.getComponents();
            for (int i = 0; i < components.size(); i++) {
                if (i >= expressions.size()) {
                    return null;
                }
                RapidType componentType = components.get(i).getType();
                if (componentType == null) {
                    continue;
                }
                if (!(componentType.isAssignable(expressions.get(i).getType()))) {
                    return null;
                }
            }
        }
        return new AggregateExpression(expression, type, expressions);
    }

    @Override
    public @NotNull Expression literal(@NotNull Object value) {
        return new LiteralExpression(value);
    }

    @Override
    public @Nullable Expression literal(@NotNull RapidLiteralExpression expression) {
        Object value = expression.getValue();
        if (value == null) {
            return null;
        }
        return new LiteralExpression(expression, value);
    }

    @Override
    public @NotNull Expression binary(@NotNull BinaryOperator operator, @NotNull Expression left, @NotNull Expression right) {
        return new BinaryExpression(operator, left, right);
    }

    @Override
    public @Nullable Expression binary(@NotNull RapidBinaryExpression expression) {
        IElementType operatorType = expression.getSign().getNode().getElementType();
        BinaryOperator binaryOperator = getBinaryOperator(operatorType);
        if (binaryOperator == null) {
            return null;
        }
        Expression left = expression(expression.getLeft());
        if (expression.getRight() == null) {
            return null;
        }
        Expression right = expression(expression.getRight());
        if (left == null || right == null) {
            return null;
        }
        if (EnumSet.of(BinaryOperator.ADD, BinaryOperator.SUBTRACT, BinaryOperator.MULTIPLY, BinaryOperator.DIVIDE, BinaryOperator.INTEGER_DIVIDE, BinaryOperator.MODULO, BinaryOperator.LESS_THAN, BinaryOperator.LESS_THAN_OR_EQUAL, BinaryOperator.GREATER_THAN, BinaryOperator.GREATER_THAN_OR_EQUAL).contains(binaryOperator)) {
            if (left.getType().isAssignable(RapidPrimitiveType.NUMBER) && right.getType().isAssignable(RapidPrimitiveType.NUMBER)) {
                return new BinaryExpression(expression, binaryOperator, left, right);
            }
        }
        if (binaryOperator == BinaryOperator.ADD) {
            if (left.getType().isAssignable(RapidPrimitiveType.STRING) && right.getType().isAssignable(RapidPrimitiveType.STRING)) {
                return new BinaryExpression(expression, binaryOperator, left, right);
            }
        }
        if (binaryOperator == BinaryOperator.EQUAL_TO || binaryOperator == BinaryOperator.NOT_EQUAL_TO) {
            return new BinaryExpression(expression, binaryOperator, left, right);
        }
        if (binaryOperator == BinaryOperator.AND || binaryOperator == BinaryOperator.XOR || binaryOperator == BinaryOperator.OR) {
            if (left.getType().isAssignable(RapidPrimitiveType.BOOLEAN) && right.getType().isAssignable(RapidPrimitiveType.BOOLEAN)) {
                return new BinaryExpression(expression, binaryOperator, left, right);
            }
        }
        if (left.getType().isAssignable(RapidPrimitiveType.POSITION) && right.getType().isAssignable(RapidPrimitiveType.POSITION)) {
            if (binaryOperator == BinaryOperator.ADD) {
                return doVectorCompute(left, right, BinaryOperator.ADD);
            }
            if (binaryOperator == BinaryOperator.SUBTRACT) {
                return doVectorCompute(left, right, BinaryOperator.SUBTRACT);
            }
            if (binaryOperator == BinaryOperator.MULTIPLY) {
                return doVectorCompute(left, right, BinaryOperator.MULTIPLY);
            }
        }
        if (left.getType().isAssignable(RapidPrimitiveType.ORIENTATION) && right.getType().isAssignable(RapidPrimitiveType.ORIENTATION)) {
            if (binaryOperator == BinaryOperator.MULTIPLY) {
                return doMultiplicationProduct(left, right);
            }
        }
        if (left.getType().isAssignable(RapidPrimitiveType.NUMBER) && right.getType().isAssignable(RapidPrimitiveType.POSITION)) {
            if (binaryOperator == BinaryOperator.MULTIPLY) {
                return doScalarCompute(right, left, BinaryOperator.MULTIPLY);
            }
        }
        if (left.getType().isAssignable(RapidPrimitiveType.POSITION) && right.getType().isAssignable(RapidPrimitiveType.NUMBER)) {
            if (binaryOperator == BinaryOperator.MULTIPLY) {
                return doScalarCompute(left, right, BinaryOperator.MULTIPLY);
            }
            if (binaryOperator == BinaryOperator.DIVIDE) {
                return doScalarCompute(left, right, BinaryOperator.DIVIDE);
            }
        }
        return null;
    }

    private @NotNull Expression doVectorCompute(@NotNull Expression left, @NotNull Expression right, @NotNull BinaryOperator binaryOperator) {
        ReferenceExpression leftVariable = getAsVariable(left);
        ReferenceExpression rightVariable = getAsVariable(right);
        ReferenceExpression result = getReference(createVariable(RapidPrimitiveType.POSITION));
        assign(component(result, "x"), binary(binaryOperator, component(leftVariable, "x"), component(rightVariable, "x")));
        assign(component(result, "y"), binary(binaryOperator, component(leftVariable, "y"), component(rightVariable, "y")));
        assign(component(result, "z"), binary(binaryOperator, component(leftVariable, "z"), component(rightVariable, "z")));
        return result;
    }

    private @NotNull Expression doScalarCompute(@NotNull Expression vector, @NotNull Expression scalar, @NotNull BinaryOperator binaryOperator) {
        ReferenceExpression variable = getAsVariable(vector);
        ReferenceExpression result = getReference(createVariable(RapidPrimitiveType.POSITION));
        assign(component(result, "x"), binary(binaryOperator, component(variable, "x"), scalar));
        assign(component(result, "y"), binary(binaryOperator, component(variable, "y"), scalar));
        assign(component(result, "z"), binary(binaryOperator, component(variable, "z"), scalar));
        return result;
    }

    private @NotNull Expression doMultiplicationProduct(@NotNull Expression left, @NotNull Expression right) {
        ReferenceExpression leftVariable = getAsVariable(left);
        ReferenceExpression rightVariable = getAsVariable(right);
        ReferenceExpression result = getReference(createVariable(RapidPrimitiveType.ORIENTATION));
        BiFunction<String, String, Expression> calculate = (leftName, rightName) -> binary(BinaryOperator.MULTIPLY, component(leftVariable, leftName), component(rightVariable, rightName));
        assign(component(result, "q1"), doCalculate(calculate.apply("q1", "q1"), BinaryOperator.SUBTRACT, calculate.apply("q2", "q2"), BinaryOperator.SUBTRACT, calculate.apply("q3", "q3"), BinaryOperator.SUBTRACT, calculate.apply("q4", "q4")));
        assign(component(result, "q2"), doCalculate(calculate.apply("q1", "q2"), BinaryOperator.ADD, calculate.apply("q2", "q1"), BinaryOperator.ADD, calculate.apply("q3", "q4"), BinaryOperator.SUBTRACT, calculate.apply("q4", "q3")));
        assign(component(result, "q3"), doCalculate(calculate.apply("q1", "q3"), BinaryOperator.ADD, calculate.apply("q3", "q1"), BinaryOperator.ADD, calculate.apply("q4", "q2"), BinaryOperator.SUBTRACT, calculate.apply("q2", "q4")));
        assign(component(result, "q4"), doCalculate(calculate.apply("q1", "q4"), BinaryOperator.ADD, calculate.apply("q4", "q1"), BinaryOperator.ADD, calculate.apply("q2", "q3"), BinaryOperator.SUBTRACT, calculate.apply("q3", "q2")));
        return result;
    }

    private @NotNull Expression doCalculate(@NotNull Expression variable1, @NotNull BinaryOperator operator1, @NotNull Expression variable2, @NotNull BinaryOperator operator2, @NotNull Expression variable3, @NotNull BinaryOperator operator3, @NotNull Expression variable4) {
        return binary(operator3, binary(operator2, binary(operator1, variable1, variable2), variable3), variable4);
    }

    private @NotNull ReferenceExpression getAsVariable(@NotNull Expression expression) {
        if (expression instanceof ReferenceExpression referenceExpression) {
            return referenceExpression;
        }
        Variable variable = createVariable(expression.getType());
        ReferenceExpression reference = getReference(variable);
        assign(reference, expression);
        return reference;
    }


    private @Nullable BinaryOperator getBinaryOperator(@NotNull IElementType elementType) {
        if (elementType == RapidTokenTypes.PLUS) {
            return BinaryOperator.ADD;
        } else if (elementType == RapidTokenTypes.MINUS) {
            return BinaryOperator.SUBTRACT;
        } else if (elementType == RapidTokenTypes.ASTERISK) {
            return BinaryOperator.MULTIPLY;
        } else if (elementType == RapidTokenTypes.DIV) {
            return BinaryOperator.DIVIDE;
        } else if (elementType == RapidTokenTypes.DIV_KEYWORD) {
            return BinaryOperator.INTEGER_DIVIDE;
        } else if (elementType == RapidTokenTypes.MOD_KEYWORD) {
            return BinaryOperator.MODULO;
        } else if (elementType == RapidTokenTypes.LT) {
            return BinaryOperator.LESS_THAN;
        } else if (elementType == RapidTokenTypes.EQ) {
            return BinaryOperator.EQUAL_TO;
        } else if (elementType == RapidTokenTypes.GT) {
            return BinaryOperator.GREATER_THAN;
        } else if (elementType == RapidTokenTypes.LTGT) {
            return BinaryOperator.NOT_EQUAL_TO;
        } else if (elementType == RapidTokenTypes.LE) {
            return BinaryOperator.LESS_THAN_OR_EQUAL;
        } else if (elementType == RapidTokenTypes.GE) {
            return BinaryOperator.GREATER_THAN_OR_EQUAL;
        } else if (elementType == RapidTokenTypes.AND_KEYWORD) {
            return BinaryOperator.AND;
        } else if (elementType == RapidTokenTypes.XOR_KEYWORD) {
            return BinaryOperator.XOR;
        } else if (elementType == RapidTokenTypes.OR_KEYWORD) {
            return BinaryOperator.OR;
        } else {
            return null;
        }
    }

    @Override
    public @NotNull Expression unary(@NotNull UnaryOperator operator, @NotNull Expression expression) {
        return new UnaryExpression(operator, expression);
    }

    @Override
    public @Nullable Expression unary(@NotNull RapidUnaryExpression expression) {
        IElementType operatorType = expression.getSign().getNode().getElementType();
        UnaryOperator unaryOperator = getUnaryOperator(operatorType);
        if (unaryOperator == null) {
            return null;
        }
        if (expression.getExpression() == null) {
            return null;
        }
        Expression component = expression(expression.getExpression());
        if (component == null) {
            return null;
        }
        if (unaryOperator == UnaryOperator.NEGATE) {
            if (component.getType().isAssignable(RapidPrimitiveType.NUMBER)) {
                return new UnaryExpression(expression, unaryOperator, component);
            }
        }
        if (unaryOperator == UnaryOperator.NOT) {
            if (component.getType().isAssignable(RapidPrimitiveType.BOOLEAN)) {
                return new UnaryExpression(expression, unaryOperator, component);
            }
        }
        if (unaryOperator == UnaryOperator.PRESENT) {
            return new UnaryExpression(expression, unaryOperator, component);
        }
        return null;
    }

    private @Nullable UnaryOperator getUnaryOperator(@NotNull IElementType elementType) {
        if (elementType == RapidTokenTypes.MINUS) {
            return UnaryOperator.NEGATE;
        } else if (elementType == RapidTokenTypes.NOT_KEYWORD) {
            return UnaryOperator.NOT;
        } else {
            return null;
        }
    }

    @Override
    public @NotNull Expression call(@NotNull Expression routine, @NotNull RapidType returnType, @NotNull Consumer<RapidArgumentBuilder> arguments) {
        Map<ArgumentDescriptor, Expression> result = new HashMap<>();
        ControlFlowArgumentBuilder argumentBuilder = new ControlFlowArgumentBuilder(result, this);
        arguments.accept(argumentBuilder);
        Variable variable = createVariable(returnType);
        ReferenceExpression reference = getReference(variable);
        call(null, routine, reference, result);
        return reference;
    }

    @Override
    public @Nullable Expression call(@NotNull RapidFunctionCallExpression expression) {
        RapidType returnType = expression.getType();
        if(returnType == null) {
            return null;
        }
        RapidSymbol symbol = expression.getReferenceExpression().getSymbol();
        if(!(symbol instanceof RapidRoutine routine)) {
            return null;
        }
        String routineName = symbol.getName();
        String moduleName;
        if(symbol instanceof PhysicalElement element) {
            PhysicalModule module = PhysicalModule.getModule(element);
            moduleName = module != null ? module.getName() : null;
        } else {
            moduleName = "";
        }
        if(routineName == null || moduleName == null) {
            return null;
        }
        Map<ArgumentDescriptor, Expression> result = new HashMap<>();
        ControlFlowArgumentBuilder argumentBuilder = new ControlFlowArgumentBuilder(result, this);
        getArgumentConsumer(expression.getArgumentList()).accept(argumentBuilder);
        Variable variable = createVariable(returnType);
        ReferenceExpression reference = getReference(variable);
        call(expression, literal(moduleName + ":" + routineName), reference, result);
        return reference;
    }

    private @NotNull Consumer<RapidArgumentBuilder> getArgumentConsumer(@NotNull RapidArgumentList argumentList) {
        return builder -> {
            List<RapidArgument> arguments = argumentList.getArguments();
            for (RapidArgument argument : arguments) {
                if(argument instanceof RapidRequiredArgument requiredArgument) {
                    RapidExpression value = requiredArgument.getArgument();
                    builder.withRequiredArgument(Objects.requireNonNullElseGet(expression(value), () -> any(value.getType())));
                } else if(argument instanceof RapidConditionalArgument conditionalArgument) {
                    RapidSymbol parameter = conditionalArgument.getParameter().getSymbol();
                    if(!(parameter instanceof RapidParameter parameterSymbol)) {
                        continue;
                    }
                    RapidExpression value = conditionalArgument.getArgument();
                    if(!(value instanceof RapidReferenceExpression referenceExpression) || !(referenceExpression.getSymbol() instanceof RapidParameter argumentSymbol)) {
                        continue;
                    }
                    String parameterName = parameterSymbol.getName();
                    String argumentName = argumentSymbol.getName();
                    if(parameterName == null || argumentName == null) {
                        continue;
                    }
                    Argument argumentValue = getArgument(argumentSymbol.getName());
                    if(argumentValue == null) {
                        continue;
                    }
                    builder.withConditionalArgument(parameterName, argumentValue);
                } else if(argument instanceof RapidOptionalArgument optionalArgument) {
                    RapidSymbol parameter = optionalArgument.getParameter().getSymbol();
                    if(!(parameter instanceof RapidParameter parameterSymbol)) {
                        continue;
                    }
                    RapidExpression value = optionalArgument.getArgument();
                    if(value == null) {
                        continue;
                    }
                    String name = parameterSymbol.getName();
                    if(name == null) {
                        continue;
                    }
                    builder.withOptionalArgument(name, Objects.requireNonNullElseGet(expression(value), () -> any(value.getType())));
                }
            }
        };
    }
    
    private void call(@Nullable RapidElement element, @NotNull Expression routine, @Nullable ReferenceExpression returnVariable, @NotNull Map<ArgumentDescriptor, Expression> arguments) {
        ArgumentDescriptor.Conditional conditional = getConditionalArgument(arguments);
        if (conditional == null) {
            // This function call does not reference any conditional arguments.
            builder.continueScope(new CallInstruction(block, element, routine, returnVariable, getArgumentVariables(arguments)));
            return;
        }
        // The argument which the conditional argument is based on, i.e. the argument is present if this parameter is present.
        Argument argument = getArgument(conditional.name());
        Expression condition;
        if (argument == null) {
            // The parameter does not exist.
            condition = any(RapidPrimitiveType.BOOLEAN);
        } else {
            ReferenceExpression reference = getReference(argument);
            condition = new UnaryExpression(UnaryOperator.PRESENT, reference);
        }
        ifThenElse(condition,
                builder -> {
                    // The argument is present, change it into an optional argument.
                    Expression expression = arguments.get(conditional);
                    Map<ArgumentDescriptor, Expression> copy = new HashMap<>(arguments);
                    copy.remove(conditional);
                    copy.put(new ArgumentDescriptor.Optional(conditional.name()), expression);
                    ((ControlFlowCodeBlockBuilder) builder).call(element, routine, returnVariable, copy);
                }, builder -> {
                    // The argument is not present, remove it.
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
                /*
                 * It's a lot easier to handle call instructions if all of its arguments are variables, instead of
                 * regular expressions.
                 */
                Variable variable = createVariable(expression.getType());
                ReferenceExpression reference = getReference(variable);
                assign(reference, expression);
                variables.put(descriptor, reference);
            }
        });
        return variables;
    }

    private @Nullable ArgumentDescriptor.Conditional getConditionalArgument(@NotNull Map<ArgumentDescriptor, ?> arguments) {
        return arguments.keySet().stream()
                .filter(argument -> argument instanceof ArgumentDescriptor.Conditional)
                .map(argument -> (ArgumentDescriptor.Conditional) argument)
                .findFirst().orElse(null);
    }

    @Override
    public @NotNull Expression any(@Nullable RapidType type) {
        // TODO: 2023-12-04 Replace usages of method with custom, to provide PsiElement
        return new ErrorExpression(Objects.requireNonNullElse(type, RapidPrimitiveType.ANYTYPE));
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
    public @NotNull RapidCodeBlockBuilder ifThen(@NotNull Expression expression, @NotNull Consumer<RapidCodeBlockBuilder> thenConsumer) {
        return ifThenElse(expression, thenConsumer, builder -> {});
    }

    @Override
    public @NotNull RapidCodeBlockBuilder ifThenElse(@NotNull Expression condition, @NotNull Consumer<RapidCodeBlockBuilder> thenConsumer, @NotNull Consumer<RapidCodeBlockBuilder> elseConsumer) {
        if (!(condition.getType().isAssignable(RapidPrimitiveType.BOOLEAN))) {
            condition = any(RapidPrimitiveType.BOOLEAN);
        }
        ConditionalBranchingInstruction instruction = new ConditionalBranchingInstruction(block, null, condition);
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
}

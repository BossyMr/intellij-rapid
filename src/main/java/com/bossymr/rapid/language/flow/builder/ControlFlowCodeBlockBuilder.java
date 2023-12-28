package com.bossymr.rapid.language.flow.builder;

import com.bossymr.rapid.language.builder.*;
import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.snapshots.Snapshot;
import com.bossymr.rapid.language.flow.instruction.*;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public @NotNull Variable createVariable(@NotNull String name, @NotNull RapidType type) {
        return block.createVariable(name, FieldType.VARIABLE, type);
    }

    @Override
    public @NotNull Variable createVariable(@NotNull RapidField field) {
        RapidType type = Objects.requireNonNullElse(field.getType(), RapidPrimitiveType.ANYTYPE);
        return block.createVariable(field.getName(), field.getFieldType(), type);
    }

    @Override
    public @Nullable Argument getArgument(@NotNull String name) {
        return block.findArgument(name);
    }

    @Override
    public @NotNull ReferenceExpression getReference(@NotNull Field field) {
        return new VariableExpression(field);
    }

    @Override
    public @NotNull ReferenceExpression getReference(@NotNull RapidReferenceExpression expression) {
        if (expression.getQualifier() != null) {
            return component(expression);
        }
        RapidSymbol symbol = expression.getSymbol();
        if (symbol == null) {
            return getAsVariable(any());
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
        return getAsVariable(any());
    }

    private @NotNull ReferenceExpression getVariableReference(@NotNull RapidReferenceExpression expression, @NotNull RapidVariable parameter, @NotNull Function<@NotNull String, @Nullable Field> function) {
        String name = parameter.getName();
        if (name == null) {
            return getAsVariable(any());
        }
        Field field = function.apply(name);
        if (field == null) {
            return getAsVariable(any());
        }
        return new VariableExpression(expression, field);
    }

    private @NotNull ReferenceExpression getFieldReference(@NotNull RapidReferenceExpression expression, @NotNull RapidField field) {
        String name = field.getName();
        if (name == null) {
            return getAsVariable(any(expression));
        }
        RapidType type = Objects.requireNonNullElse(field.getType(), RapidPrimitiveType.ANYTYPE);
        if (field instanceof PhysicalField physicalField) {
            PhysicalRoutine routine = PhysicalRoutine.getRoutine(physicalField);
            if (routine != null) {
                Variable variable = block.findVariable(name);
                if (variable == null) {
                    return getAsVariable(any(expression, type));
                }
                return new VariableExpression(expression, variable);
            }
        }
        return getAsVariable(any(expression, type));
    }

    @Override
    public @NotNull ReferenceExpression getReference(@NotNull RapidType type, @NotNull String moduleName, @NotNull String name) {
        return getAsVariable(any(type));
    }

    @Override
    public @NotNull ReferenceExpression index(@NotNull ReferenceExpression variable, @NotNull Expression index) {
        if (variable.getType().getDimensions() <= 0) {
            throw new IllegalArgumentException("Cannot create index expression for variable of type: " + variable.getType());
        }
        if (!(index.getType().isAssignable(RapidPrimitiveType.NUMBER))) {
            throw new IllegalArgumentException("Cannot reference index of type: " + index.getType());
        }
        return new IndexExpression(getAsVariable(variable), index);
    }

    @Override
    public @NotNull ReferenceExpression index(@NotNull RapidIndexExpression expression) {
        if (!(expression.getExpression() instanceof RapidReferenceExpression referenceExpression)) {
            return getAsVariable(any());
        }
        ReferenceExpression variable = getReference(referenceExpression);
        List<RapidExpression> dimensions = expression.getArray().getDimensions();
        if (dimensions.isEmpty()) {
            return getAsVariable(any());
        }
        RapidType type = Objects.requireNonNullElseGet(variable.getType(), () -> RapidPrimitiveType.ANYTYPE.createArrayType(dimensions.size()));
        if (type.getDimensions() < dimensions.size()) {
            return getAsVariable(any());
        }
        List<Expression> expressions = dimensions.stream()
                                                 .map(expr -> expressionOrError(expr, RapidPrimitiveType.NUMBER))
                                                 .toList();
        if (expressions.contains(null)) {
            return getAsVariable(any());
        }
        IndexExpression indexExpression = new IndexExpression(expression, variable, expressions.get(0));
        for (int i = 1; i < expressions.size(); i++) {
            indexExpression = new IndexExpression(expression, indexExpression, expressions.get(i));
        }
        return indexExpression;
    }

    @Override
    public @NotNull ReferenceExpression component(@NotNull Expression variable, @NotNull String name) {
        RapidType type = variable.getType();
        if (!(type.getRootStructure() instanceof RapidRecord record)) {
            throw new IllegalArgumentException("Cannot create component expression for variable of type: " + type);
        }
        for (RapidComponent component : record.getComponents()) {
            if (name.equals(component.getName())) {
                return new ComponentExpression(Objects.requireNonNullElse(component.getType(), RapidPrimitiveType.ANYTYPE), getAsVariable(variable), name);
            }
        }
        throw new IllegalArgumentException("Record: " + record.getName() + " does not have a component: " + name);
    }

    @Override
    public @NotNull ReferenceExpression component(@NotNull RapidReferenceExpression expression) {
        RapidExpression qualifier = expression.getQualifier();
        if (!(qualifier instanceof RapidReferenceExpression referenceExpression)) {
            return getAsVariable(any());
        }
        ReferenceExpression variable = getReference(referenceExpression);
        RapidSymbol symbol = expression.getSymbol();
        if (!(symbol instanceof RapidComponent component)) {
            return getAsVariable(any());
        }
        String name = symbol.getName();
        if (name == null) {
            return getAsVariable(any());
        }
        RapidType type = Objects.requireNonNullElse(component.getType(), RapidPrimitiveType.ANYTYPE);
        return new ComponentExpression(expression, type, variable, name);
    }

    @Override
    public @NotNull Expression aggregate(@NotNull RapidType aggregateType, @NotNull List<? extends Expression> expressions) {
        return new AggregateExpression(aggregateType, expressions);
    }

    @Override
    public @NotNull Expression aggregate(@NotNull RapidAggregateExpression expression) {
        RapidType type = expression.getType();
        if (type == null) {
            return any();
        }
        RapidType defaultType = type.getDimensions() > 0 ? type.createArrayType(type.getDimensions() - 1) : null;
        List<Expression> expressions = expression.getExpressions().stream()
                                                 .map(expr -> expressionOrError(expr, defaultType))
                                                 .collect(Collectors.toList());
        if (type.getDimensions() > 0) {
            // Replace any expressions which are of an invalid type, with an error expression.
            Objects.requireNonNull(defaultType);
            for (ListIterator<Expression> iterator = expressions.listIterator(); iterator.hasNext(); ) {
                Expression expr = iterator.next();
                if (!(defaultType.isAssignable(expr.getType()))) {
                    iterator.remove();
                    iterator.add(any(defaultType));
                }
            }
        } else if (type.getRootStructure() instanceof RapidRecord record) {
            List<RapidComponent> components = record.getComponents();
            for (int i = 0; i < components.size(); i++) {
                RapidType componentType = components.get(i).getType();
                if (i >= expressions.size()) {
                    // This aggregate is too short, add an error expression of the correct type.
                    expressions.add(any(componentType));
                    continue;
                }
                if (componentType == null) {
                    // The component doesn't have a type, we assume that this expression is correct.
                    continue;
                }
                if (!(componentType.isAssignable(expressions.get(i).getType()))) {
                    // The expression is of a different type.
                    expressions.set(i, any(componentType));
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
    public @NotNull Expression literal(@NotNull RapidLiteralExpression expression) {
        Object value = expression.getValue();
        if (value == null) {
            return any();
        }
        return new LiteralExpression(expression, value);
    }

    @Override
    public @NotNull Expression binary(@NotNull BinaryOperator operator, @NotNull Expression left, @NotNull Expression right) {
        return new BinaryExpression(operator, left, right);
    }

    @Override
    public @NotNull Expression binary(@NotNull RapidBinaryExpression expression) {
        RapidType type = expression.getType();
        if (type == null) {
            return any();
        }
        IElementType operatorType = expression.getSign().getNode().getElementType();
        BinaryOperator binaryOperator = getBinaryOperator(operatorType);
        if (binaryOperator == null) {
            return any(type);
        }
        Expression left = expressionOrError(expression.getLeft());
        Expression right = expression.getRight() != null ? expressionOrError(expression.getRight()) : any();
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
        return any(type);
    }

    private @NotNull Expression doVectorCompute(@NotNull Expression left, @NotNull Expression right, @NotNull BinaryOperator binaryOperator) {
        ReferenceExpression leftVariable = getAsVariable(left);
        ReferenceExpression rightVariable = getAsVariable(right);
        Expression result = getReference(createVariable(RapidPrimitiveType.POSITION));
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
        assign(component(result, "q1"), doCalculate(calculate.apply("q1", "q1"), BinaryOperator.SUBTRACT, calculate.apply("q2", "q2"), BinaryOperator.SUBTRACT, calculate.apply("q3", "q3"), calculate.apply("q4", "q4")));
        assign(component(result, "q2"), doCalculate(calculate.apply("q1", "q2"), BinaryOperator.ADD, calculate.apply("q2", "q1"), BinaryOperator.ADD, calculate.apply("q3", "q4"), calculate.apply("q4", "q3")));
        assign(component(result, "q3"), doCalculate(calculate.apply("q1", "q3"), BinaryOperator.ADD, calculate.apply("q3", "q1"), BinaryOperator.ADD, calculate.apply("q4", "q2"), calculate.apply("q2", "q4")));
        assign(component(result, "q4"), doCalculate(calculate.apply("q1", "q4"), BinaryOperator.ADD, calculate.apply("q4", "q1"), BinaryOperator.ADD, calculate.apply("q2", "q3"), calculate.apply("q3", "q2")));
        return result;
    }

    private @NotNull Expression doCalculate(@NotNull Expression variable1, @NotNull BinaryOperator operator1, @NotNull Expression variable2, @NotNull BinaryOperator operator2, @NotNull Expression variable3, @NotNull Expression variable4) {
        return binary(BinaryOperator.SUBTRACT, binary(operator2, binary(operator1, variable1, variable2), variable3), variable4);
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
    public @NotNull Expression unary(@NotNull RapidUnaryExpression expression) {
        RapidType type = expression.getType();
        if (type == null) {
            return any();
        }
        IElementType operatorType = expression.getSign().getNode().getElementType();
        UnaryOperator unaryOperator = getUnaryOperator(operatorType);
        if (unaryOperator == null) {
            return any(type);
        }
        Expression component = expression.getExpression() != null ? expressionOrError(expression.getExpression()) : any();
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
        return any(type);
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
        call(null, expressionOfType(routine, RapidPrimitiveType.STRING), reference, result);
        return reference;
    }

    @Override
    public @NotNull Expression call(@NotNull RapidFunctionCallExpression expression) {
        RapidType returnType = expression.getType();
        if (returnType == null) {
            return any();
        }
        RapidReferenceExpression referenceExpression = expression.getReferenceExpression();
        RapidSymbol symbol = referenceExpression.getSymbol();
        if (!(symbol instanceof RapidRoutine)) {
            return any(returnType);
        }
        String routineName = symbol.getName();
        String moduleName;
        if (symbol instanceof PhysicalSymbol element) {
            PhysicalModule module = PhysicalModule.getModule(element);
            moduleName = module != null ? module.getName() : null;
        } else {
            moduleName = "";
        }
        if (routineName == null || moduleName == null) {
            return any(returnType);
        }
        Map<ArgumentDescriptor, Expression> result = new HashMap<>();
        ControlFlowArgumentBuilder argumentBuilder = new ControlFlowArgumentBuilder(result, this);
        getArgumentConsumer(expression.getArgumentList()).accept(argumentBuilder);
        Variable variable = createVariable(returnType);
        ReferenceExpression reference = getReference(variable);
        call(expression, new LiteralExpression(referenceExpression, moduleName + ":" + routineName), reference, result);
        return reference;
    }

    private @NotNull Consumer<RapidArgumentBuilder> getArgumentConsumer(@NotNull RapidArgumentList argumentList) {
        return builder -> {
            List<RapidArgument> arguments = argumentList.getArguments();
            for (RapidArgument argument : arguments) {
                if (argument instanceof RapidRequiredArgument requiredArgument) {
                    RapidExpression value = requiredArgument.getArgument();
                    builder.withRequiredArgument(expressionOrError(value));
                } else if (argument instanceof RapidConditionalArgument conditionalArgument) {
                    RapidSymbol parameter = conditionalArgument.getParameter().getSymbol();
                    if (!(parameter instanceof RapidParameter parameterSymbol)) {
                        continue;
                    }
                    RapidExpression value = conditionalArgument.getArgument();
                    if (!(value instanceof RapidReferenceExpression referenceExpression) || !(referenceExpression.getSymbol() instanceof RapidParameter argumentSymbol)) {
                        continue;
                    }
                    String parameterName = parameterSymbol.getName();
                    String argumentName = argumentSymbol.getName();
                    if (parameterName == null || argumentName == null) {
                        continue;
                    }
                    Argument argumentValue = getArgument(argumentSymbol.getName());
                    if (argumentValue == null) {
                        continue;
                    }
                    builder.withConditionalArgument(parameterName, argumentValue);
                } else if (argument instanceof RapidOptionalArgument optionalArgument) {
                    RapidSymbol parameter = optionalArgument.getParameter().getSymbol();
                    if (!(parameter instanceof RapidParameter parameterSymbol)) {
                        continue;
                    }
                    RapidExpression value = optionalArgument.getArgument();
                    if (value == null) {
                        continue;
                    }
                    String name = parameterSymbol.getName();
                    if (name == null) {
                        continue;
                    }
                    builder.withOptionalArgument(name, expressionOrError(value));
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
        return new SnapshotExpression(Snapshot.createSnapshot(type != null ? type : RapidPrimitiveType.ANYTYPE));
    }

    private @NotNull Expression any(@Nullable RapidExpression expression) {
        if (expression == null) {
            return any(RapidPrimitiveType.ANYTYPE);
        } else {
            return any(expression, expression.getType());
        }
    }

    private @NotNull Expression any(@Nullable RapidExpression expression, @Nullable RapidType type) {
        RapidType valueType = Objects.requireNonNullElse(type, RapidPrimitiveType.ANYTYPE);
        return new SnapshotExpression(Snapshot.createSnapshot(valueType, Optionality.PRESENT), expression);
    }

    @Override
    public @NotNull Expression expression(@NotNull RapidExpression expression) {
        if (expression instanceof RapidReferenceExpression referenceExpression) {
            return getReference(referenceExpression);
        }
        if (expression instanceof RapidIndexExpression indexExpression) {
            return index(indexExpression);
        }
        if (expression instanceof RapidAggregateExpression aggregateExpression) {
            return aggregate(aggregateExpression);
        }
        if (expression instanceof RapidFunctionCallExpression functionCallExpression) {
            return call(functionCallExpression);
        }
        if (expression instanceof RapidParenthesisedExpression parenthesisedExpression) {
            RapidExpression component = parenthesisedExpression.getExpression();
            if (component == null) {
                return any();
            }
            return expression(component);
        }
        if (expression instanceof RapidLiteralExpression literalExpression) {
            return literal(literalExpression);
        }
        if (expression instanceof RapidBinaryExpression binaryExpression) {
            return binary(binaryExpression);
        }
        if (expression instanceof RapidUnaryExpression unaryExpression) {
            return unary(unaryExpression);
        }
        return any(expression);
    }

    public @NotNull Expression expressionOfType(@NotNull Expression expression, @NotNull RapidType type) {
        if (expression.getType().isAssignable(type)) {
            return expression;
        }
        return any(expression.getElement(), type);
    }

    private @NotNull Expression expressionOrError(@NotNull RapidExpression expression) {
        return expression(expression);
    }

    private @NotNull Expression expressionOrError(@Nullable RapidExpression expression, @Nullable RapidType type) {
        if (expression == null) {
            return any(null, type);
        }
        return expression(expression);
    }

    @Override
    public void returnValue(@Nullable Expression expression) {
        if (block.getReturnType() != null) {
            if (expression == null) {
                expression = any(block.getReturnType());
            } else if (!(block.getReturnType().isAssignable(expression.getType()))) {
                expression = any(expression.getElement(), block.getReturnType());
            }
        }
        if (block.getReturnType() == null && expression != null) {
            expression = null;
        }
        builder.continueScope(new ReturnInstruction(block, null, expression));
        builder.exitScope();
    }

    @Override
    public void returnValue(@NotNull RapidReturnStatement statement) {
        RapidExpression returnValue = statement.getExpression();
        RapidType returnType = block.getReturnType();
        if (returnType == null) {
            builder.continueScope(new ReturnInstruction(block, statement, null));
            builder.exitScope();
        } else {
            Expression expression = returnValue != null ? expressionOrError(returnValue, returnType) : any(returnType);
            builder.continueScope(new ReturnInstruction(block, statement, expression));
            builder.exitScope();
        }
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
    public @NotNull Label createLabel(@NotNull RapidLabelStatement statement) {
        String name = statement.getName();
        if (name == null) {
            return createLabel();
        }
        return createLabel(name);
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
    public @NotNull RapidCodeBlockBuilder ifThen(@NotNull Expression expression, @NotNull Consumer<RapidCodeBlockBuilder> thenConsumer) {
        return ifThenElse(expression, thenConsumer, builder -> {});
    }

    @Override
    public @NotNull RapidCodeBlockBuilder ifThenElse(@NotNull Expression condition, @NotNull Consumer<RapidCodeBlockBuilder> thenConsumer, @NotNull Consumer<RapidCodeBlockBuilder> elseConsumer) {
        if (!(condition.getType().isAssignable(RapidPrimitiveType.BOOLEAN))) {
            condition = any(RapidPrimitiveType.BOOLEAN);
        }
        condition = getAsVariable(condition);
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

    private @NotNull Consumer<RapidCodeBlockBuilder> getBlockConsumer(@Nullable RapidStatementList statementList) {
        if (statementList == null) {
            return builder -> {};
        }
        return builder -> {
            for (RapidStatement statement : statementList.getStatements()) {
                builder.statement(statement);
            }
        };
    }

    @Override
    public @NotNull RapidCodeBlockBuilder ifThenElse(@NotNull RapidIfStatement statement) {
        Expression expression = expressionOrError(statement.getCondition(), RapidPrimitiveType.BOOLEAN);
        return ifThenElse(expression, getBlockConsumer(statement.getThenBranch()), getBlockConsumer(statement.getElseBranch()));
    }

    @Override
    public @NotNull RapidCodeBlockBuilder whileLoop(@NotNull Expression condition, @NotNull Consumer<RapidCodeBlockBuilder> consumer) {
        Label label = createLabel();
        ifThen(condition, thenConsumer -> {
            consumer.accept(thenConsumer);
            thenConsumer.goTo(label);
        });
        return this;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder whileLoop(@NotNull RapidWhileStatement statement) {
        Label label = createLabel();
        Expression expression = expressionOrError(statement.getCondition(), RapidPrimitiveType.BOOLEAN);
        return ifThen(expression, thenBuilder -> {
            RapidStatementList statementList = statement.getStatementList();
            if (statementList != null) {
                for (RapidStatement instruction : statementList.getStatements()) {
                    thenBuilder.statement(instruction);
                }
            }
            thenBuilder.goTo(label);
        });
    }

    @Override
    public @NotNull RapidCodeBlockBuilder forLoop(@NotNull Expression fromExpression, @NotNull Expression toExpression, @Nullable Expression stepExpression, @NotNull BiConsumer<Variable, RapidCodeBlockBuilder> consumer) {
        Variable indexVariable = createVariable(RapidPrimitiveType.NUMBER);
        ReferenceExpression indexExpression = getReference(indexVariable);
        assign(indexExpression, fromExpression);
        if (stepExpression == null) {
            Variable stepVariable = createVariable(RapidPrimitiveType.NUMBER);
            stepExpression = getReference(stepVariable);
            ifThenElse(binary(BinaryOperator.LESS_THAN, indexExpression, toExpression),
                    thenBuilder -> thenBuilder.assign(getReference(stepVariable), thenBuilder.literal(1)),
                    elseBuilder -> elseBuilder.assign(getReference(stepVariable), elseBuilder.literal(-1)));
        }
        Label label = createLabel();
        Variable breakVariable = createVariable(RapidPrimitiveType.BOOLEAN);
        ReferenceExpression breakExpression = getReference(breakVariable);
        ifThenElse(binary(BinaryOperator.LESS_THAN, stepExpression, literal(0)),
                thenBuilder -> thenBuilder.assign(breakExpression, thenBuilder.binary(BinaryOperator.GREATER_THAN, indexExpression, toExpression)),
                elseBuilder -> elseBuilder.assign(breakExpression, elseBuilder.binary(BinaryOperator.LESS_THAN, indexExpression, toExpression)));
        Expression stepVariable = stepExpression;
        return ifThen(breakExpression,
                thenBuilder -> {
                    consumer.accept(indexVariable, thenBuilder);
                    thenBuilder.assign(indexExpression, binary(BinaryOperator.ADD, indexExpression, stepVariable));
                    thenBuilder.goTo(label);
                });
    }

    @Override
    public @NotNull RapidCodeBlockBuilder forLoop(@NotNull RapidForStatement statement) {
        Expression fromExpression = expressionOrError(statement.getFromExpression(), RapidPrimitiveType.NUMBER);
        Expression toExpression = expressionOrError(statement.getToExpression(), RapidPrimitiveType.NUMBER);
        Expression stepExpression = statement.getStepExpression() != null ? expressionOrError(statement.getStepExpression(), RapidPrimitiveType.NUMBER) : null;
        String name = statement.getVariable() != null ? statement.getVariable().getName() : null;
        return forLoop(fromExpression, toExpression, stepExpression, (variable, builder) -> {
            variable.setName(name);
            RapidStatementList statementList = statement.getStatementList();
            if (statementList != null) {
                for (RapidStatement instruction : statementList.getStatements()) {
                    builder.statement(instruction);
                }
            }
            variable.setName(null);
        });
    }

    @Override
    public @NotNull RapidCodeBlockBuilder test(@NotNull Expression condition, @NotNull Consumer<RapidTestBlockBuilder> consumer) {
        ControlFlowTestBlockBuilder testBlockBuilder = new ControlFlowTestBlockBuilder(this, condition);
        consumer.accept(testBlockBuilder);
        return this;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder test(@NotNull RapidTestStatement statement) {
        Expression expression = expressionOrError(statement.getExpression(), RapidPrimitiveType.ANYTYPE);
        return test(expression, builder -> {
            for (RapidTestCaseStatement caseStatement : statement.getTestCaseStatements()) {
                if (caseStatement.isDefault()) {
                    builder.withDefaultCase(codeBuilder -> {
                        for (RapidStatement instruction : caseStatement.getStatements().getStatements()) {
                            codeBuilder.statement(instruction);
                        }
                    });
                } else {
                    List<RapidExpression> conditions = caseStatement.getExpressions();
                    List<Expression> expressions;
                    if (conditions == null) {
                        expressions = List.of(any(expression.getType()));
                    } else {
                        expressions = conditions.stream().map(expr -> expressionOrError(expr, expression.getType())).toList();
                    }
                    builder.withCase(expressions, codeBuilder -> {
                        for (RapidStatement instruction : caseStatement.getStatements().getStatements()) {
                            codeBuilder.statement(instruction);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void goTo(@NotNull Label label) {
        if (!(label instanceof ControlFlowLabel instruction)) {
            builder.exitScope();
            return;
        }
        builder.goTo(instruction);
    }

    @Override
    public void goTo(@NotNull RapidGotoStatement statement) {
        RapidReferenceExpression referenceExpression = statement.getReferenceExpression();
        if (referenceExpression == null) {
            builder.exitScope();
            return;
        }
        RapidSymbol symbol = referenceExpression.getSymbol();
        if (!(symbol instanceof RapidLabelStatement label)) {
            builder.exitScope();
            return;
        }
        String name = label.getName();
        if (name == null) {
            builder.exitScope();
            return;
        }
        Label target = getLabel(name);
        goTo(target);
    }

    @Override
    public void raise(@Nullable Expression expression) {
        if (expression != null) {
            expression = expressionOfType(expression, RapidPrimitiveType.NUMBER);
        }
        builder.continueScope(new ThrowInstruction(block, null, expression));
        builder.exitScope();
    }

    @Override
    public void raise(@NotNull RapidRaiseStatement statement) {
        RapidExpression component = statement.getExpression();
        Expression expression = component != null ? expressionOfType(expressionOrError(component), RapidPrimitiveType.NUMBER) : null;
        builder.continueScope(new ThrowInstruction(block, statement, expression));
        builder.exitScope();
    }

    @Override
    public void tryNext() {
        builder.continueScope(new TryNextInstruction(block, null));
        builder.exitScope();
    }

    @Override
    public void tryNext(@NotNull RapidTryNextStatement statement) {
        builder.continueScope(new TryNextInstruction(block, statement));
        builder.exitScope();
    }

    @Override
    public void exit() {
        builder.continueScope(new ExitInstruction(block, null));
        builder.exitScope();
    }

    @Override
    public void exit(@NotNull RapidExitStatement statement) {
        builder.continueScope(new ExitInstruction(block, statement));
        builder.exitScope();
    }

    @Override
    public void retry() {
        builder.continueScope(new RetryInstruction(block, null));
        builder.exitScope();
    }

    @Override
    public void retry(@NotNull RapidRetryStatement statement) {
        builder.continueScope(new RetryInstruction(block, statement));
        builder.exitScope();
    }

    @Override
    public @NotNull RapidCodeBlockBuilder assign(@NotNull ReferenceExpression variable, @NotNull Expression expression) {
        expression = expressionOfType(expression, variable.getType());
        builder.continueScope(new AssignmentInstruction(block, null, variable, expression));
        return this;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder assign(@NotNull RapidAssignmentStatement statement) {
        RapidExpression left = statement.getLeft();
        ReferenceExpression variable = left != null ? getAsVariable(expressionOrError(left)) : getAsVariable(any(RapidPrimitiveType.ANYTYPE));
        RapidExpression right = statement.getRight();
        Expression expression = right != null ? expressionOrError(right) : any(variable.getType());
        expression = expressionOfType(expression, variable.getType());
        builder.continueScope(new AssignmentInstruction(block, statement, variable, expression));
        return this;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder connect(@NotNull ReferenceExpression variable, @NotNull String routine) {
        variable = getAsVariable(expressionOfType(variable, RapidPrimitiveType.NUMBER));
        builder.continueScope(new ConnectInstruction(block, null, variable, new LiteralExpression(routine)));
        return this;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder connect(@NotNull RapidConnectStatement statement) {
        ReferenceExpression variable = getAsVariable(expressionOrError(statement.getLeft(), RapidPrimitiveType.NUMBER));
        Expression expression = expressionOrError(statement.getRight(), RapidPrimitiveType.STRING);
        builder.continueScope(new ConnectInstruction(block, statement, variable, expression));
        return this;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder invoke(@NotNull Expression routine, @NotNull Consumer<RapidArgumentBuilder> arguments) {
        Map<ArgumentDescriptor, Expression> result = new HashMap<>();
        ControlFlowArgumentBuilder argumentBuilder = new ControlFlowArgumentBuilder(result, this);
        arguments.accept(argumentBuilder);
        routine = expressionOfType(routine, RapidPrimitiveType.STRING);
        call(null, routine, null, result);
        return this;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder invoke(@NotNull RapidProcedureCallStatement statement) {
        Map<ArgumentDescriptor, Expression> result = new HashMap<>();
        ControlFlowArgumentBuilder argumentBuilder = new ControlFlowArgumentBuilder(result, this);
        getArgumentConsumer(statement.getArgumentList()).accept(argumentBuilder);
        Expression expression = null;
        if (statement.getReferenceExpression() instanceof RapidReferenceExpression referenceExpression) {
            RapidSymbol symbol = referenceExpression.getSymbol();
            if (symbol == null) {
                expression = new LiteralExpression(referenceExpression, ":" + referenceExpression.getCanonicalText());
            } else if (symbol instanceof RapidRoutine routine) {
                String name = routine.getName();
                String moduleName = "";
                if (routine instanceof PhysicalRoutine physicalRoutine) {
                    PhysicalModule module = PhysicalModule.getModule(physicalRoutine);
                    if (module != null) {
                        moduleName = module.getName();
                    }
                }
                if (moduleName != null && name != null) {
                    expression = new LiteralExpression(referenceExpression, moduleName + ":" + name);
                }
            }
        }
        if (expression == null) {
            expression = expressionOrError(statement.getReferenceExpression(), RapidPrimitiveType.STRING);
        }
        call(null, expression, null, result);
        return this;
    }

    @Override
    public @NotNull RapidCodeBlockBuilder statement(@NotNull RapidStatement statement) {
        if (statement instanceof RapidReturnStatement returnStatement) {
            returnValue(returnStatement);
        } else if (statement instanceof RapidTestStatement testStatement) {
            test(testStatement);
        } else if (statement instanceof RapidExitStatement exitStatement) {
            exit(exitStatement);
        } else if (statement instanceof RapidRaiseStatement raiseStatement) {
            raise(raiseStatement);
        } else if (statement instanceof RapidTryNextStatement tryNextStatement) {
            tryNext(tryNextStatement);
        } else if (statement instanceof RapidAssignmentStatement assignmentStatement) {
            assign(assignmentStatement);
        } else if (statement instanceof RapidIfStatement ifStatement) {
            ifThenElse(ifStatement);
        } else if (statement instanceof RapidConnectStatement connectStatement) {
            connect(connectStatement);
        } else if (statement instanceof RapidLabelStatement labelStatement) {
            createLabel(labelStatement);
        } else if (statement instanceof RapidProcedureCallStatement procedureCallStatement) {
            invoke(procedureCallStatement);
        } else if (statement instanceof RapidRetryStatement retryStatement) {
            retry(retryStatement);
        } else if (statement instanceof RapidForStatement forStatement) {
            forLoop(forStatement);
        } else if (statement instanceof RapidGotoStatement gotoStatement) {
            goTo(gotoStatement);
        } else if (statement instanceof RapidWhileStatement whileStatement) {
            whileLoop(whileStatement);
        }
        return this;
    }
}

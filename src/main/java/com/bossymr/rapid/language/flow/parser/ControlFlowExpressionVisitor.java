package com.bossymr.rapid.language.flow.parser;

import com.bossymr.rapid.language.builder.RapidArgumentBuilder;
import com.bossymr.rapid.language.builder.RapidCodeBlockBuilder;
import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.builder.ControlFlowCodeBlockBuilder;
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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

class ControlFlowExpressionVisitor extends RapidElementVisitor {

    private final @NotNull ControlFlowCodeBlockBuilder builder;

    private final @NotNull AtomicReference<Expression> result = new AtomicReference<>();

    public ControlFlowExpressionVisitor(@NotNull RapidCodeBlockBuilder builder) {
        this.builder = (ControlFlowCodeBlockBuilder) builder;
    }

    public static @NotNull Expression getExpression(@NotNull RapidExpression expression, @NotNull RapidCodeBlockBuilder builder) {
        ControlFlowExpressionVisitor visitor = new ControlFlowExpressionVisitor(builder);
        expression.accept(visitor);
        return Objects.requireNonNull(visitor.getResult());
    }

    public static @NotNull Consumer<RapidArgumentBuilder> getArgumentBuilder(@NotNull RapidCodeBlockBuilder builder, @NotNull RapidArgumentList argumentList) {
        return argumentBuilder -> {
            for (RapidArgument argument : argumentList.getArguments()) {
                if (argument instanceof RapidRequiredArgument requiredArgument) {
                    RapidExpression value = requiredArgument.getArgument();
                    argumentBuilder.withRequiredArgument(getExpression(value, builder));
                }
                if (argument instanceof RapidConditionalArgument conditionalArgument) {
                    RapidReferenceExpression parameter = conditionalArgument.getParameter();
                    RapidExpression value = conditionalArgument.getArgument();
                    Expression input = value != null ? getExpression(value, builder) : builder.error(null, RapidPrimitiveType.ANYTYPE);
                    ReferenceExpression referenceExpression = builder.getArgument(parameter.getText());
                    if (!(referenceExpression instanceof VariableExpression variableExpression) || !(variableExpression.getField() instanceof Argument)) {
                        return;
                    }
                    argumentBuilder.withConditionalArgument((Argument) variableExpression.getField(), input);
                }
                if (argument instanceof RapidOptionalArgument optionalArgument) {
                    RapidReferenceExpression parameter = optionalArgument.getParameter();
                    RapidExpression value = optionalArgument.getArgument();
                    Expression input = value != null ? getExpression(value, builder) : builder.error(null, RapidPrimitiveType.ANYTYPE);
                    argumentBuilder.withOptionalArgument(parameter.getText(), input);
                }
            }
        };
    }

    public @NotNull Expression getResult() {
        return Objects.requireNonNull(result.get());
    }

    @Override
    public void visitAggregateExpression(@NotNull RapidAggregateExpression expression) {
        RapidType type = expression.getType();
        if (type == null) {
            result.set(builder.error(expression, RapidPrimitiveType.ANYTYPE));
            return;
        }
        if(type.getDimensions() > 0) {
            // TODO: 2023-12-01
        } else if(type.getRootStructure() instanceof RapidRecord record) {
            // TODO: 2023-12-01
        } else {
            result.set(builder.error(expression, type));
        }
    }

    @Override
    public void visitUnaryExpression(@NotNull RapidUnaryExpression expression) {
        RapidType type = expression.getType();
        if (type == null) {
            result.set(builder.error(expression, RapidPrimitiveType.ANYTYPE));
            return;
        }
        IElementType operatorType = expression.getSign().getNode().getElementType();
        UnaryOperator unaryOperator = getUnaryOperator(operatorType);
        RapidExpression componentExpression = expression.getExpression();
        if (unaryOperator == null || componentExpression == null) {
            result.set(builder.error(expression, type));
            return;
        }
        Expression component = getExpression(componentExpression, builder);
        switch (unaryOperator) {
            case NOT -> {
                if (!component.getType().isAssignable(RapidPrimitiveType.NUMBER)) {
                    result.set(builder.error(expression, RapidPrimitiveType.BOOLEAN));
                    return;
                }
            }
            case NEGATE -> {
                if (!component.getType().isAssignable(RapidPrimitiveType.NUMBER)) {
                    result.set(builder.error(expression, RapidPrimitiveType.NUMBER));
                    return;
                }
            }
        }
        result.set(builder.unary(expression, unaryOperator, component));
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
    public void visitBinaryExpression(@NotNull RapidBinaryExpression expression) {
        RapidType type = expression.getType();
        IElementType operatorType = expression.getSign().getNode().getElementType();
        BinaryOperator binaryOperator = getBinaryOperator(operatorType);
        if (binaryOperator == null) {
            result.set(builder.error(expression, Objects.requireNonNullElse(type, RapidPrimitiveType.ANYTYPE)));
            return;
        }
        Expression left = getExpression(expression.getLeft(), builder);
        Expression right = expression.getRight() != null ? getExpression(expression.getRight(), builder) : builder.error(null, RapidPrimitiveType.ANYTYPE);
        switch (binaryOperator) {
            case ADD, SUBTRACT, MULTIPLY, DIVIDE, INTEGER_DIVIDE, MODULO -> {
                if (binaryOperator == BinaryOperator.ADD) {
                    if (left.getType().isAssignable(RapidPrimitiveType.STRING) && right.getType().isAssignable(RapidPrimitiveType.STRING)) {
                        break;
                    }
                }
                if (binaryOperator == BinaryOperator.ADD || binaryOperator == BinaryOperator.SUBTRACT) {
                    if (left.getType().isAssignable(RapidPrimitiveType.POSITION) && right.getType().isAssignable(RapidPrimitiveType.POSITION)) {
                        break;
                    }
                }
                if (binaryOperator == BinaryOperator.MULTIPLY || binaryOperator == BinaryOperator.DIVIDE) {
                    if (left.getType().isAssignable(RapidPrimitiveType.POSITION)) {
                        if (right.getType().isAssignable(RapidPrimitiveType.POSITION)) {
                            if (binaryOperator == BinaryOperator.MULTIPLY) {
                                result.set(doVectorProduct(left, right));
                                break;
                            }
                        } else if (right.getType().isAssignable(RapidPrimitiveType.NUMBER)) {
                            if (binaryOperator == BinaryOperator.MULTIPLY) {
                                result.set(doScalarVectorMultiply(left, right));
                            } else {
                                result.set(doScalarVectorDivision(left, right));
                            }
                            break;
                        }
                    } else if (right.getType().isAssignable(RapidPrimitiveType.POSITION)) {
                        if (left.getType().isAssignable(RapidPrimitiveType.NUMBER)) {
                            if (binaryOperator == BinaryOperator.MULTIPLY) {
                                result.set(doScalarVectorMultiply(right, left));
                                break;
                            }
                        }
                    }
                }
                if (!(left.getType().isAssignable(RapidPrimitiveType.NUMBER)) || !(right.getType().isAssignable(RapidPrimitiveType.NUMBER))) {
                    result.set(builder.error(expression, RapidPrimitiveType.NUMBER));
                    return;
                }
            }
            case LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL -> {
                if (!left.getType().isAssignable(RapidPrimitiveType.NUMBER) || !right.getType().isAssignable(RapidPrimitiveType.NUMBER)) {
                    result.set(builder.error(expression, RapidPrimitiveType.BOOLEAN));
                    return;
                }
            }
            case EQUAL_TO -> {
                if (!left.getType().isAssignable(right.getType())) {
                    result.set(builder.literal(false));
                    return;
                }
            }
            case NOT_EQUAL_TO -> {
                if (!left.getType().isAssignable(right.getType())) {
                    result.set(builder.literal(true));
                    return;
                }
            }
            case AND, XOR, OR -> {
                if (!left.getType().isAssignable(RapidPrimitiveType.BOOLEAN) || !right.getType().isAssignable(RapidPrimitiveType.BOOLEAN)) {
                    result.set(builder.error(expression, RapidPrimitiveType.BOOLEAN));
                    return;
                }
            }
        }
        result.set(builder.binary(expression, binaryOperator, left, right));
    }

    private @NotNull Expression doScalarVectorMultiply(@NotNull Expression vector, @NotNull Expression scalar) {
        ReferenceExpression variable = getAsVariable(vector);
        ReferenceExpression result = builder.createVariable(RapidPrimitiveType.POSITION);
        builder.assign(getComponentExpression(result, "x"), new BinaryExpression(BinaryOperator.MULTIPLY, getComponentExpression(variable, "x"), scalar));
        builder.assign(getComponentExpression(result, "y"), new BinaryExpression(BinaryOperator.MULTIPLY, getComponentExpression(variable, "y"), scalar));
        builder.assign(getComponentExpression(result, "z"), new BinaryExpression(BinaryOperator.MULTIPLY, getComponentExpression(variable, "z"), scalar));
        return result;
    }

    private @NotNull Expression doScalarVectorDivision(@NotNull Expression vector, @NotNull Expression scalar) {
        ReferenceExpression variable = getAsVariable(vector);
        ReferenceExpression result = builder.createVariable(RapidPrimitiveType.POSITION);
        builder.assign(getComponentExpression(result, "x"), new BinaryExpression(BinaryOperator.DIVIDE, getComponentExpression(variable, "x"), scalar));
        builder.assign(getComponentExpression(result, "y"), new BinaryExpression(BinaryOperator.DIVIDE, getComponentExpression(variable, "y"), scalar));
        builder.assign(getComponentExpression(result, "z"), new BinaryExpression(BinaryOperator.DIVIDE, getComponentExpression(variable, "z"), scalar));
        return result;
    }

    private @NotNull Expression doVectorProduct(@NotNull Expression left, @NotNull Expression right) {
        ReferenceExpression leftVariable = getAsVariable(left);
        ReferenceExpression rightVariable = getAsVariable(right);
        ReferenceExpression result = builder.createVariable(RapidPrimitiveType.POSITION);
        builder.assign(getComponentExpression(result, "x"), new BinaryExpression(BinaryOperator.MULTIPLY, getComponentExpression(leftVariable, "x"), getComponentExpression(rightVariable, "x")));
        builder.assign(getComponentExpression(result, "y"), new BinaryExpression(BinaryOperator.MULTIPLY, getComponentExpression(leftVariable, "y"), getComponentExpression(rightVariable, "y")));
        builder.assign(getComponentExpression(result, "z"), new BinaryExpression(BinaryOperator.MULTIPLY, getComponentExpression(leftVariable, "z"), getComponentExpression(rightVariable, "z")));
        return result;
    }

    private @NotNull ComponentExpression getComponentExpression(@NotNull ReferenceExpression variable, @NotNull String name) {
        return new ComponentExpression(RapidPrimitiveType.NUMBER, variable, name);
    }

    private @NotNull ReferenceExpression getAsVariable(@NotNull Expression expression) {
        if (expression instanceof ReferenceExpression referenceExpression) {
            return referenceExpression;
        }
        ReferenceExpression variable = builder.createVariable(expression.getType());
        builder.assign(variable, expression);
        return variable;
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
    public void visitReferenceExpression(@NotNull RapidReferenceExpression expression) {
        RapidType type = expression.getType();
        if (type == null) {
            result.set(builder.error(expression, RapidPrimitiveType.ANYTYPE));
            return;
        }
        RapidSymbol symbol = expression.getSymbol();
        if (symbol == null || symbol.getName() == null) {
            result.set(builder.error(expression, type));
            return;
        }
        String name = symbol.getName();
        RapidExpression qualifier = expression.getQualifier();
        if (symbol instanceof RapidComponent component) {
            if (component.getName() == null) {
                result.set(builder.error(expression, type));
                return;
            }
            if (!(qualifier instanceof RapidReferenceExpression referenceExpression)) {
                result.set(builder.error(expression, type));
                return;
            }
            Expression qualifierExpression = getExpression(referenceExpression, builder);
            if (!(qualifierExpression instanceof ReferenceExpression qualifierReferenceExpression)) {
                result.set(builder.error(expression, type));
                return;
            }
            result.set(builder.component(expression, type, qualifierReferenceExpression, component.getName()));
        } else if (symbol instanceof PhysicalField field) {
            PhysicalRoutine routine = PhysicalRoutine.getRoutine(field);
            if (routine == null) {
                PhysicalModule module = PhysicalModule.getModule(field);
                if (module == null || module.getName() == null) {
                    result.set(builder.error(expression, type));
                    return;
                }
                result.set(builder.getField(expression, module.getName(), name, type));
            } else {
                result.set(builder.getVariable(expression, name));
            }
        } else if (symbol instanceof RapidTargetVariable) {
            result.set(builder.getVariable(expression, name));
        } else if (symbol instanceof RapidField) {
            result.set(builder.getField(expression, "", name, type));
        } else if (symbol instanceof RapidParameter) {
            result.set(builder.getArgument(expression, name));
        } else {
            result.set(builder.error(expression, type));
        }
    }

    @Override
    public void visitIndexExpression(@NotNull RapidIndexExpression expression) {
        RapidType type = expression.getType();
        if (type == null) {
            result.set(builder.error(expression, RapidPrimitiveType.ANYTYPE));
            return;
        }
        Expression component = getExpression(expression.getExpression(), builder);
        if (!(component instanceof ReferenceExpression variable)) {
            result.set(builder.error(expression, type));
            return;
        }
        IndexExpression indexExpression = null;
        for (RapidExpression dimension : expression.getArray().getDimensions()) {
            Expression dimensionExpression = getExpression(dimension, builder);
            indexExpression = builder.index(expression, indexExpression != null ? indexExpression : variable, dimensionExpression);
        }
        result.set(Objects.requireNonNullElseGet(indexExpression, () -> builder.error(expression, type)));
    }

    @Override
    public void visitFunctionCallExpression(@NotNull RapidFunctionCallExpression expression) {
        RapidType type = expression.getType();
        if (type == null) {
            result.set(builder.error(expression, RapidPrimitiveType.ANYTYPE));
            return;
        }
        RapidSymbol symbol = expression.getReferenceExpression().getSymbol();
        if (!(symbol instanceof RapidRoutine)) {
            result.set(builder.error(expression, type));
            return;
        }
        String routineName = symbol.getName();
        String moduleName;
        if (symbol instanceof PhysicalElement element) {
            PhysicalModule module = PhysicalModule.getModule(element);
            moduleName = module != null ? module.getName() : null;
        } else {
            moduleName = "";
        }
        if (routineName == null || moduleName == null) {
            result.set(builder.error(expression, type));
            return;
        }
        result.set(builder.call(moduleName + ":" + routineName, type, getArgumentBuilder(builder, expression.getArgumentList())));
    }

    @Override
    public void visitParenthesisedExpression(@NotNull RapidParenthesisedExpression expression) {
        RapidExpression component = expression.getExpression();
        if (component == null) {
            result.set(builder.error(expression, Objects.requireNonNullElse(expression.getType(), RapidPrimitiveType.ANYTYPE)));
        } else {
            result.set(getExpression(component, builder));
        }
    }

    @Override
    public void visitLiteralExpression(@NotNull RapidLiteralExpression expression) {
        RapidType type = expression.getType();
        Object object = expression.getValue();
        if (type == null) {
            result.set(builder.error(expression, RapidPrimitiveType.ANYTYPE));
            return;
        }
        if (object == null) {
            result.set(builder.error(expression, type));
            return;
        }
        result.set(builder.literal(object));
    }
}

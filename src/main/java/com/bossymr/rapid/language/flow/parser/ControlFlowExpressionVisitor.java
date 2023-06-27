package com.bossymr.rapid.language.flow.parser;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.Variable;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.Operator;
import com.bossymr.rapid.language.flow.value.Value;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.symbol.physical.PhysicalVisibleSymbol;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ControlFlowExpressionVisitor extends RapidElementVisitor {

    public static final @NotNull TokenSet NUMBER_TOKEN_SET = TokenSet.create(RapidTokenTypes.PLUS, RapidTokenTypes.MINUS, RapidTokenTypes.ASTERISK, RapidTokenTypes.DIV, RapidTokenTypes.DIV_KEYWORD, RapidTokenTypes.MOD_KEYWORD);
    public static final @NotNull TokenSet BOOLEAN_TOKEN_SET = TokenSet.create(RapidTokenTypes.LT, RapidTokenTypes.LE, RapidTokenTypes.EQ, RapidTokenTypes.LTGT, RapidTokenTypes.GT, RapidTokenTypes.GE, RapidTokenTypes.AND_KEYWORD, RapidTokenTypes.OR_KEYWORD);
    public static final @NotNull TokenSet STRING_TOKEN_SET = TokenSet.create(RapidTokenTypes.PLUS);
    private final @NotNull ControlFlowBuilder builder;
    private final @NotNull Deque<VariableKey> targetVariable = new ArrayDeque<>();

    public ControlFlowExpressionVisitor(@NotNull ControlFlowBuilder builder, @NotNull VariableKey targetVariable) {
        this.builder = builder;
        this.targetVariable.addLast(targetVariable);
    }

    public static @NotNull Value computeValue(@NotNull ControlFlowBuilder builder, @NotNull RapidExpression expression) {
        RapidType type = getType(expression.getType());
        if (expression instanceof RapidLiteralExpression literalExpression) {
            Object value = literalExpression.getValue();
            if (value == null) {
                return new Value.Error();
            }
            return new Value.Constant(type, value);
        }
        if (expression instanceof RapidUnaryExpression unaryExpression) {
            if (unaryExpression.getExpression() instanceof RapidLiteralExpression literalExpression) {
                Object object = literalExpression.getValue();
                if (object == null) {
                    return new Value.Error();
                }
                IElementType elementType = unaryExpression.getSign().getNode().getElementType();
                if (object instanceof Boolean value) {
                    if (elementType != RapidTokenTypes.NOT_KEYWORD) {
                        return new Value.Error();
                    }
                    return new Value.Constant(type, !value);
                }
                if (elementType != RapidTokenTypes.MINUS) {
                    return new Value.Error();
                }
                if (object instanceof Long value) {
                    return new Value.Constant(type, -value);
                }
                if (object instanceof Double value) {
                    return new Value.Constant(type, -value);
                }
                return new Value.Error();
            }
        }
        if (expression instanceof RapidReferenceExpression referenceExpression) {
            RapidSymbol symbol = referenceExpression.getSymbol();
            if (symbol instanceof RapidRoutine routine) {
                String moduleName = routine instanceof PhysicalVisibleSymbol physicalSymbol ? ControlFlowElementVisitor.getModuleName(physicalSymbol) : null;
                String name = routine.getName();
                if (name == null) {
                    return new Value.Error();
                }
                return new Value.Variable.Constant(type, (moduleName != null ? moduleName + ":" : "") + name);
            }
            Value.Variable variable = computeVariable(builder, referenceExpression);
            return variable != null ? variable : new Value.Error();
        }
        return computeExpression(builder, expression);
    }

    public static @Nullable Value.Variable computeVariable(@NotNull ControlFlowBuilder builder, @NotNull RapidReferenceExpression expression) {
        RapidType type = getType(expression.getType());
        RapidSymbol symbol = expression.getSymbol();
        if (symbol == null) {
            return null;
        }
        String name = symbol.getName();
        if (name == null) {
            return null;
        }
        RapidExpression qualifier = expression.getQualifier();
        if (qualifier != null) {
            if (qualifier instanceof RapidReferenceExpression referenceExpression) {
                Value.Variable variable = computeVariable(builder, referenceExpression);
                if (variable != null && symbol instanceof RapidComponent component && component.getName() != null) {
                    return new Value.Variable.Component(type, variable, component.getName());
                }
            }
            return null;
        }
        if (symbol instanceof PhysicalField field) {
            PhysicalRoutine routine = PsiTreeUtil.getParentOfType(field, PhysicalRoutine.class);
            if (routine == null) {
                String moduleName = ControlFlowElementVisitor.getModuleName(field);
                return new Value.Variable.Field(type, moduleName, name);
            } else {
                Variable variable = builder.findVariable(name);
                if (variable == null) {
                    return null;
                }
                return new Value.Variable.Local(type, variable.index());
            }
        }
        if (symbol instanceof RapidField) {
            return new Value.Variable.Field(type, null, name);
        }
        if (symbol instanceof RapidParameter) {
            Argument argument = builder.findArgument(name);
            if (argument == null) {
                return null;
            }
            return new Value.Variable.Local(type, argument.index());
        }
        return null;
    }

    public static Value.@NotNull Variable computeExpression(@NotNull ControlFlowBuilder builder, @NotNull RapidExpression expression) {
        VariableKey variableKey = VariableKey.createVariable();
        return computeExpression(builder, variableKey, expression);
    }

    public static @NotNull Value.Variable computeExpression(@NotNull ControlFlowBuilder builder, @NotNull String name, @Nullable FieldType fieldType, @NotNull RapidExpression expression) {
        VariableKey variableKey = VariableKey.createField(name, fieldType);
        return computeExpression(builder, variableKey, expression);
    }

    private static Value.@NotNull Variable computeExpression(@NotNull ControlFlowBuilder builder, VariableKey variableKey, @NotNull RapidExpression expression) {
        ControlFlowExpressionVisitor visitor = new ControlFlowExpressionVisitor(builder, variableKey);
        expression.accept(visitor);
        Value.Variable result = variableKey.retrieve();
        if (result == null) {
            Value.Variable variable = builder.createVariable(variableKey, RapidType.ANYTYPE, null);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(expression, variable, new Expression.Variable(new Value.Error())));
            return variable;
        }
        return result;
    }

    private static @NotNull RapidType getType(@Nullable RapidType type) {
        return type != null ? type : RapidType.ANYTYPE;
    }

    public static void buildFunctionCall(@NotNull PsiElement element, @NotNull ControlFlowBuilder builder, @Nullable RapidRoutine routine, @NotNull List<RapidArgument> arguments, @NotNull Value routineValue, @Nullable Value.Variable returnVariable, @NotNull BasicBlock nextBlock) {
        Map<Integer, RapidArgument> map = calculateArguments(routine, arguments);
        buildFunctionCall(element, builder, routineValue, nextBlock, returnVariable, map);
    }

    private static void buildFunctionCall(@NotNull PsiElement element, @NotNull ControlFlowBuilder builder, @NotNull Value routine, @NotNull BasicBlock nextBlock, @Nullable Value.Variable returnVariable, @NotNull Map<Integer, RapidArgument> arguments) {
        Optional<Integer> index = arguments.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof RapidConditionalArgument)
                .map(Map.Entry::getKey)
                .findFirst();
        Map<Integer, Value> values = buildArguments(builder, arguments);
        if (index.isEmpty()) {
            builder.exitBasicBlock(new BranchingInstruction.CallInstruction(element, routine, values, returnVariable, nextBlock));
        } else {
            int value = index.get();
            VariableKey variableKey = VariableKey.createVariable();
            Value.Variable presentReturnVariable = builder.createVariable(variableKey, RapidType.BOOLEAN, null);
            BasicBlock ifBlock = builder.createBasicBlock();
            RapidType type = values.get(value).type();
            if (type == null) {
                builder.failScope(element);
                return;
            }
            Value.Constant presentRoutine = new Value.Constant(RapidType.ANYTYPE, "Present");
            Map<Integer, Value> presentArguments = Map.of(0, new Value.Variable.Local(type, value));
            builder.exitBasicBlock(new BranchingInstruction.CallInstruction(element, presentRoutine, presentArguments, presentReturnVariable, ifBlock));
            builder.enterBasicBlock(ifBlock);
            BasicBlock presentBlock = builder.createBasicBlock();
            BasicBlock missingBlock = builder.createBasicBlock();
            builder.exitBasicBlock(new BranchingInstruction.ConditionalBranchingInstruction(element, presentReturnVariable, presentBlock, missingBlock));
            builder.enterBasicBlock(presentBlock);
            RapidOptionalArgument optionalArgument = createOptionalArgument((RapidConditionalArgument) arguments.get(value));
            if (optionalArgument == null) {
                builder.failScope(element);
            } else {
                Map<Integer, RapidArgument> copy = new HashMap<>(arguments);
                copy.put(value, optionalArgument);
                buildFunctionCall(element, builder, routine, nextBlock, returnVariable, copy);
            }
            builder.enterBasicBlock(missingBlock);
            Map<Integer, RapidArgument> copy = new HashMap<>(arguments);
            copy.remove(value);
            buildFunctionCall(element, builder, routine, nextBlock, returnVariable, copy);
        }
    }

    private static @Nullable RapidOptionalArgument createOptionalArgument(@NotNull RapidConditionalArgument argument) {
        RapidElementFactory elementFactory = RapidElementFactory.getInstance(argument.getProject());
        RapidExpression value = argument.getArgument();
        if (value == null) {
            return null;
        }
        RapidExpression expression = elementFactory.createExpressionFromText("DUMMY(\\" + argument.getParameter().getText() + ":=" + value.getText() + ")");
        RapidFunctionCallExpression functionCallExpression = (RapidFunctionCallExpression) expression;
        return (RapidOptionalArgument) functionCallExpression.getArgumentList().getArguments().get(0);
    }

    private static @NotNull Map<Integer, Value> buildArguments(@NotNull ControlFlowBuilder builder, @NotNull Map<Integer, RapidArgument> arguments) {
        Map<Integer, Value> result = new HashMap<>();
        for (Map.Entry<Integer, RapidArgument> entry : arguments.entrySet()) {
            int index = entry.getKey();
            RapidArgument argument = entry.getValue();
            if (argument instanceof RapidRequiredArgument requiredArgument) {
                result.put(index, computeValue(builder, requiredArgument.getArgument()));
            } else if (argument instanceof RapidOptionalArgument) {
                result.put(index, null);
            } else if (argument instanceof RapidConditionalArgument conditionalArgument) {
                RapidExpression expression = conditionalArgument.getArgument();
                if (!(expression instanceof RapidReferenceExpression referenceExpression)) {
                    continue;
                }
                RapidSymbol symbol = referenceExpression.getSymbol();
                if (symbol == null) {
                    continue;
                }
                String name = symbol.getName();
                if (name == null) {
                    continue;
                }
                Argument variable = builder.findArgument(name);
                if (variable == null) {
                    continue;
                }
                result.put(index, new Value.Variable.Local(variable.type(), variable.index()));
            }
        }
        return result;
    }

    private static @NotNull Map<Integer, RapidArgument> calculateArguments(@Nullable RapidRoutine routine, @NotNull List<RapidArgument> arguments) {
        Map<Integer, RapidArgument> result = new HashMap<>();
        if (routine == null) {
            for (int i = 0; i < arguments.size(); i++) {
                result.put(i, arguments.get(i));
            }
            return result;
        }
        Map<RapidParameter, RapidArgument> map = calculateCorresponding(routine, arguments);
        List<? extends RapidParameterGroup> parameterGroups = routine.getParameters();
        if (parameterGroups == null) {
            return result;
        }
        List<? extends RapidParameter> parameters = parameterGroups.stream()
                .map(RapidParameterGroup::getParameters)
                .flatMap(List::stream)
                .toList();
        for (int i = 0; i < parameters.size(); i++) {
            RapidParameter parameter = parameters.get(i);
            if (map.containsKey(parameter)) {
                result.put(i, map.get(parameter));
            }
        }
        return result;
    }

    private static @NotNull Map<RapidParameter, RapidArgument> calculateCorresponding(@NotNull RapidRoutine routine, @NotNull List<RapidArgument> arguments) {
        Map<RapidParameter, RapidArgument> result = new HashMap<>();
        List<? extends RapidParameterGroup> parameterGroups = routine.getParameters();
        if (parameterGroups == null) {
            return result;
        }
        int parameterIndex = 0;
        for (RapidArgument argument : arguments) {
            RapidParameter parameter;
            RapidReferenceExpression expression = argument.getParameter();
            if (expression != null) {
                if (!(expression.getSymbol() instanceof RapidParameter symbol)) {
                    continue;
                }
                parameter = symbol;
            } else {
                RapidParameterGroup parameterGroup = parameterGroups.stream()
                        .filter(group -> !(group.isOptional()))
                        .toList().get(parameterIndex);
                parameter = parameterGroup.getParameters().get(0);
            }
            result.put(parameter, argument);
            if (!(parameter.getParameterGroup().isOptional())) {
                parameterIndex += 1;
            }
        }
        return result;
    }

    private @NotNull Value.Variable popVariable(@NotNull RapidType type, @Nullable Object initialValue) {
        VariableKey variableKey = targetVariable.removeLast();
        return builder.createVariable(variableKey, type, initialValue);
    }

    @Override
    public void visitAggregateExpression(@NotNull RapidAggregateExpression expression) {
        List<RapidExpression> expressions = expression.getExpressions();
        RapidType type = getType(expression.getType());
        List<Value> values = expressions.stream()
                .map(element -> computeValue(builder, element))
                .toList();
        if (values.contains(null)) {
            targetVariable.removeLast();
            return;
        }
        Value.Variable variable = popVariable(type, null);
        Expression.Aggregate aggregate = new Expression.Aggregate(values);
        builder.continueScope(new LinearInstruction.AssignmentInstruction(expression, variable, aggregate));
    }

    @Override
    public void visitUnaryExpression(@NotNull RapidUnaryExpression expression) {
        RapidExpression internal = expression.getExpression();
        RapidType type = getType(expression.getType());
        if (internal == null) {
            targetVariable.removeLast();
            return;
        }
        Value value = computeValue(builder, internal);
        Value.Variable variable = popVariable(type, null);
        Operator.UnaryOperator operator = getUnaryOperator(expression.getSign().getNode().getElementType());
        Expression computation = operator != null ? new Expression.Unary(operator, value) : new Expression.Variable(value);
        builder.continueScope(new LinearInstruction.AssignmentInstruction(expression, variable, computation));
    }

    @Override
    public void visitReferenceExpression(@NotNull RapidReferenceExpression expression) {
        RapidType type = getType(expression.getType());
        Value.Variable value = computeVariable(builder, expression);
        if (value == null) {
            targetVariable.removeLast();
            return;
        }
        Value.Variable variable = popVariable(type, null);
        builder.continueScope(new LinearInstruction.AssignmentInstruction(expression, variable, new Expression.Variable(value)));
    }

    @Override
    public void visitBinaryExpression(@NotNull RapidBinaryExpression expression) {
        Value leftValue = computeValue(builder, expression.getLeft());
        RapidType type = getType(expression.getType());
        RapidExpression right = expression.getRight();
        if (right == null) {
            targetVariable.removeLast();
            return;
        }
        IElementType elementType = expression.getSign().getNode().getElementType();
        if (NUMBER_TOKEN_SET.contains(elementType) && !(type.isAssignable(RapidType.NUMBER))) {
            if (STRING_TOKEN_SET.contains(elementType) && !(type.isAssignable(RapidType.STRING))) {
                targetVariable.removeLast();
                return;
            }
        }
        if (BOOLEAN_TOKEN_SET.contains(elementType) && !(type.isAssignable(RapidType.BOOLEAN))) {
            targetVariable.removeLast();
            return;
        }
        Value rightValue = computeValue(builder, right);
        Value.Variable variable = popVariable(type, null);
        Operator.BinaryOperator operator = getBinaryOperator(elementType);
        Expression.Binary binary = new Expression.Binary(operator, leftValue, rightValue);
        builder.continueScope(new LinearInstruction.AssignmentInstruction(expression, variable, binary));
    }

    private @Nullable Operator.UnaryOperator getUnaryOperator(@NotNull IElementType elementType) {
        if (elementType == RapidTokenTypes.MINUS) {
            return Operator.UnaryOperator.NEGATE;
        } else if (elementType == RapidTokenTypes.NOT_KEYWORD) {
            return Operator.UnaryOperator.NOT;
        } else {
            return null;
        }
    }

    private @NotNull Operator.BinaryOperator getBinaryOperator(@NotNull IElementType elementType) {
        if (elementType == RapidTokenTypes.PLUS) {
            return Operator.BinaryOperator.ADD;
        } else if (elementType == RapidTokenTypes.MINUS) {
            return Operator.BinaryOperator.SUBTRACT;
        } else if (elementType == RapidTokenTypes.ASTERISK) {
            return Operator.BinaryOperator.MULTIPLY;
        } else if (elementType == RapidTokenTypes.DIV) {
            return Operator.BinaryOperator.DIVIDE;
        } else if (elementType == RapidTokenTypes.DIV_KEYWORD) {
            return Operator.BinaryOperator.INTEGER_DIVIDE;
        } else if (elementType == RapidTokenTypes.MOD_KEYWORD) {
            return Operator.BinaryOperator.MODULO;
        } else if (elementType == RapidTokenTypes.LT) {
            return Operator.BinaryOperator.LESS_THAN;
        } else if (elementType == RapidTokenTypes.EQ) {
            return Operator.BinaryOperator.EQUAL_TO;
        } else if (elementType == RapidTokenTypes.GT) {
            return Operator.BinaryOperator.GREATER_THAN;
        } else if (elementType == RapidTokenTypes.LTGT) {
            return Operator.BinaryOperator.NOT_EQUAL_TO;
        } else if (elementType == RapidTokenTypes.LE) {
            return Operator.BinaryOperator.LESS_THAN_OR_EQUAL;
        } else if (elementType == RapidTokenTypes.GE) {
            return Operator.BinaryOperator.GREATER_THAN_OR_EQUAL;
        } else if (elementType == RapidTokenTypes.AND_KEYWORD) {
            return Operator.BinaryOperator.AND;
        } else if (elementType == RapidTokenTypes.XOR_KEYWORD) {
            return Operator.BinaryOperator.XOR;
        } else if (elementType == RapidTokenTypes.OR_KEYWORD) {
            return Operator.BinaryOperator.OR;
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void visitFunctionCallExpression(@NotNull RapidFunctionCallExpression expression) {
        RapidType type = getType(expression.getType());
        RapidSymbol symbol = expression.getReferenceExpression().getSymbol();
        if (!(symbol instanceof RapidRoutine routine) || routine.getName() == null) {
            targetVariable.removeLast();
            return;
        }
        String moduleName = routine instanceof PhysicalRoutine physicalRoutine ? ControlFlowElementVisitor.getModuleName(physicalRoutine) : null;
        String name = (moduleName != null ? moduleName + ":" : "") + routine.getName();
        BasicBlock nextBlock = builder.createBasicBlock();
        Value.Constant routineValue = new Value.Constant(RapidType.ANYTYPE, name);
        List<RapidArgument> arguments = expression.getArgumentList().getArguments();
        Value.Variable returnVariable = popVariable(type, null);
        buildFunctionCall(expression, builder, routine, arguments, routineValue, returnVariable, nextBlock);
        builder.enterBasicBlock(nextBlock);
    }

    @Override
    public void visitIndexExpression(@NotNull RapidIndexExpression expression) {
        RapidType type = getType(expression.getType());
        if (!(expression.getExpression() instanceof RapidReferenceExpression referenceExpression)) {
            targetVariable.removeLast();
            return;
        }
        Value.Variable variable = computeExpression(builder, referenceExpression);
        RapidArray array = expression.getArray();
        List<RapidExpression> dimensions = array.getDimensions();
        List<Value> values = dimensions.stream()
                .map(dimension -> computeValue(builder, dimension))
                .toList();
        if (values.stream().anyMatch(value -> !(value instanceof Value.Constant))) {
            targetVariable.removeLast();
            return;
        }
        List<Value.Constant> constants = values.stream()
                .map(value -> (Value.Constant) value)
                .toList();
        for (int i = 1; i < dimensions.size(); i++) {
            targetVariable.addLast(VariableKey.createVariable());
        }
        for (int i = 0; i < constants.size(); i++) {
            Value.Constant constant = constants.get(i);
            RapidType arrayType = type.createArrayType(type.getDimensions() - (i + 1));
            Value.Variable latest = popVariable(arrayType, null);
            Value.Variable indexVariable = new Value.Variable.Index(arrayType, variable, constant);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(expression, latest, new Expression.Variable(indexVariable)));
            variable = latest;
        }
    }

    @Override
    public void visitParenthesisedExpression(@NotNull RapidParenthesisedExpression expression) {
        RapidExpression internal = expression.getExpression();
        if (internal == null) {
            targetVariable.removeLast();
            return;
        }
        internal.accept(this);
    }

    @Override
    public void visitLiteralExpression(@NotNull RapidLiteralExpression expression) {
        RapidType type = getType(expression.getType());
        Object initialValue = expression.getValue();
        if (initialValue == null) {
            targetVariable.removeLast();
            return;
        }
        popVariable(type, initialValue);
    }
}

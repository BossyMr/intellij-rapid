package com.bossymr.rapid.language.flow.parser;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.ArgumentDescriptor;
import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.Variable;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.*;
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

    /**
     * Computes the value of the specified expression. If possible, a constant value will be returned, otherwise, the
     * expression will be evaluated to a variable which is returned.
     *
     * @param builder the builder.
     * @param expression the expression.
     * @return the value of the specified expression, or an {@link ErrorValue} if the expression could not evaluated.
     */
    public static @NotNull Value computeValue(@NotNull ControlFlowBuilder builder, @NotNull RapidExpression expression) {
        RapidType type = getType(expression.getType());
        if (expression instanceof RapidLiteralExpression literalExpression) {
            Object value = literalExpression.getValue();
            if (value == null) {
                return new ErrorValue();
            }
            return new ConstantValue(type, value);
        }
        if (expression instanceof RapidUnaryExpression unaryExpression) {
            if (unaryExpression.getExpression() instanceof RapidLiteralExpression literalExpression) {
                Object object = literalExpression.getValue();
                if (object == null) {
                    return new ErrorValue();
                }
                IElementType elementType = unaryExpression.getSign().getNode().getElementType();
                if (object instanceof Boolean value) {
                    if (elementType != RapidTokenTypes.NOT_KEYWORD) {
                        return new ErrorValue();
                    }
                    return new ConstantValue(type, !value);
                }
                if (elementType != RapidTokenTypes.MINUS) {
                    return new ErrorValue();
                }
                if (object instanceof Long value) {
                    return new ConstantValue(type, -value);
                }
                if (object instanceof Double value) {
                    return new ConstantValue(type, -value);
                }
                return new ErrorValue();
            }
        }
        if (expression instanceof RapidIndexExpression indexExpression) {
            ReferenceValue value = computeExpression(builder, indexExpression.getExpression());
            for (RapidExpression dimension : indexExpression.getArray().getDimensions()) {
                value = new IndexValue(value, computeValue(builder, dimension));
            }
            return value;
        }
        if (expression instanceof RapidReferenceExpression referenceExpression) {
            RapidSymbol symbol = referenceExpression.getSymbol();
            if (symbol instanceof RapidRoutine routine) {
                String moduleName = routine instanceof PhysicalVisibleSymbol physicalSymbol ? ControlFlowElementVisitor.getModuleName(physicalSymbol) : null;
                String name = routine.getName();
                if (name == null) {
                    return new ErrorValue();
                }
                return new ConstantValue(RapidType.STRING, (moduleName != null ? moduleName + ":" : "") + name);
            }
            ReferenceValue variable = computeVariable(builder, referenceExpression);
            return variable != null ? variable : new ErrorValue();
        }
        return computeExpression(builder, VariableKey.createVariable(), expression);
    }

    public static @Nullable ReferenceValue computeVariable(@NotNull ControlFlowBuilder builder, @NotNull RapidReferenceExpression expression) {
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
                ReferenceValue variable = computeVariable(builder, referenceExpression);
                if (variable != null && symbol instanceof RapidComponent component && component.getName() != null) {
                    return new ComponentValue(type, variable, component.getName());
                }
            }
            return null;
        }
        if (symbol instanceof PhysicalField field) {
            PhysicalRoutine routine = PsiTreeUtil.getParentOfType(field, PhysicalRoutine.class);
            if (routine == null) {
                String moduleName = ControlFlowElementVisitor.getModuleName(field);
                return new FieldValue(type, moduleName, name);
            } else {
                Variable variable = builder.findVariable(name);
                if (variable == null) {
                    return null;
                }
                return new VariableValue(variable);
            }
        }
        if(symbol instanceof RapidTargetVariable targetVariable) {
            Variable variable = builder.findVariable(targetVariable.getName());
            if(variable == null) {
                return null;
            }
            return new VariableValue(variable);
        }
        if (symbol instanceof RapidField) {
            return new FieldValue(type, null, name);
        }
        if (symbol instanceof RapidParameter) {
            Argument argument = builder.findArgument(name);
            if (argument == null) {
                return null;
            }
            return new VariableValue(argument);
        }
        return null;
    }

    public static @NotNull ReferenceValue computeExpression(@NotNull ControlFlowBuilder builder, @NotNull RapidExpression expression) {
        Value value = computeValue(builder, expression);
        if (value instanceof ReferenceValue referenceValue) {
            return referenceValue;
        }
        ReferenceValue variable = builder.createVariable(VariableKey.createVariable(), value.getType());
        builder.continueScope(new LinearInstruction.AssignmentInstruction(expression, variable, new ValueExpression(value)));
        return variable;
    }

    public static @NotNull ReferenceValue computeExpression(@NotNull ControlFlowBuilder builder, @NotNull String name, @Nullable FieldType fieldType, @NotNull RapidExpression expression) {
        VariableKey variableKey = VariableKey.createField(name, fieldType);
        return computeExpression(builder, variableKey, expression);
    }

    private static @NotNull ReferenceValue computeExpression(@NotNull ControlFlowBuilder builder, @NotNull VariableKey variableKey, @NotNull RapidExpression expression) {
        ControlFlowExpressionVisitor visitor = new ControlFlowExpressionVisitor(builder, variableKey);
        expression.accept(visitor);
        ReferenceValue result = variableKey.retrieve();
        if (result == null) {
            ReferenceValue variable = builder.createVariable(variableKey, RapidType.ANYTYPE);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(expression, variable, new ValueExpression(new ErrorValue())));
            return variable;
        }
        return result;
    }

    private static @NotNull RapidType getType(@Nullable RapidType type) {
        return type != null ? type : RapidType.ANYTYPE;
    }

    public static void buildFunctionCall(@NotNull PsiElement element, @NotNull ControlFlowBuilder builder, @NotNull List<RapidArgument> arguments, @NotNull Value routineValue, @Nullable ReferenceValue returnVariable, @NotNull BasicBlock nextBlock) {
        if (!(routineValue.getType().isAssignable(RapidType.STRING))) {
            throw new IllegalArgumentException("Cannot invoke: " + routineValue);
        }
        buildFunctionCall(element, builder, routineValue, nextBlock, returnVariable, getArguments(arguments));
    }

    private static void buildFunctionCall(@NotNull PsiElement element, @NotNull ControlFlowBuilder builder, @NotNull Value routine, @NotNull BasicBlock nextBlock, @Nullable ReferenceValue returnVariable, @NotNull Map<ArgumentDescriptor, RapidExpression> arguments) {
        Optional<ArgumentDescriptor> descriptor = arguments.keySet().stream()
                .filter(expression -> expression instanceof ArgumentDescriptor.Conditional)
                .findFirst();
        if (descriptor.isEmpty()) {
            Map<ArgumentDescriptor, Value> values = getArguments(builder, arguments);
            builder.exitBasicBlock(new BranchingInstruction.CallInstruction(element, routine, values, returnVariable, nextBlock));
        } else {
            ArgumentDescriptor value = descriptor.get();
            if (!(value instanceof ArgumentDescriptor.Conditional conditional)) {
                throw new IllegalStateException();
            }
            ReferenceValue presentReturnVariable = builder.createVariable(VariableKey.createVariable(), RapidType.BOOLEAN);
            BasicBlock ifBlock = builder.createBasicBlock();
            ConstantValue presentRoutine = new ConstantValue(RapidType.STRING, ":Present");
            Argument argument = builder.findArgument(conditional.name());
            Objects.requireNonNull(argument);
            Map<ArgumentDescriptor, Value> presentArguments = Map.of(new ArgumentDescriptor.Required(0), new VariableValue(argument));
            builder.exitBasicBlock(new BranchingInstruction.CallInstruction(element, presentRoutine, presentArguments, presentReturnVariable, ifBlock));
            builder.enterBasicBlock(ifBlock);
            BasicBlock presentBlock = builder.createBasicBlock();
            BasicBlock missingBlock = builder.createBasicBlock();
            builder.exitBasicBlock(new BranchingInstruction.ConditionalBranchingInstruction(element, presentReturnVariable, presentBlock, missingBlock));
            builder.enterBasicBlock(presentBlock);
            if (arguments.get(value) == null) {
                builder.failScope(element);
            } else {
                Map<ArgumentDescriptor, RapidExpression> copy = new HashMap<>(arguments);
                copy.remove(value);
                copy.put(new ArgumentDescriptor.Optional(conditional.name()), arguments.get(value));
                buildFunctionCall(element, builder, routine, nextBlock, returnVariable, copy);
            }
            builder.enterBasicBlock(missingBlock);
            Map<ArgumentDescriptor, RapidExpression> copy = new HashMap<>(arguments);
            copy.remove(value);
            buildFunctionCall(element, builder, routine, nextBlock, returnVariable, copy);
        }
    }

    private static @NotNull Map<ArgumentDescriptor, Value> getArguments(@NotNull ControlFlowBuilder builder, @NotNull Map<ArgumentDescriptor, RapidExpression> arguments) {
        Map<ArgumentDescriptor, Value> result = new HashMap<>();
        arguments.forEach((descriptor, expression) -> {
            Value value = expression != null ? computeValue(builder, expression) : null;
            result.put(descriptor, value);
        });
        return result;
    }

    private static @NotNull Map<ArgumentDescriptor, RapidExpression> getArguments(@NotNull List<RapidArgument> arguments) {
        Map<ArgumentDescriptor, RapidExpression> result = new HashMap<>();
        int index = 0;
        for (RapidArgument argument : arguments) {
            if (argument instanceof RapidRequiredArgument) {
                result.put(new ArgumentDescriptor.Required(index), argument.getArgument());
            } else {
                RapidReferenceExpression referenceExpression = argument.getParameter();
                Objects.requireNonNull(referenceExpression);
                String canonicalText = referenceExpression.getCanonicalText();
                ArgumentDescriptor argumentDescriptor = argument instanceof RapidConditionalArgument ? new ArgumentDescriptor.Conditional(canonicalText) : new ArgumentDescriptor.Optional(canonicalText);
                result.put(argumentDescriptor, argument.getArgument());
            }
            index += 1;
        }
        return result;
    }

    private @NotNull ReferenceValue popVariable(@NotNull RapidType type) {
        VariableKey variableKey = targetVariable.removeLast();
        return builder.createVariable(variableKey, type);
    }

    @Override
    public void visitAggregateExpression(@NotNull RapidAggregateExpression expression) {
        List<RapidExpression> expressions = expression.getExpressions();
        if (expression.getType() == null || expression.getType().getDimensions() <= 0 && !(expression.getType().getTargetStructure() instanceof RapidRecord)) {
            targetVariable.removeLast();
            return;
        }
        RapidType type = getType(expression.getType());
        List<Value> values = expressions.stream()
                .map(element -> computeValue(builder, element))
                .toList();
        if (values.contains(null)) {
            targetVariable.removeLast();
            return;
        }
        ReferenceValue variable = popVariable(type);
        AggregateExpression aggregate = new AggregateExpression(values);
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
        ReferenceValue variable = popVariable(type);
        UnaryOperator operator = getUnaryOperator(expression.getSign().getNode().getElementType());
        Expression computation = operator != null ? new UnaryExpression(operator, value) : new ValueExpression(value);
        builder.continueScope(new LinearInstruction.AssignmentInstruction(expression, variable, computation));
    }

    @Override
    public void visitReferenceExpression(@NotNull RapidReferenceExpression expression) {
        RapidType type = getType(expression.getType());
        ReferenceValue value = computeVariable(builder, expression);
        if (value == null) {
            targetVariable.removeLast();
            return;
        }
        ReferenceValue variable = popVariable(type);
        builder.continueScope(new LinearInstruction.AssignmentInstruction(expression, variable, new ValueExpression(value)));
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
        ReferenceValue variable = popVariable(type);
        BinaryOperator operator = getBinaryOperator(elementType);
        BinaryExpression binary = new BinaryExpression(operator, leftValue, rightValue);
        builder.continueScope(new LinearInstruction.AssignmentInstruction(expression, variable, binary));
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

    private @NotNull BinaryOperator getBinaryOperator(@NotNull IElementType elementType) {
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
            throw new IllegalStateException();
        }
    }

    @Override
    public void visitFunctionCallExpression(@NotNull RapidFunctionCallExpression expression) {
        RapidType type = getType(expression.getType());
        RapidSymbol symbol = expression.getReferenceExpression().getSymbol();
        ConstantValue routineValue;
        if (!(symbol instanceof RapidRoutine routine) || routine.getName() == null) {
            routineValue = new ConstantValue(RapidType.STRING, expression.getReferenceExpression().getCanonicalText());
        } else {
            String moduleName = routine instanceof PhysicalRoutine physicalRoutine ? ControlFlowElementVisitor.getModuleName(physicalRoutine) : null;
            String name = (moduleName != null ? moduleName + ":" : "") + routine.getName();
            routineValue = new ConstantValue(RapidType.STRING, name);
        }
        BasicBlock nextBlock = builder.createBasicBlock();
        List<RapidArgument> arguments = expression.getArgumentList().getArguments();
        ReferenceValue returnVariable = popVariable(type);
        buildFunctionCall(expression, builder, arguments, routineValue, returnVariable, nextBlock);
        builder.enterBasicBlock(nextBlock);
    }

    @Override
    public void visitIndexExpression(@NotNull RapidIndexExpression expression) {
        RapidType type = getType(expression.getType());
        if (!(expression.getExpression() instanceof RapidReferenceExpression referenceExpression)) {
            targetVariable.removeLast();
            return;
        }
        ReferenceValue variable = computeExpression(builder, referenceExpression);
        RapidArray array = expression.getArray();
        List<RapidExpression> dimensions = array.getDimensions();
        List<Value> values = dimensions.stream()
                .map(dimension -> computeValue(builder, dimension))
                .toList();
        if (values.stream().anyMatch(value -> !(value instanceof ConstantValue))) {
            targetVariable.removeLast();
            return;
        }
        for (Value value : values) {
            variable = new IndexValue(variable, value);
        }
        ReferenceValue referenceValue = popVariable(type.createArrayType(type.getDimensions() - values.size()));
        builder.continueScope(new LinearInstruction.AssignmentInstruction(expression, referenceValue, new ValueExpression(variable)));
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
        ReferenceValue referenceValue = popVariable(type);
        builder.continueScope(new LinearInstruction.AssignmentInstruction(expression, referenceValue, new ValueExpression(new ConstantValue(type, initialValue))));
    }
}

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
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ControlFlowExpressionVisitor extends RapidElementVisitor {

    private final @NotNull ControlFlowBuilder builder;
    private final @NotNull Deque<Optional<Expression>> stack = new ArrayDeque<>();

    public ControlFlowExpressionVisitor(@NotNull ControlFlowBuilder builder) {
        this.builder = builder;
    }

    public @NotNull Expression visit(@NotNull RapidExpression expression) {
        expression.accept(this);
        return stack.removeLast()
                .orElseGet(() -> builder.createVariable(expression.getType() != null ? expression.getType() : RapidPrimitiveType.ANYTYPE));
    }

    @Override
    public void visitAggregateExpression(@NotNull RapidAggregateExpression expression) {
        RapidType type = expression.getType();
        if (type == null) {
            stack.addLast(Optional.empty());
            return;
        }
        List<Expression> components = new ArrayList<>();
        for (RapidExpression component : expression.getExpressions()) {
            component.accept(this);
            Expression expr = stack.removeLast().orElse(null);
            if (expr == null) {
                stack.addLast(Optional.empty());
                return;
            }
            components.add(expr);
        }
        stack.addLast(Optional.of(new AggregateExpression(expression, type, components)));
    }

    @Override
    public void visitUnaryExpression(@NotNull RapidUnaryExpression expression) {
        RapidType type = expression.getType();
        IElementType elementType = expression.getSign().getNode().getElementType();
        UnaryOperator unaryOperator = getUnaryOperator(elementType);
        RapidExpression child = expression.getExpression();
        if (type == null || unaryOperator == null || child == null) {
            stack.addLast(Optional.empty());
            return;
        }
        child.accept(this);
        Expression component = stack.removeLast().orElse(null);
        if (component == null) {
            stack.addLast(Optional.empty());
            return;
        }
        stack.addLast(Optional.of(new UnaryExpression(expression, unaryOperator, component)));
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
    public void visitReferenceExpression(@NotNull RapidReferenceExpression expression) {
        RapidType type = expression.getType();
        RapidSymbol symbol = expression.getSymbol();
        if (type == null || symbol == null) {
            stack.addLast(Optional.empty());
            return;
        }
        String name = symbol.getName();
        if (name == null) {
            stack.addLast(Optional.empty());
            return;
        }
        RapidExpression qualifier = expression.getQualifier();
        if (symbol instanceof RapidComponent component) {
            if (!(qualifier instanceof RapidReferenceExpression referenceExpression)) {
                stack.addLast(Optional.empty());
                return;
            }
            referenceExpression.accept(this);
            Expression qualifierExpression = stack.removeLast().orElse(null);
            if (!(qualifierExpression instanceof ReferenceExpression qualifierReferenceExpression) || component.getName() == null) {
                stack.addLast(Optional.empty());
                return;
            }
            stack.addLast(Optional.of(new ComponentExpression(expression, type, qualifierReferenceExpression, component.getName())));
        } else if (symbol instanceof PhysicalField field) {
            PhysicalRoutine routine = PhysicalRoutine.getRoutine(field);
            if (routine == null) {
                String moduleName = ControlFlowElementVisitor.getModuleName(field);
                stack.addLast(Optional.of(new FieldExpression(expression, type, moduleName != null ? moduleName : "", name)));
            } else {
                Variable variable = builder.findVariable(name);
                if (variable == null) {
                    stack.addLast(Optional.empty());
                } else {
                    stack.addLast(Optional.of(new VariableExpression(expression, variable)));
                }
            }
        } else if (symbol instanceof RapidTargetVariable) {
            Variable variable = builder.findVariable(name);
            if(variable == null) {
                stack.addLast(Optional.empty());
            } else {
                stack.addLast(Optional.of(new VariableExpression(expression, variable)));
            }
        } else if (symbol instanceof RapidField) {
            stack.addLast(Optional.of(new FieldExpression(type, "", name)));
        } else if (symbol instanceof RapidParameter) {
            Argument argument = builder.findArgument(name);
            if (argument == null) {
                stack.addLast(Optional.empty());
            } else {
                stack.addLast(Optional.of(new VariableExpression(expression, argument)));
            }
        }
    }

    @Override
    public void visitBinaryExpression(@NotNull RapidBinaryExpression expression) {
        RapidType type = expression.getType();
        IElementType elementType = expression.getSign().getNode().getElementType();
        BinaryOperator binaryOperator = getBinaryOperator(elementType);
        RapidExpression rightExpr = expression.getRight();
        if (type == null || binaryOperator == null || rightExpr == null) {
            stack.addLast(Optional.empty());
            return;
        }
        expression.getLeft().accept(this);
        Expression left = stack.removeLast().orElse(null);
        rightExpr.accept(this);
        Expression right = stack.removeLast().orElse(null);
        if (left == null || right == null) {
            stack.addLast(Optional.empty());
            return;
        }
        stack.addLast(Optional.of(new BinaryExpression(expression, binaryOperator, left, right)));
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
    public void visitFunctionCallExpression(@NotNull RapidFunctionCallExpression expression) {
        RapidType type = expression.getType();
        RapidSymbol symbol = expression.getReferenceExpression().getSymbol();
        if (type == null || !(symbol instanceof RapidRoutine)) {
            stack.addLast(Optional.empty());
            return;
        }
        String routineName = symbol.getName();
        String moduleName = ControlFlowElementVisitor.getModuleName(symbol);
        if (routineName == null) {
            stack.addLast(Optional.empty());
            return;
        }
        String name = (moduleName != null ? moduleName : "") + ":" + routineName;
        Expression routineExpr = new ConstantExpression(RapidPrimitiveType.STRING, name);
        BasicBlock basicBlock = builder.createBasicBlock();
        List<RapidArgument> arguments = expression.getArgumentList().getArguments();
        Expression functionCall = createFunctionCall(expression, type, arguments, routineExpr, basicBlock);
        stack.addLast(Optional.of(functionCall));
    }

    public @NotNull Expression createFunctionCall(@NotNull PsiElement element, @NotNull RapidType type, @NotNull List<RapidArgument> arguments, @NotNull Expression routineName, @NotNull BasicBlock nextBlock) {
        ReferenceExpression expression = builder.createVariable(type);
        createFunctionCall(element, arguments, routineName, expression, nextBlock);
        return expression;
    }

    public void createFunctionCall(@NotNull PsiElement element, @NotNull List<RapidArgument> arguments, @NotNull Expression routineName, @Nullable ReferenceExpression returnVariable, @NotNull BasicBlock nextBlock) {
        Map<ArgumentDescriptor, RapidExpression> descriptors = getArgumentDescriptors(arguments);
        createFunctionCall(element, descriptors, routineName, returnVariable, nextBlock);
        builder.enterBasicBlock(nextBlock);
    }

    public void createFunctionCall(@NotNull PsiElement element, @NotNull Map<ArgumentDescriptor, RapidExpression> arguments, @NotNull Expression routineName, @Nullable ReferenceExpression returnVariable, @NotNull BasicBlock nextBlock) {
        Optional<ArgumentDescriptor.Conditional> optional = getFirstOptionalArgument(arguments);
        if (optional.isEmpty()) {
            Map<ArgumentDescriptor, ReferenceExpression> expressions = getArgumentExpressions(element, arguments);
            builder.exitBasicBlock(new BranchingInstruction.CallInstruction(element, routineName, expressions, returnVariable, nextBlock));
            return;
        }
        ArgumentDescriptor.Conditional argumentDescriptor = optional.orElseThrow();
        ReferenceExpression isPresentVariable = builder.createVariable(RapidPrimitiveType.BOOLEAN);
        ConstantExpression isPresentRoutineName = new ConstantExpression(RapidPrimitiveType.STRING, ":Present");
        Argument argument = Objects.requireNonNull(builder.findArgument(argumentDescriptor.name()));
        Map<ArgumentDescriptor, ReferenceExpression> map = Map.of(new ArgumentDescriptor.Required(0), new VariableExpression(argument));
        BasicBlock ifStatementBlock = builder.createBasicBlock();
        // isPresentVariable := :Present(_0 := argumentName)
        builder.exitBasicBlock(new BranchingInstruction.CallInstruction(element, isPresentRoutineName, map, isPresentVariable, ifStatementBlock));
        builder.enterBasicBlock(ifStatementBlock);
        BasicBlock presentBlock = builder.createBasicBlock();
        BasicBlock missingBlock = builder.createBasicBlock();
        builder.exitBasicBlock(new BranchingInstruction.ConditionalBranchingInstruction(element, isPresentVariable, presentBlock, missingBlock));
        builder.enterBasicBlock(presentBlock);
        RapidExpression expression = arguments.get(argumentDescriptor);
        if (expression == null) {
            builder.failScope(element);
        } else {
            Map<ArgumentDescriptor, RapidExpression> copy = new HashMap<>(arguments);
            copy.remove(argumentDescriptor);
            copy.put(new ArgumentDescriptor.Optional(argumentDescriptor.name()), expression);
            createFunctionCall(element, copy, routineName, returnVariable, nextBlock);
        }
        builder.enterBasicBlock(missingBlock);
        Map<ArgumentDescriptor, RapidExpression> copy = new HashMap<>(arguments);
        copy.remove(argumentDescriptor);
        createFunctionCall(element, copy, routineName, returnVariable, nextBlock);
    }

    private @NotNull Optional<ArgumentDescriptor.Conditional> getFirstOptionalArgument(@NotNull Map<ArgumentDescriptor, RapidExpression> arguments) {
        return arguments.keySet().stream()
                .filter(argument -> argument instanceof ArgumentDescriptor.Conditional)
                .map(argument -> (ArgumentDescriptor.Conditional) argument)
                .findFirst();
    }

    private @NotNull Map<ArgumentDescriptor, RapidExpression> getArgumentDescriptors(@NotNull List<RapidArgument> arguments) {
        Map<ArgumentDescriptor, RapidExpression> map = new HashMap<>();
        for (int i = 0; i < arguments.size(); i++) {
            RapidArgument argument = arguments.get(i);
            if (argument instanceof RapidRequiredArgument) {
                map.put(new ArgumentDescriptor.Required(i), argument.getArgument());
            } else {
                ArgumentDescriptor argumentDescriptor = getArgumentDescriptor(argument);
                map.put(argumentDescriptor, argument.getArgument());
            }
        }
        return map;
    }

    private static @NotNull ArgumentDescriptor getArgumentDescriptor(@NotNull RapidArgument argument) {
        RapidReferenceExpression referenceExpression = Objects.requireNonNull(argument.getParameter());
        String canonicalText = referenceExpression.getCanonicalText();
        ArgumentDescriptor argumentDescriptor;
        if (argument instanceof RapidConditionalArgument) {
            argumentDescriptor = new ArgumentDescriptor.Conditional(canonicalText);
        } else {
            argumentDescriptor = new ArgumentDescriptor.Optional(canonicalText);
        }
        return argumentDescriptor;
    }

    private @NotNull Map<ArgumentDescriptor, ReferenceExpression> getArgumentExpressions(@NotNull PsiElement element, @NotNull Map<ArgumentDescriptor, RapidExpression> arguments) {
        Map<ArgumentDescriptor, ReferenceExpression> map = new HashMap<>();
        arguments.forEach((descriptor, expression) -> {
            ReferenceExpression referenceExpression = null;
            if (expression != null) {
                expression.accept(this);
                Expression result = stack.removeLast().orElse(null);
                if (result instanceof ReferenceExpression) {
                    referenceExpression = ((ReferenceExpression) result);
                } else if (result != null) {
                    ReferenceExpression variable = builder.createVariable(result.getType());
                    builder.continueScope(new LinearInstruction.AssignmentInstruction(element, variable, result));
                    referenceExpression = variable;
                }
            }
            map.put(descriptor, referenceExpression);
        });
        return map;
    }

    @Override
    public void visitIndexExpression(@NotNull RapidIndexExpression expression) {
        RapidExpression array = expression.getExpression();
        array.accept(this);
        Expression variable = stack.removeLast().orElse(null);
        if (!(variable instanceof ReferenceExpression referenceExpression)) {
            stack.addLast(Optional.empty());
            return;
        }
        IndexExpression indexExpression = null;
        for (RapidExpression dimension : expression.getArray().getDimensions()) {
            dimension.accept(this);
            Expression component = stack.removeLast().orElse(null);
            if (component == null) {
                stack.addLast(Optional.empty());
                return;
            }
            indexExpression = new IndexExpression(expression, Objects.requireNonNullElse(indexExpression, referenceExpression), component);
        }
        stack.addLast(Optional.ofNullable(indexExpression));
    }

    @Override
    public void visitParenthesisedExpression(@NotNull RapidParenthesisedExpression expression) {
        RapidExpression component = expression.getExpression();
        if (component == null) {
            stack.addLast(Optional.empty());
            return;
        }
        component.accept(this);
    }

    @Override
    public void visitLiteralExpression(@NotNull RapidLiteralExpression expression) {
        RapidType type = expression.getType();
        Object object = expression.getValue();
        if (type == null || object == null) {
            stack.addLast(Optional.empty());
            return;
        }
        stack.addLast(Optional.of(new ConstantExpression(expression, type, object)));
    }
}

package com.bossymr.rapid.ide.editor.insight;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.editor.insight.fix.*;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.*;
import com.bossymr.rapid.language.symbol.resolve.ResolveService;
import com.bossymr.rapid.language.type.RapidArrayType;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.ColorUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class RapidAnnotator extends RapidElementVisitor implements Annotator {

    private AnnotationHolder annotationHolder;

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
        try {
            this.annotationHolder = annotationHolder;
            element.accept(this);
        } finally {
            this.annotationHolder = null;
        }
    }

    @Override
    public void visitModule(@NotNull PhysicalModule module) {
        checkModuleName(module);
        super.visitModule(module);
    }

    @Override
    public void visitAlias(@NotNull PhysicalAlias alias) {
        checkAliasType(alias);
        super.visitAlias(alias);
    }

    @Override
    public void visitRecord(@NotNull PhysicalRecord record) {
        super.visitRecord(record);
    }

    @Override
    public void visitComponent(@NotNull PhysicalComponent component) {
        super.visitComponent(component);
    }

    @Override
    public void visitStructure(@NotNull PhysicalStructure structure) {
        checkSymbolAfter(structure, Set.of(PhysicalField.class, PhysicalRoutine.class));
        super.visitStructure(structure);
    }

    @Override
    public void visitField(@NotNull PhysicalField field) {
        checkSymbolAfter(field, Set.of(PhysicalRoutine.class));
        RapidArray array = field.getArray();
        if (array != null) {
            checkDimensionDepth(array);
            checkDimensionExpression(array);
        }
        RapidExpression initializer = field.getInitializer();
        checkType(field.getType(), initializer);
        super.visitField(field);
    }

    @Override
    public void visitVariable(@NotNull PhysicalVariable variable) {
        super.visitVariable(variable);
    }

    @Override
    public void visitRoutine(@NotNull PhysicalRoutine routine) {
        PsiElement nameIdentifier = routine.getNameIdentifier();
        if (nameIdentifier != null) {
            RoutineType routineType = routine.getRoutineType();
            if (routine.getParameterList() == null && routineType != RoutineType.TRAP) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.missing.parameter.list"))
                                .range(nameIdentifier)
                                .create();
            }
            if (routine.getParameterList() != null && routineType == RoutineType.TRAP) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.unexpected.parameter.list"))
                                .range(nameIdentifier)
                                .create();
            }
            if (routine.getTypeElement() == null && routineType == RoutineType.FUNCTION) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.missing.return.type"))
                                .range(nameIdentifier)
                                .create();
            }
            if (routine.getTypeElement() != null && routineType != RoutineType.FUNCTION) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.return.type.unexpected"))
                                .range(nameIdentifier)
                                .create();
            }
        }
        super.visitRoutine(routine);
    }

    @Override
    public void visitSymbol(@NotNull PhysicalSymbol symbol) {
        checkIdentifierLength(symbol);
        checkDuplicateSymbol(symbol);
        super.visitSymbol(symbol);
    }

    @Override
    public void visitTypeElement(@NotNull RapidTypeElement typeElement) {
        RapidReferenceExpression expression = typeElement.getReferenceExpression();
        if (expression != null) {
            RapidSymbol symbol = expression.getSymbol();
            if (symbol != null && !(symbol instanceof RapidStructure)) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.reference.not.type", symbol.getPresentableName()))
                                .range(expression)
                                .create();
            }
        }
        super.visitTypeElement(typeElement);
    }

    @Override
    public void visitAttributeList(@NotNull RapidAttributeList attributeList) {
        checkAttributeList(attributeList);
        super.visitAttributeList(attributeList);
    }

    @Override
    public void visitParameterList(@NotNull RapidParameterList parameterList) {
        super.visitParameterList(parameterList);
    }

    @Override
    public void visitStatementList(@NotNull RapidStatementList statementList) {
        super.visitStatementList(statementList);
    }

    @Override
    public void visitParameterGroup(@NotNull PhysicalParameterGroup parameterGroup) {
        if (!(parameterGroup.isOptional()) && parameterGroup.getParameters().size() > 1) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.parameter.group.mutually.exclusive"))
                            .range(parameterGroup)
                            .create();
        }
        super.visitParameterGroup(parameterGroup);
    }

    @Override
    public void visitParameter(@NotNull PhysicalParameter parameter) {
        super.visitParameter(parameter);
    }

    @Override
    public void visitArgumentList(@NotNull RapidArgumentList argumentList) {
        if (argumentList.getParent() instanceof RapidCallExpression expression) {
            checkArgumentList(expression, argumentList);
        }
        super.visitArgumentList(argumentList);
    }

    private void checkArgumentList(@NotNull RapidCallExpression expression, @NotNull RapidArgumentList argumentList) {
        if (!(expression.getReferenceExpression() instanceof RapidReferenceExpression referenceExpression)) {
            return;
        }
        if (!(referenceExpression.getSymbol() instanceof RapidRoutine routine)) {
            return;
        }
        List<? extends RapidParameterGroup> parameterGroups = routine.getParameters();
        if (parameterGroups == null) {
            return;
        }
        Map<String, RapidParameter> parametersByName = new HashMap<>();
        for (RapidParameterGroup parameterGroup : parameterGroups) {
            for (RapidParameter parameter : parameterGroup.getParameters()) {
                parametersByName.put(parameter.getName(), parameter);
            }
        }
        Map<RapidArgument, RapidParameter> parameters = getParameters(routine.getPresentableName(), parameterGroups, argumentList);
        List<RapidArgument> arguments = argumentList.getArguments();
        for (RapidArgument argument : parameters.keySet()) {
            RapidParameter parameter = parameters.get(argument);
            if (argument instanceof RapidConditionalArgument || argument instanceof RapidOptionalArgument) {
                String text = argument.getParameter().getText();
                if (parameter == null) {
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.routine.parameter.not.found", text))
                                    .range(argument.getParameter())
                                    .create();
                } else {
                    if (!parameter.getParameterGroup().isOptional()) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.parameter.required", text))
                                        .range(argument)
                                        .create();
                    } else {
                        List<RapidArgument> previous = arguments.subList(0, arguments.indexOf(argument));
                        Optional<RapidArgument> previousExclusive = previous.stream().filter(arg -> parameters.containsKey(arg) && parameters.get(arg).getParameterGroup().equals(parameter.getParameterGroup())).findFirst();
                        if (previousExclusive.isPresent()) {
                            RapidParameter previousParameter = parameters.get(previousExclusive.get());
                            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.parameter.exclusive", previousParameter.getPresentableName(), parameter.getPresentableName()))
                                            .range(argument)
                                            .create();
                        }
                    }
                }
            }
            if (argument instanceof RapidRequiredArgument) {
                if (parameter != null && parameter.getName() != null && argument.getParameter() != null) {
                    String parameterName = argument.getParameter().getText();
                    if (!parameterName.equals(parameter.getName())) {
                        if (parametersByName.containsKey(parameterName)) {
                            if (!parametersByName.get(parameterName).getParameterGroup().isOptional()) {
                                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.parameter.order"))
                                                .range(argument)
                                                .create();
                            } else {
                                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.parameter.optional", parameterName))
                                                .range(argument)
                                                .create();
                            }
                        } else {
                            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.routine.parameter.not.found", parameterName))
                                            .range(argument)
                                            .create();
                        }
                    }
                }
            }
            if (parameter != null && argument instanceof RapidOptionalArgument && argument.getArgument() == null) {
                RapidType type = parameter.getType();
                if (type != null && !(type.getPresentableText().equals("switch"))) {
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.optional.parameter.not.switch", parameter.getName()))
                                    .range(argument)
                                    .create();
                }
            }
            if (parameter != null) {
                checkType(parameter.getType(), argument.getArgument());
            }
        }
    }

    private @NotNull Map<@NotNull RapidArgument, @Nullable RapidParameter> getParameters(@NotNull String routineName, @NotNull List<? extends RapidParameterGroup> parameterGroups, @NotNull RapidArgumentList argumentList) {
        List<RapidRequiredArgument> requiredArguments = argumentList.getArguments().stream()
                                                                    .filter(argument -> argument instanceof RapidRequiredArgument)
                                                                    .map(argument -> (RapidRequiredArgument) argument)
                                                                    .toList();
        List<? extends RapidParameter> requiredParameters = parameterGroups.stream()
                                                                           .filter(parameterGroup -> !parameterGroup.isOptional())
                                                                           .map(RapidParameterGroup::getParameters)
                                                                           .filter(parameters -> !parameters.isEmpty())
                                                                           .map(parameters -> parameters.get(0))
                                                                           .toList();
        Map<String, RapidParameter> parameters = new HashMap<>();
        for (RapidParameterGroup parameterGroup : parameterGroups) {
            for (RapidParameter parameter : parameterGroup.getParameters()) {
                String name = parameter.getName();
                if (name == null) {
                    continue;
                }
                parameters.put(name, parameter);
            }
        }
        if (requiredParameters.size() != requiredArguments.size()) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.routine.call.number.of.components", routineName, requiredParameters.size()))
                            .range(argumentList)
                            .create();
        }
        Map<RapidArgument, RapidParameter> result = new HashMap<>();
        for (RapidArgument argument : argumentList.getArguments()) {
            if (argument instanceof RapidRequiredArgument requiredArgument) {
                int index = requiredArguments.indexOf(requiredArgument);
                if (index >= requiredParameters.size()) {
                    result.put(argument, null);
                } else {
                    result.put(argument, requiredParameters.get(index));
                }
            } else if (argument instanceof RapidOptionalArgument || argument instanceof RapidConditionalArgument) {
                String parameterName = argument.getParameter().getText();
                result.put(argument, parameters.get(parameterName));
            }
        }
        return result;
    }

    @Override
    public void visitExpressionList(@NotNull RapidExpressionList expressionList) {
        super.visitExpressionList(expressionList);
    }

    @Override
    public void visitArgument(@NotNull RapidArgument argument) {
        super.visitArgument(argument);
    }

    @Override
    public void visitRequiredArgument(@NotNull RapidRequiredArgument argument) {
        super.visitRequiredArgument(argument);
    }

    @Override
    public void visitOptionalArgument(@NotNull RapidOptionalArgument argument) {
        super.visitOptionalArgument(argument);
    }

    @Override
    public void visitConditionalArgument(@NotNull RapidConditionalArgument argument) {
        super.visitConditionalArgument(argument);
    }

    @Override
    public void visitAggregateExpression(@NotNull RapidAggregateExpression expression) {
        RapidType type = expression.getType();
        if (type != null) {
            if (type.isArray() && type instanceof RapidArrayType arrayType) {
                RapidExpression lengthExpression = arrayType.getLength();
                List<RapidExpression> expressions = expression.getExpressions();
                if (lengthExpression instanceof RapidLiteralExpression literalExpression && literalExpression.getValue() instanceof Integer length) {
                    if (length != expressions.size()) {
                        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.array.length", type.getText(), length))
                                        .range(expression)
                                        .create();
                    }
                }
                RapidType componentType = type.createArrayType(type.getDimensions() - 1);
                for (RapidExpression component : expressions) {
                    checkType(componentType, component);
                }
            }
            if (type.isRecord() && type.getRootStructure() instanceof RapidRecord record) {
                List<? extends RapidComponent> components = record.getComponents();
                List<RapidExpression> expressions = expression.getExpressions();
                if (components.size() != expressions.size()) {
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.aggregate.number.of.components", record.getPresentableName(), components.size()))
                                    .range(expression)
                                    .create();
                } else {
                    for (int i = 0; i < components.size(); i++) {
                        RapidComponent component = components.get(i);
                        RapidExpression value = expressions.get(i);
                        checkType(component.getType(), value);
                    }
                }
            }
        }
        super.visitAggregateExpression(expression);
    }

    @Override
    public void visitUnaryExpression(@NotNull RapidUnaryExpression expression) {
        RapidExpression component = expression.getExpression();
        if (component != null) {
            if (component.getType() != null && expression.getType() == null) {
                String sign = expression.getSign().getText();
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.unary.not.applicable", sign, component.getType().getPresentableText()))
                                .range(expression)
                                .create();
            }
        }
        super.visitUnaryExpression(expression);
    }

    private void checkIsVariable(@Nullable RapidExpression expression) {
        if (!(expression instanceof RapidReferenceExpression referenceExpression)) {
            return;
        }
        RapidSymbol symbol = referenceExpression.getSymbol();
        if (symbol != null && !(symbol instanceof RapidVariable)) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.reference.not.variable", symbol.getPresentableName()))
                            .range(expression)
                            .create();
        }
    }

    @Override
    public void visitReferenceExpression(@NotNull RapidReferenceExpression expression) {
        checkReference(expression);
        super.visitReferenceExpression(expression);
    }

    @Override
    public void visitBinaryExpression(@NotNull RapidBinaryExpression expression) {
        RapidExpression left = expression.getLeft();
        RapidExpression right = expression.getRight();
        if (right != null) {
            if (left.getType() != null && right.getType() != null) {
                if (expression.getType() == null) {
                    String sign = expression.getSign().getText();
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.binary.not.applicable", sign, left.getType().getPresentableText(), right.getType().getPresentableText()))
                                    .range(expression)
                                    .create();
                }
            }
        }
        super.visitBinaryExpression(expression);
    }

    @Override
    public void visitFunctionCallExpression(@NotNull RapidFunctionCallExpression expression) {
        checkRoutineType(expression.getReferenceExpression(), RoutineType.FUNCTION);
        super.visitFunctionCallExpression(expression);
    }

    private void checkRoutineType(@NotNull RapidExpression expression, @NotNull RoutineType expected) {
        if (!(expression instanceof RapidReferenceExpression referenceExpression)) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.not.routine.of.type", expected.getPresentableText()))
                            .range(expression)
                            .create();
            return;
        }
        RapidSymbol symbol = referenceExpression.getSymbol();
        if (symbol == null) {
            return;
        }
        if (symbol instanceof RapidRoutine routine && routine.getRoutineType() == expected) {
            return;
        }
        AnnotationBuilder builder = annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.not.routine.of.type", expected.getPresentableText()))
                                                    .range(referenceExpression);
        if (symbol instanceof PhysicalRoutine routine) {
            builder = builder.withFix(new ChangeRoutineTypeFix(routine, expected));
        }
        builder.create();
    }

    @Override
    public void visitIndexExpression(@NotNull RapidIndexExpression expression) {
        RapidExpression variable = expression.getExpression();
        RapidType type = variable.getType();
        if (type != null && !(type.isArray())) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.array.type.expected", type.getPresentableText()))
                            .range(variable)
                            .create();
        }
        RapidSymbol symbol = variable instanceof RapidReferenceExpression referenceExpression ? referenceExpression.getSymbol() : null;
        if (symbol != null && !(symbol instanceof RapidVariable)) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.not.variable"))
                            .range(variable)
                            .create();
        }
        RapidArray array = expression.getArray();
        List<RapidExpression> dimensions = array.getDimensions();
        for (int i = 0; i < dimensions.size(); i++) {
            RapidExpression dimension = dimensions.get(i);
            checkType(RapidPrimitiveType.NUMBER, dimension);
            if (symbol != null && type != null && type.isArray() && type.getDimensions() < (i + 1)) {
                AnnotationBuilder builder = annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.array.wrong.degree", symbol.getPresentableName(), type.getDimensions()))
                                                            .range(dimension);
                if (symbol instanceof PhysicalVariable field) {
                    builder = builder.withFix(new ChangeVariableTypeFix(field, type.createArrayType(dimensions.size())));
                }
                builder.create();
            }
        }
        super.visitIndexExpression(expression);
    }

    @Override
    public void visitLiteralExpression(@NotNull RapidLiteralExpression expression) {
        super.visitLiteralExpression(expression);
    }

    @Override
    public void visitStatement(@NotNull RapidStatement statement) {
        super.visitStatement(statement);
    }

    @Override
    public void visitTestCaseStatement(@NotNull RapidTestCaseStatement statement) {
        RapidTestStatement testStatement = PsiTreeUtil.getParentOfType(statement, RapidTestStatement.class);
        Objects.requireNonNull(testStatement);
        RapidExpression expression = testStatement.getExpression();
        if (expression != null) {
            if (statement.getExpressions() != null) {
                for (RapidExpression condition : statement.getExpressions()) {
                    checkType(expression.getType(), condition);
                }
            }
        }
        super.visitTestCaseStatement(statement);
    }

    @Override
    public void visitAssignmentStatement(@NotNull RapidAssignmentStatement statement) {
        RapidExpression target = statement.getLeft();
        if (target != null) {
            checkType(target.getType(), statement.getRight());
        }
        super.visitAssignmentStatement(statement);
    }

    @Override
    public void visitProcedureCallStatement(@NotNull RapidProcedureCallStatement statement) {
        if (statement.isLate()) {
            checkType(RapidPrimitiveType.STRING, statement.getReferenceExpression());
        } else {
            RapidExpression expression = statement.getReferenceExpression();
            if (expression instanceof RapidReferenceExpression referenceExpression) {
                checkRoutineType(referenceExpression, RoutineType.PROCEDURE);
            }
        }
        super.visitProcedureCallStatement(statement);
    }

    @Override
    public void visitGotoStatement(@NotNull RapidGotoStatement statement) {
        RapidReferenceExpression expression = statement.getReferenceExpression();
        RapidSymbol symbol = expression != null ? expression.getSymbol() : null;
        if (symbol != null && !(symbol instanceof RapidLabelStatement)) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.not.label"))
                            .range(expression)
                            .create();
        }
        if (symbol instanceof RapidLabelStatement label) {
            RapidStatementList currentList = RapidStatementList.getStatementList(statement);
            RapidStatementList targetList = RapidStatementList.getStatementList(label);
            if (currentList != null && targetList != null && !(currentList.equals(targetList))) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.label.different.statement.list"))
                                .range(expression)
                                .create();
            }
        }
        super.visitGotoStatement(statement);
    }

    @Override
    public void visitConnectStatement(@NotNull RapidConnectStatement statement) {
        if (statement.getLeft() != null) {
            checkConnectTarget(statement.getLeft());
        }
        if (statement.getRight() != null) {
            checkRoutineType(statement.getRight(), RoutineType.TRAP);
        }
        super.visitConnectStatement(statement);
    }

    private void checkConnectTarget(@NotNull RapidExpression expression) {
        checkType(RapidPrimitiveType.NUMBER, expression);
        if (getConnectTargetExpression(expression) instanceof RapidReferenceExpression referenceExpression) {
            RapidSymbol symbol = referenceExpression.getSymbol();
            if (symbol instanceof PhysicalField field) {
                PhysicalRoutine routine = PhysicalRoutine.getRoutine(field);
                if (routine == null) {
                    return;
                }
            }
        }
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.connect.target.invalid"))
                        .range(expression)
                        .create();
    }

    private @NotNull RapidExpression getConnectTargetExpression(@NotNull RapidExpression expression) {
        if (expression instanceof RapidIndexExpression indexExpression) {
            RapidExpression variable = indexExpression.getExpression();
            return getConnectTargetExpression(variable);
        }
        if (expression instanceof RapidReferenceExpression referenceExpression) {
            RapidExpression qualifier = referenceExpression.getQualifier();
            if (qualifier != null) {
                return getConnectTargetExpression(qualifier);
            }
        }
        return expression;
    }

    @Override
    public void visitReturnStatement(@NotNull RapidReturnStatement statement) {
        RapidExpression expression = statement.getExpression();
        PhysicalRoutine routine = PhysicalRoutine.getRoutine(statement);
        if (routine != null) {
            if (routine.getRoutineType() == RoutineType.FUNCTION) {
                RapidType returnType = routine.getType();
                if (expression == null) {
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.return.value.missing"))
                                    .range(statement)
                                    .withFix(new ChangeRoutineTypeFix(routine, RoutineType.PROCEDURE))
                                    .withFix(new ChangeRoutineTypeFix(routine, RoutineType.TRAP))
                                    .create();
                } else {
                    checkType(returnType, expression);
                }
            } else if (expression != null) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.return.invalid", routine.getRoutineType().getPresentableText()))
                                .range(expression)
                                .withFix(new RemoveElementFix(expression, RapidBundle.message("quick.fix.family.remove.return.value")))
                                .withFix(new ChangeRoutineTypeFix(routine, RoutineType.FUNCTION))
                                .create();
            }
        }
        super.visitReturnStatement(statement);
    }

    @Override
    public void visitExitStatement(@NotNull RapidExitStatement statement) {
        super.visitExitStatement(statement);
    }

    @Override
    public void visitRaiseStatement(@NotNull RapidRaiseStatement statement) {
        RapidStatementList statementList = RapidStatementList.getStatementList(statement);
        Objects.requireNonNull(statementList);
        RapidExpression expression = statement.getExpression();
        if (statementList.getStatementListType() == BlockType.ERROR_CLAUSE) {
            if (expression != null) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.raise.value.invalid"))
                                .range(expression)
                                .withFix(new RemoveElementFix(expression, RapidBundle.message("quick.fix.family.remove.raise.value")))
                                .create();
            }
        } else {
            if (expression == null) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.raise.value.missing"))
                                .range(statement)
                                .create();
            } else {
                checkType(RapidPrimitiveType.NUMBER, expression);
            }
        }
        super.visitRaiseStatement(statement);
    }

    @Override
    public void visitRetryStatement(@NotNull RapidRetryStatement statement) {
        checkIfInErrorHandler(statement);
        super.visitRetryStatement(statement);
    }

    @Override
    public void visitTryNextStatement(@NotNull RapidTryNextStatement statement) {
        checkIfInErrorHandler(statement);
        super.visitTryNextStatement(statement);
    }

    private void checkIfInErrorHandler(@NotNull RapidStatement statement) {
        RapidStatementList statementList = RapidStatementList.getStatementList(statement);
        Objects.requireNonNull(statementList);
        if (statementList.getStatementListType() == BlockType.ERROR_CLAUSE) {
            return;
        }
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.statement.outside.error.handler"))
                        .range(statement)
                        .create();
    }

    @Override
    public void visitTargetVariable(@NotNull PhysicalTargetVariable variable) {
        super.visitTargetVariable(variable);
    }

    @Override
    public void visitIfStatement(@NotNull RapidIfStatement statement) {
        checkType(RapidPrimitiveType.BOOLEAN, statement.getCondition());
        super.visitIfStatement(statement);
    }

    @Override
    public void visitForStatement(@NotNull RapidForStatement statement) {
        checkType(RapidPrimitiveType.NUMBER, statement.getFromExpression());
        checkType(RapidPrimitiveType.NUMBER, statement.getToExpression());
        checkType(RapidPrimitiveType.NUMBER, statement.getStepExpression());
        super.visitForStatement(statement);
    }

    @Override
    public void visitWhileStatement(@NotNull RapidWhileStatement statement) {
        checkType(RapidPrimitiveType.BOOLEAN, statement.getCondition());
        super.visitWhileStatement(statement);
    }

    @Override
    public void visitTestStatement(@NotNull RapidTestStatement statement) {
        boolean foundDefault = false;
        for (RapidTestCaseStatement element : statement.getTestCaseStatements()) {
            if (element.isDefault()) {
                foundDefault = true;
                continue;
            }
            if (foundDefault) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.test.case.statement.after.default"))
                                .range(element.getFirstChild())
                                .withFix(new ReorderTestStatementFix(statement))
                                .create();
            }
        }
        super.visitTestStatement(statement);
    }

    @Override
    public void visitLabel(@NotNull RapidLabelStatement statement) {
        super.visitLabel(statement);
    }

    private void checkDimensionDepth(@NotNull RapidArray array) {
        if (array.getDimensions().size() > 3) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.array.too.many.dimensions"))
                            .range(array)
                            .create();
        }
    }

    private void checkDimensionExpression(@NotNull RapidArray array) {
        for (RapidExpression dimension : array.getDimensions()) {
            checkType(RapidPrimitiveType.NUMBER, dimension);
            if (dimension.isConstant()) {
                continue;
            }
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.expression.not.constant"))
                            .range(dimension)
                            .create();
        }
    }

    private void checkType(@Nullable RapidType requiredType, @Nullable RapidExpression expression) {
        checkIsVariable(expression);
        if (requiredType == null || expression == null) {
            return;
        }
        RapidType providedType = expression.getType();
        if (providedType == null) {
            return;
        }
        if (requiredType.isAssignable(providedType)) {
            return;
        }
        String message = RapidBundle.message("annotation.description.incompatible.types", requiredType.getPresentableText(), providedType.getPresentableText());
        String tooltip = RapidBundle.message("annotation.tooltip.incompatible.types",
                requiredType.getPresentableText(), providedType.getPresentableText(),
                "#" + ColorUtil.toHex(UIUtil.getContextHelpForeground()));
        AnnotationBuilder builder = annotationHolder.newAnnotation(HighlightSeverity.ERROR, message)
                                                    .tooltip(tooltip)
                                                    .range(expression);
        if (expression instanceof RapidReferenceExpression referenceExpression) {
            RapidSymbol symbol = referenceExpression.getSymbol();
            if (symbol instanceof PhysicalVariable variable && !(variable instanceof PhysicalTargetVariable)) {
                builder = builder.withFix(new ChangeVariableTypeFix(variable, requiredType));
            }
        }
        if (expression instanceof RapidFunctionCallExpression callExpression) {
            RapidSymbol symbol = callExpression.getReferenceExpression().getSymbol();
            if (symbol instanceof PhysicalRoutine routine) {
                builder = builder.withFix(new ChangeReturnTypeFix(routine, requiredType));
            }
        }
        PsiElement parent = expression.getParent();
        if (parent instanceof RapidReturnStatement statement) {
            PhysicalRoutine routine = PhysicalRoutine.getRoutine(statement);
            if (routine != null) {
                builder = builder.withFix(new ChangeReturnTypeFix(routine, providedType));
            }
        }
        if (parent instanceof RapidAssignmentStatement statement) {
            if (statement.getLeft() instanceof RapidReferenceExpression referenceExpression) {
                RapidSymbol symbol = referenceExpression.getSymbol();
                if (symbol instanceof PhysicalRoutine routine) {
                    builder = builder.withFix(new ChangeReturnTypeFix(routine, providedType));
                }
                if (symbol instanceof PhysicalVariable field) {
                    builder = builder.withFix(new ChangeVariableTypeFix(field, providedType));
                }
            }
        }
        builder.create();
    }

    private void checkReference(@NotNull RapidReferenceExpression expression) {
        PsiElement identifier = expression.getIdentifier();
        if (identifier == null) {
            return;
        }
        RapidSymbol symbol = expression.getSymbol();
        if (symbol != null) {
            return;
        }
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.reference.cannot.resolve.symbol", expression.getText()))
                        .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                        .range(identifier)
                        .create();
    }

    private void checkAliasType(@NotNull PhysicalAlias alias) {
        RapidType type = alias.getType();
        RapidTypeElement typeElement = alias.getTypeElement();
        if (typeElement == null || type == null) {
            return;
        }
        RapidStructure structure = type.getStructure();
        if (!(structure instanceof RapidAlias)) {
            return;
        }
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.alias.type.alias"))
                        .range(typeElement)
                        .create();
    }

    private void checkIdentifierLength(@NotNull PhysicalSymbol symbol) {
        PsiElement identifier = symbol.getNameIdentifier();
        if (identifier == null) {
            return;
        }
        if (identifier.getTextLength() > 32) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.identifier.length"))
                            .range(identifier)
                            .withFix(new InvokeRenameElementFix(symbol))
                            .create();
        }
    }

    public void checkSymbolAfter(@NotNull PhysicalVisibleSymbol symbol, @NotNull Set<Class<? extends PhysicalVisibleSymbol>> elements) {
        PsiElement identifier = symbol.getNameIdentifier();
        if (identifier == null) {
            return;
        }
        PhysicalModule module = PhysicalModule.getModule(symbol);
        if (module == null) {
            return;
        }
        PsiElement sibling = PsiTreeUtil.skipWhitespacesAndCommentsBackward(symbol);
        for (Class<? extends PhysicalVisibleSymbol> element : elements) {
            if (element.isInstance(sibling)) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.wrong.arrangement"))
                                .range(identifier)
                                .withFix(new SymbolArrangementOrderFix(module))
                                .create();
            }
        }
    }

    private void checkDuplicateSymbol(@NotNull PhysicalSymbol symbol) {
        PsiElement identifier = symbol.getNameIdentifier();
        String name = symbol.getName();
        if (identifier == null || name == null) {
            return;
        }
        ResolveService service = ResolveService.getInstance(symbol.getProject());
        List<RapidSymbol> symbols = service.findSymbols(symbol, name);
        if (symbols.isEmpty() || symbols.size() == 1) {
            return;
        }
        RapidSymbol duplicateSymbol = symbols.get(0);
        AnnotationBuilder annotationBuilder = annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.declaration.duplicate.symbol", name))
                                                              .range(identifier);
        if (duplicateSymbol instanceof PhysicalSymbol physicalSymbol) {
            annotationBuilder = annotationBuilder.withFix(new NavigateToAlreadyDeclaredSymbolFix(physicalSymbol));
        }
        annotationBuilder.create();
    }

    private void checkModuleName(@NotNull PhysicalModule module) {
        PsiElement identifier = module.getNameIdentifier();
        String name = module.getName();
        if (identifier == null || name == null) {
            return;
        }
        PsiFile containingFile = module.getContainingFile();
        String fileName = containingFile.getViewProvider().getVirtualFile().getNameWithoutExtension();
        if (name.equals(fileName)) {
            return;
        }
        AnnotationBuilder annotationBuilder = annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.name", name))
                                                              .range(identifier);
        if (containingFile instanceof RapidFile file) {
            List<PhysicalModule> modules = file.getModules();
            if (modules.size() > 1) {
                PsiDirectory containingDirectory = containingFile.getContainingDirectory();
                if (containingDirectory != null) {
                    PsiFile correctFile = containingDirectory.findFile(name + RapidFileType.DEFAULT_DOT_EXTENSION);
                    if (correctFile == null) {
                        annotationBuilder = annotationBuilder.withFix(new MoveModuleToSeparateFileFix(module));
                    }
                }
            }
            boolean canRenameFile = modules.stream().noneMatch(otherModule -> fileName.equals(otherModule.getName()));
            if (canRenameFile) {
                annotationBuilder = annotationBuilder.withFix(new RenameElementFix(containingFile, name + RapidFileType.DEFAULT_DOT_EXTENSION))
                                                     .withFix(new RenameElementFix(module, fileName));
            }
        }
        annotationBuilder.create();
    }

    private void checkAttributeList(@NotNull RapidAttributeList attributeList) {
        List<ASTNode> unsorted = List.of(attributeList.getNode().getChildren(ModuleType.TOKEN_SET));
        List<ASTNode> sorted = new ArrayList<>(unsorted);
        List<ModuleType> attributes = unsorted.stream().map(node -> ModuleType.getAttribute(node.getElementType())).toList();
        sorted.sort(Comparator.comparing(element -> ModuleType.getAttribute(element.getElementType()), Comparator.comparing(Enum::ordinal)));
        for (ASTNode element : unsorted) {
            ModuleType moduleType = ModuleType.getAttribute(element.getElementType());
            int index = unsorted.indexOf(element);
            if (sorted.indexOf(element) > index) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.attribute.order"))
                                .range(element)
                                .withFix(new ReorderModuleAttributeFix(attributeList))
                                .create();
            }
            int firstIndex = attributes.indexOf(moduleType);
            if (firstIndex != attributes.lastIndexOf(moduleType) && index == firstIndex) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.attribute.duplicate"))
                                .range(element)
                                .withFix(new RemoveModuleAttributeFix(element.getPsi()))
                                .create();
            }
            if (ModuleType.MUTUALLY_EXCLUSIVE.containsKey(moduleType)) {
                List<ModuleType> exlusiveList = ModuleType.MUTUALLY_EXCLUSIVE.get(moduleType);
                Optional<ModuleType> exclusiveType = attributes.stream().filter(exlusiveList::contains).findFirst();
                if (exclusiveType.isPresent()) {
                    ModuleType otherType = exclusiveType.orElseThrow();
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.attribute.mutually.exclusive", otherType.getText(), moduleType.getText()))
                                    .range(element)
                                    .withFix(new RemoveModuleAttributeFix(element.getPsi()))
                                    .withFix(new RemoveModuleAttributeFix(unsorted.get(attributes.indexOf(otherType)).getPsi()))
                                    .create();
                }
            }
        }
    }
}
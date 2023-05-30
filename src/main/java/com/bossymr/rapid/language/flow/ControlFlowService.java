package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.conditon.Expression;
import com.bossymr.rapid.language.flow.conditon.Operator;
import com.bossymr.rapid.language.flow.conditon.Value;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ControlFlowService extends RapidElementVisitor {

    private final @NotNull ControlFlowBuilder builder = new ControlFlowBuilder();

    public static @NotNull ControlFlow createControlFlow(@NotNull Module module) {
        ControlFlowService analyzer = new ControlFlowService();
        PsiManager manager = PsiManager.getInstance(module.getProject());
        for (VirtualFile virtualFile : FileTypeIndex.getFiles(RapidFileType.getInstance(), module.getModuleContentScope())) {
            PsiFile file = manager.findFile(virtualFile);
            if (file != null) {
                file.accept(analyzer);
            }
        }
        return analyzer.getControlFlow();
    }

    public static @NotNull ControlFlow createFunctionBlock(@NotNull PhysicalRoutine routine) {
        ControlFlowService analyzer = new ControlFlowService();
        routine.accept(analyzer);
        return analyzer.getControlFlow();
    }

    public @NotNull ControlFlow getControlFlow() {
        return builder.build();
    }

    @Override
    public void visitFile(@NotNull PsiFile psiFile) {
        if (psiFile instanceof RapidFile file) {
            for (PhysicalModule module : file.getModules()) {
                module.accept(this);
            }
        }
        super.visitFile(psiFile);
    }

    @Override
    public void visitModule(@NotNull PhysicalModule module) {
        for (PhysicalVisibleSymbol symbol : module.getSymbols()) {
            symbol.accept(this);
        }
        super.visitModule(module);
    }

    @Override
    public void visitField(@NotNull PhysicalField field) {
        String name = field.getName();
        RapidType type = field.getType();
        RapidExpression initializer = field.getInitializer();
        FieldType fieldType = field.getFieldType();
        if (name == null || type == null) {
            return;
        }
        if (fieldType == FieldType.CONSTANT) {
            if (initializer == null) {
                return;
            }
        }
        if (builder.isInsideScope()) {
            if (initializer != null) {
                computeExpressioninVariable(initializer, name, fieldType);
            } else {
                builder.pushVariable(VariableKey.createField(name, fieldType));
                builder.popVariable(type, null);
            }
        } else {
            CachedValuesManager.getCachedValue(field, () -> {
                String moduleName = getModuleName(field);
                if (moduleName == null) {
                    return CachedValueProvider.Result.createSingleDependency(null, PsiModificationTracker.MODIFICATION_COUNT);
                }
                Block block = builder.enterField(moduleName, name, type, fieldType);
                builder.enterScope(StatementListType.STATEMENT_LIST);
                Variable variable;
                if (initializer != null) {
                    variable = computeExpression(initializer);
                } else {
                    builder.pushVariable(VariableKey.createVariable());
                    variable = builder.popVariable(type, null);
                }
                if (builder.isInsideScope()) {
                    builder.exitScope(new BranchingInstruction.ReturnInstruction(new Value.Variable.Local(variable.index())));
                }
                builder.exitBlock();
                return CachedValueProvider.Result.createSingleDependency(block, PsiModificationTracker.MODIFICATION_COUNT);
            });
            super.visitField(field);
        }
    }

    @Override
    public void visitRoutine(@NotNull PhysicalRoutine routine) {
        CachedValuesManager.getCachedValue(routine, () -> {
            String moduleName = getModuleName(routine);
            String name = routine.getName();
            RapidType type = routine.getType();
            RoutineType routineType = routine.getRoutineType();
            if (moduleName == null || name == null) {
                return CachedValueProvider.Result.createSingleDependency(null, PsiModificationTracker.MODIFICATION_COUNT);
            }
            if (type != null) {
                if (routineType != RoutineType.FUNCTION) {
                    return CachedValueProvider.Result.createSingleDependency(null, PsiModificationTracker.MODIFICATION_COUNT);
                }
            } else {
                if (routineType == RoutineType.FUNCTION) {
                    return CachedValueProvider.Result.createSingleDependency(null, PsiModificationTracker.MODIFICATION_COUNT);
                }
            }
            if (routine.getParameterList() != null) {
                if (routineType == RoutineType.TRAP) {
                    return CachedValueProvider.Result.createSingleDependency(null, PsiModificationTracker.MODIFICATION_COUNT);
                }
            } else {
                if (routineType != RoutineType.TRAP) {
                    return CachedValueProvider.Result.createSingleDependency(null, PsiModificationTracker.MODIFICATION_COUNT);
                }
            }
            Block block = builder.enterFunction(moduleName, name, type, routineType, routineType != RoutineType.TRAP);
            List<PhysicalParameterGroup> parameters = routine.getParameters();
            if (parameters != null) {
                for (PhysicalParameterGroup parameterGroup : parameters) {
                    parameterGroup.accept(this);
                }
            }
            builder.enterScope(StatementListType.STATEMENT_LIST);
            for (PhysicalField field : routine.getFields()) {
                field.accept(this);
            }
            routine.getStatementList().accept(this);
            if (builder.isInsideScope()) {
                builder.exitScope(new BranchingInstruction.ReturnInstruction(null));
            }
            for (RapidStatementList statementList : routine.getStatementLists()) {
                if (statementList.getStatementListType() == StatementListType.STATEMENT_LIST) {
                    continue;
                }
                if (statementList.getStatementListType() == StatementListType.ERROR_CLAUSE) {
                    List<RapidExpression> expressions = statementList.getExpressions();
                    List<Value> values;
                    if (expressions == null) {
                        values = new ArrayList<>();
                    } else {
                        values = expressions.stream()
                                .map(this::getValue)
                                .toList();
                    }
                    builder.enterScope(values);
                } else {
                    builder.enterScope(statementList.getStatementListType());
                }
                statementList.accept(this);
                if (builder.isInsideScope()) {
                    builder.exitScope(new BranchingInstruction.ReturnInstruction(null));
                }
            }
            builder.exitBlock();
            return CachedValueProvider.Result.createSingleDependency(block, PsiModificationTracker.MODIFICATION_COUNT);
        });
        super.visitRoutine(routine);
    }

    @Override
    public void visitStatementList(@NotNull RapidStatementList statementList) {
        for (RapidStatement element : statementList.getStatements()) {
            element.accept(this);
        }
        super.visitStatementList(statementList);
    }

    @Override
    public void visitTestStatement(@NotNull RapidTestStatement statement) {
        RapidExpression completeExpression = statement.getExpression();
        if (completeExpression == null) {
            builder.failScope();
            return;
        }
        Value completeValue = getValue(completeExpression);
        if (completeValue == null) {
            builder.failScope();
            return;
        }
        Scope nextScope = null; // The scope which comes after this test statement
        List<RapidTestCaseStatement> testCaseStatements = statement.getTestCaseStatements();
        for (int i = 0; i < testCaseStatements.size(); i++) {
            RapidTestCaseStatement testCaseStatement = testCaseStatements.get(i);
            if (testCaseStatement.isDefault()) {
                Scope defaultScope = builder.createScope();
                builder.exitScope(new BranchingInstruction.UnconditionalBranchingInstruction(defaultScope));
                builder.enterScope(defaultScope);
                testCaseStatement.getStatements().accept(this);
                if (builder.isInsideScope()) {
                    if (nextScope == null) {
                        nextScope = builder.createScope();
                    }
                    builder.exitScope(new BranchingInstruction.UnconditionalBranchingInstruction(nextScope));
                    builder.enterScope(nextScope);
                }
                return;
            }
            List<RapidExpression> expressions = testCaseStatement.getExpressions();
            if (expressions == null) {
                builder.failScope();
                continue;
            }
            Scope onSuccess = builder.createScope();
            Scope onFailure = i + 1 == testCaseStatements.size() ? (nextScope != null ? nextScope = builder.createScope() : nextScope) : builder.createScope();
            builder.pushVariable(VariableKey.createVariable());
            Variable conditionVariable = builder.popVariable(RapidType.BOOLEAN, false);
            for (RapidExpression expression : expressions) {
                Value value = getValue(expression);
                if (value == null) {
                    builder.failScope();
                    continue;
                }
                builder.pushVariable(VariableKey.createVariable());
                Variable checkValue = builder.popVariable(RapidType.BOOLEAN, null);
                Expression equalExpression = new Expression.Binary(Operator.BinaryOperator.EQUAL_TO, completeValue, value);
                builder.continueScope(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(checkValue.index()), equalExpression));
                Expression conditionExpression = new Expression.Binary(Operator.BinaryOperator.OR, new Value.Variable.Local(conditionVariable.index()), new Value.Variable.Local(checkValue.index()));
                builder.continueScope(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(conditionVariable.index()), conditionExpression));
            }
            builder.exitScope(new BranchingInstruction.ConditionalBranchingInstruction(new Value.Variable.Local(conditionVariable.index()), onSuccess, onFailure));
            builder.enterScope(onSuccess);
            testCaseStatement.getStatements().accept(this);
            if (builder.isInsideScope()) {
                if (nextScope == null) {
                    nextScope = builder.createScope();
                }
                builder.exitScope(new BranchingInstruction.UnconditionalBranchingInstruction(nextScope));
            }
            builder.enterScope(onFailure);
        }
        super.visitTestStatement(statement);
    }

    @Override
    public void visitForStatement(@NotNull RapidForStatement statement) {
        RapidTargetVariable targetVariable = statement.getVariable();
        RapidExpression fromExpression = statement.getFromExpression();
        RapidExpression toExpression = statement.getToExpression();
        RapidStatementList statementList = statement.getStatementList();
        String variableName;
        if (targetVariable == null || (variableName = targetVariable.getName()) == null || fromExpression == null || toExpression == null || statementList == null) {
            builder.failScope();
            return;
        }
        Value toValue = getValue(toExpression);
        if (toValue == null) {
            builder.failScope();
            return;
        }
        Variable indexVariable = computeExpressioninVariable(fromExpression, variableName, null);
        Value stepValue;
        Scope loopScope = builder.createScope();
        Scope nextScope = builder.createScope();
        if (statement.getStepExpression() == null) {
            // Compute default step value -> +1 if from < to or -1 if from > to
            builder.pushVariable(VariableKey.createVariable());
            Variable stepVariable = builder.popVariable(RapidType.NUMBER, 0);
            stepValue = new Value.Variable.Local(stepVariable.index());
            builder.pushVariable(VariableKey.createVariable());
            Variable directionVariable = builder.popVariable(RapidType.BOOLEAN, false); // TRUE = ASCENDING FALSE = DESCENDING
            Expression directionExpression = new Expression.Binary(Operator.BinaryOperator.LESS_THAN, new Value.Variable.Local(indexVariable.index()), toValue);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(directionVariable.index()), directionExpression));
            Scope ascending = builder.createScope();
            Scope descending = builder.createScope();
            builder.exitScope(new BranchingInstruction.ConditionalBranchingInstruction(new Value.Variable.Local(directionVariable.index()), ascending, descending));
            builder.enterScope(ascending);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(stepVariable.index()), new Expression.Variable(new Value.Constant(1))));
            builder.exitScope(new BranchingInstruction.UnconditionalBranchingInstruction(loopScope));
            builder.enterScope(descending);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(stepVariable.index()), new Expression.Variable(new Value.Constant(-1))));
            builder.exitScope(new BranchingInstruction.UnconditionalBranchingInstruction(loopScope));
        } else {
            stepValue = getValue(statement.getStepExpression());
            if (stepValue == null) {
                builder.failScope();
                return;
            }
            builder.exitScope(new BranchingInstruction.UnconditionalBranchingInstruction(loopScope));
        }
        builder.enterScope(loopScope);
        statementList.accept(this);
        if (builder.isInsideScope()) {
            Expression indexExpression = new Expression.Binary(Operator.BinaryOperator.ADD, new Value.Variable.Local(indexVariable.index()), stepValue);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(indexVariable.index()), indexExpression));
            builder.pushVariable(VariableKey.createVariable());
            Variable conditionVariable = builder.popVariable(RapidType.BOOLEAN, false);
            Expression conditionExpression = new Expression.Binary(Operator.BinaryOperator.EQUAL_TO, new Value.Variable.Local(indexVariable.index()), toValue);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(conditionVariable.index()), conditionExpression));
            builder.exitScope(new BranchingInstruction.ConditionalBranchingInstruction(new Value.Variable.Local(conditionVariable.index()), nextScope, loopScope));
        }
        builder.enterScope(nextScope);
        super.visitForStatement(statement);
    }

    @Override
    public void visitWhileStatement(@NotNull RapidWhileStatement statement) {
        RapidExpression condition = statement.getCondition();
        RapidStatementList statementList = statement.getStatementList();
        if (condition == null || statementList == null) {
            builder.failScope();
            return;
        }
        Scope conditionScope = builder.createScope();
        Scope nextScope = builder.createScope();
        Scope loopScope = builder.createScope();
        builder.exitScope(new BranchingInstruction.UnconditionalBranchingInstruction(conditionScope));
        builder.enterScope(conditionScope);
        Value conditionValue = getValue(condition);
        if (conditionValue == null) {
            builder.failScope();
            return;
        }
        builder.exitScope(new BranchingInstruction.ConditionalBranchingInstruction(conditionValue, loopScope, nextScope));
        builder.enterScope(loopScope);
        statementList.accept(this);
        if (builder.isInsideScope()) {
            builder.exitScope(new BranchingInstruction.UnconditionalBranchingInstruction(conditionScope));
        }
        builder.enterScope(nextScope);
        super.visitWhileStatement(statement);
    }

    @Override
    public void visitIfStatement(@NotNull RapidIfStatement statement) {
        Scope nextScope = builder.createScope();
        visitIfStatement(statement, nextScope);
        builder.enterScope(nextScope);
        super.visitIfStatement(statement);
    }

    public void visitIfStatement(@NotNull RapidIfStatement statement, @NotNull Scope nextScope) {
        RapidStatementList thenBranch = statement.getThenBranch();
        if (thenBranch == null) {
            builder.failScope();
            return;
        }
        RapidStatementList elseBranch = statement.getElseBranch();
        Scope thenScope = builder.createScope();
        // If the scope has no else branch, go to the next scope instead.
        Scope elseScope = elseBranch != null ? builder.createScope() : nextScope;
        RapidExpression condition = Objects.requireNonNull(statement.getCondition());
        Value value = getValue(condition);
        if (value == null) {
            builder.failScope();
            return;
        }
        builder.exitScope(new BranchingInstruction.ConditionalBranchingInstruction(value, thenScope, elseScope));
        builder.enterScope(thenScope);
        thenBranch.accept(this);
        if (builder.isInsideScope()) {
            builder.exitScope(new BranchingInstruction.UnconditionalBranchingInstruction(nextScope));
        }
        if (elseBranch != null) {
            builder.enterScope(elseScope);
            if (elseBranch.getStatements().size() == 1) {
                if (elseBranch.getStatements().get(0) instanceof RapidIfStatement ifStatement) {
                    visitIfStatement(ifStatement, nextScope);
                    return;
                }
            }
            elseBranch.accept(this);
            if (builder.isInsideScope()) {
                builder.exitScope(new BranchingInstruction.UnconditionalBranchingInstruction(nextScope));
            }
        }
    }

    @Override
    public void visitProcedureCallStatement(@NotNull RapidProcedureCallStatement statement) {
        RapidExpression expression = statement.getReferenceExpression();
        Value value = getValue(expression);
        if (value == null) {
            builder.failScope();
            return;
        }
        Map<Integer, RapidArgument> argumentMap = new HashMap<>();
        if (expression instanceof RapidReferenceExpression referenceExpression) {
            if (!(referenceExpression.getSymbol() instanceof RapidRoutine routine)) {
                builder.failScope();
                return;
            }
            argumentMap = getArguments(routine, statement.getArgumentList().getArguments());
        } else {
            List<RapidArgument> arguments = statement.getArgumentList().getArguments();
            int size = arguments.size();
            for (int i = 0; i < size; i++) {
                RapidArgument argument = arguments.get(i);
                argumentMap.put(i, argument);
            }
        }
        Scope onSuccess = builder.createScope();
        buildFunctionCall(null, onSuccess, value, argumentMap);
        builder.enterScope(onSuccess);
        super.visitProcedureCallStatement(statement);
    }

    @Override
    public void visitConnectStatement(@NotNull RapidConnectStatement statement) {
        if (!(statement.getLeft() instanceof RapidReferenceExpression referenceExpression)) {
            builder.failScope();
            return;
        }
        Value.Variable variable = getVariable(referenceExpression);
        if (variable == null || !(statement.getRight() instanceof RapidReferenceExpression trapReference)) {
            builder.failScope();
            return;
        }
        if (!(trapReference instanceof PhysicalRoutine routine) || routine.getRoutineType() != RoutineType.TRAP || routine.getName() == null) {
            builder.failScope();
            return;
        }
        String routineName = routine.getName();
        String name = getModuleName(routine) + ":" + routineName;
        builder.continueScope(new LinearInstruction.ConnectInstruction(variable, new Value.Constant(name)));
        super.visitConnectStatement(statement);
    }

    @Override
    public void visitReturnStatement(@NotNull RapidReturnStatement statement) {
        RapidExpression expression = statement.getExpression();
        Value value;
        if (expression != null) {
            value = getValue(expression);
            if (value == null) {
                builder.failScope();
                return;
            }
        } else {
            value = null;
        }
        builder.exitScope(new BranchingInstruction.ReturnInstruction(value));
        super.visitReturnStatement(statement);
    }

    @Override
    public void visitGotoStatement(@NotNull RapidGotoStatement statement) {
        RapidReferenceExpression referenceExpression = statement.getReferenceExpression();
        if (!(referenceExpression instanceof RapidLabelStatement labelStatement)) {
            builder.failScope();
            return;
        }
        String name = labelStatement.getName();
        if (name == null) {
            builder.failScope();
            return;
        }
        builder.enterLabel(name);
        super.visitGotoStatement(statement);
    }

    @Override
    public void visitLabel(@NotNull RapidLabelStatement statement) {
        String name = statement.getName();
        if (name == null) {
            builder.failScope();
            return;
        }
        builder.enterLabel(name);
        super.visitLabel(statement);
    }

    @Override
    public void visitAssignmentStatement(@NotNull RapidAssignmentStatement statement) {
        if (!(statement.getLeft() instanceof RapidReferenceExpression referenceExpression)) {
            builder.failScope();
            return;
        }
        RapidExpression right = statement.getRight();
        Value.Variable variable = getVariable(referenceExpression);
        if (right == null || variable == null) {
            builder.failScope();
            return;
        }
        Value value = getValue(right);
        if (value == null) {
            builder.failScope();
            return;
        }
        builder.continueScope(new LinearInstruction.AssignmentInstruction(variable, new Expression.Variable(value)));
        super.visitAssignmentStatement(statement);
    }

    @Override
    public void visitExitStatement(@NotNull RapidExitStatement statement) {
        builder.exitScope(new BranchingInstruction.ExitInstruction());
        super.visitExitStatement(statement);
    }

    @Override
    public void visitRaiseStatement(@NotNull RapidRaiseStatement statement) {
        Value exception;
        RapidExpression expression = statement.getExpression();
        if (expression != null) {
            exception = getValue(expression);
            if (exception == null) {
                builder.failScope();
                return;
            }
        } else {
            exception = null;
        }
        builder.exitScope(new BranchingInstruction.ThrowInstruction(exception));
        super.visitRaiseStatement(statement);
    }

    @Override
    public void visitTryNextStatement(@NotNull RapidTryNextStatement statement) {
        builder.exitScope(new BranchingInstruction.TryNextInstruction());
        super.visitTryNextStatement(statement);
    }

    @Override
    public void visitRetryStatement(@NotNull RapidRetryStatement statement) {
        builder.exitScope(new BranchingInstruction.RetryInstruction());
        super.visitRetryStatement(statement);
    }

    @Override
    public void visitParameterGroup(@NotNull PhysicalParameterGroup parameterGroup) {
        ArgumentGroup argumentGroup = new ArgumentGroup(parameterGroup.isOptional(), new ArrayList<>());
        for (PhysicalParameter parameter : parameterGroup.getParameters()) {
            String name = parameter.getName();
            RapidType type = parameter.getType();
            if (name == null || type == null) {
                builder.failScope();
                return;
            }
            Argument argument = builder.createArgument(name, type, parameter.getParameterType());
            argumentGroup.arguments().add(argument);
        }
        builder.withArgumentGroup(argumentGroup);
        super.visitParameterGroup(parameterGroup);
    }

    private @Nullable String getModuleName(@NotNull PhysicalVisibleSymbol symbol) {
        PhysicalModule module = PsiTreeUtil.getParentOfType(symbol, PhysicalModule.class);
        if (module == null) {
            return null;
        }
        return module.getName();
    }

    /**
     * Computes the value of the specific expression and stores the result in a new variable.
     *
     * @param expression the expression.
     * @param name       the name of the variable.
     * @param fieldType  the type of the variable.
     * @return the variable.
     */
    public @NotNull Variable computeExpressioninVariable(@NotNull RapidExpression expression, @NotNull String
            name, @Nullable FieldType fieldType) {
        VariableKey variableKey = VariableKey.createField(name, fieldType);
        builder.pushVariable(variableKey);
        expression.accept(this);
        return variableKey.retrieve();
    }

    /**
     * Computes the value of the specific expression and stores the result in a new variable.
     *
     * @param expression the expression.
     * @return the variable.
     */
    public @NotNull Variable computeExpression(@NotNull RapidExpression expression) {
        VariableKey variableKey = VariableKey.createVariable();
        builder.pushVariable(variableKey);
        expression.accept(this);
        return variableKey.retrieve();
    }

    public @Nullable Value getValue(@NotNull RapidExpression expression) {
        if (expression instanceof RapidLiteralExpression literalExpression) {
            Object value = literalExpression.getValue();
            if (value == null) {
                return null;
            }
            return new Value.Constant(value);
        }
        if (expression instanceof RapidUnaryExpression unaryExpression) {
            RapidExpression internal = unaryExpression.getExpression();
            if (internal instanceof RapidLiteralExpression literalExpression) {
                Object value = literalExpression.getValue();
                if (value == null) {
                    return null;
                }
                if (value instanceof Boolean boolValue) {
                    if (unaryExpression.getSign().getNode().getElementType() != RapidTokenTypes.NOT_KEYWORD) {
                        return null;
                    }
                    return new Value.Constant(!boolValue);
                }
                if (value instanceof Long longValue) {
                    if (unaryExpression.getSign().getNode().getElementType() != RapidTokenTypes.MINUS) {
                        return null;
                    }
                    return new Value.Constant(-longValue);
                }
                if (value instanceof Double doubleValue) {
                    if (unaryExpression.getSign().getNode().getElementType() != RapidTokenTypes.MINUS) {
                        return null;
                    }
                    return new Value.Constant(-doubleValue);
                }
            }
        }
        if (expression instanceof RapidReferenceExpression referenceExpression) {
            RapidSymbol symbol = referenceExpression.getSymbol();
            if (symbol instanceof RapidRoutine routine) {
                String name = routine.getName();
                if (name != null) {
                    return new Value.Variable.Constant(name);
                }
            }
            return getVariable(referenceExpression);
        }
        Variable child = computeExpression(expression);
        return new Value.Variable.Local(child.index());
    }

    private @Nullable Value.Variable getVariable(@NotNull RapidReferenceExpression referenceExpression) {
        RapidSymbol symbol = referenceExpression.getSymbol();
        if (symbol == null) {
            return null;
        }
        String name = symbol.getName();
        if (name == null) {
            return null;
        }
        if (symbol instanceof PhysicalField field) {
            PhysicalRoutine routine = PsiTreeUtil.getParentOfType(field, PhysicalRoutine.class);
            if (routine == null) {
                String moduleName = getModuleName(field);
                return new Value.Variable.Field(moduleName, name);
            } else {
                Variable variable = builder.getVariableInBlock(name);
                if (variable == null) {
                    return null;
                }
                return new Value.Variable.Local(variable.index());
            }
        }
        if (symbol instanceof RapidField) {
            return new Value.Variable.Field(null, name);
        }
        if (symbol instanceof RapidParameter) {
            Argument parameter = builder.getArgumentInBlock(name);
            if (parameter == null) {
                throw new IllegalStateException();
            }
            return new Value.Variable.Local(parameter.index());
        }
        return null;
    }

    @Override
    public void visitAggregateExpression(@NotNull RapidAggregateExpression expression) {
        List<RapidExpression> expressions = expression.getExpressions();
        RapidType type = expression.getType();
        if (type == null) {
            builder.failScope();
            return;
        }
        Variable variable = builder.popVariable(type, null);
        List<Value> values = expressions.stream().map(this::getValue).toList();
        if (values.contains(null)) {
            builder.failScope();
            return;
        }
        Expression.Aggregate value = new Expression.Aggregate(values);
        builder.continueScope(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(variable.index()), value));
        super.visitAggregateExpression(expression);
    }

    @Override
    public void visitFunctionCallExpression(@NotNull RapidFunctionCallExpression expression) {
        RapidReferenceExpression referenceExpression = Objects.requireNonNull(expression.getReferenceExpression());
        RapidSymbol symbol = referenceExpression.getSymbol();
        RapidType type = expression.getType();
        if (!(symbol instanceof RapidRoutine routine) || routine.getName() == null || type == null) {
            builder.failScope();
            return;
        }
        String moduleName = symbol instanceof PhysicalVisibleSymbol physicalSymbol ? getModuleName(physicalSymbol) : null;
        String functionName = (moduleName != null ? moduleName + ":" : "") + symbol.getName();
        Map<Integer, RapidArgument> arguments = getArguments(routine, expression.getArgumentList().getArguments());
        Variable returnVariable = builder.popVariable(type, null);
        Scope onSuccess = builder.createScope();
        buildFunctionCall(new Value.Variable.Local(returnVariable.index()), onSuccess, new Value.Constant(functionName), arguments);
        builder.enterScope(onSuccess);
        super.visitFunctionCallExpression(expression);
    }

    /**
     * If the function call has conditional parameters, they are replaced with if statements.
     * <p>
     * {@code doSomething(\foo ? bar)}
     * <pre>{@code
     * IF Present(bar) THEN
     *     doSomething(\foo := bar)
     * ELSE
     *     doSomething()
     * ENDIF
     * }</pre>
     */
    private void buildFunctionCall(@Nullable Value.Variable returnVariable, @NotNull Scope
            onSuccess, @NotNull Value routine, @NotNull Map<Integer, RapidArgument> presentArguments) {
        List<Integer> integers = presentArguments.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof RapidConditionalArgument)
                .map(Map.Entry::getKey)
                .toList();
        Map<Integer, Value> values = buildArguments(presentArguments);
        if (integers.isEmpty()) {
            // The function call has no conditional parameters
            builder.exitScope(new BranchingInstruction.CallInstruction(routine, values, returnVariable, onSuccess));
        } else {
            Integer argument = integers.get(0);
            builder.pushVariable(VariableKey.createVariable());
            Variable variable = builder.popVariable(RapidType.BOOLEAN, null); // The variable to put whether the argument is present
            Scope ifStatement = builder.createScope(); // The scope with the if statement
            builder.exitScope(new BranchingInstruction.CallInstruction(new Value.Constant("Present"), Map.of(0, new Value.Variable.Local(argument)), new Value.Variable.Local(variable.index()), ifStatement));
            Scope onPresent = builder.createScope(); // The scope to call the method with the argument
            Scope onMissing = builder.createScope(); // The scope to call the method without the argument
            builder.enterScope(ifStatement);
            builder.exitScope(new BranchingInstruction.ConditionalBranchingInstruction(new Value.Variable.Local(variable.index()), onPresent, onMissing));
            builder.enterScope(onPresent);
            Map<Integer, RapidArgument> onPresentCopy = new HashMap<>(presentArguments); // Copy of parameters with the argument
            RapidArgument value = createArgument((RapidConditionalArgument) presentArguments.get(argument));
            if (value == null) {
                builder.failScope();
            } else {
                onPresentCopy.put(argument, value);
                buildFunctionCall(returnVariable, onSuccess, routine, onPresentCopy);
            }
            builder.enterScope(onMissing);
            Map<Integer, RapidArgument> onFailureCopy = new HashMap<>(presentArguments); // Copy of parameters without the argument
            onFailureCopy.remove(argument);
            buildFunctionCall(returnVariable, onSuccess, routine, onFailureCopy);
        }
    }

    private @Nullable RapidArgument createArgument(@NotNull RapidConditionalArgument argument) {
        RapidElementFactory elementFactory = RapidElementFactory.getInstance(argument.getProject());
        RapidExpression value = argument.getArgument();
        if (value == null) {
            return null;
        }
        RapidExpression expression = elementFactory.createExpressionFromText("DUMMY(\\" + argument.getParameter().getText() + ":=" + value.getText() + ")");
        return ((RapidFunctionCallExpression) expression).getArgumentList().getArguments().get(0);
    }

    private @NotNull Map<Integer, Value> buildArguments(@NotNull Map<Integer, RapidArgument> arguments) {
        Map<Integer, Value> values = new HashMap<>();
        for (Map.Entry<Integer, RapidArgument> entry : arguments.entrySet()) {
            Integer index = entry.getKey();
            RapidArgument argument = entry.getValue();
            if (argument instanceof RapidRequiredArgument requiredArgument) {
                values.put(index, getValue(requiredArgument.getArgument()));
            } else if (argument instanceof RapidOptionalArgument) {
                values.put(index, null);
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
                Argument variable = builder.getArgumentInBlock(name);
                if (variable == null) {
                    continue;
                }
                values.put(index, new Value.Variable.Local(variable.index()));
            }
        }
        return values;
    }

    private @NotNull Map<Integer, RapidArgument> getArguments(@NotNull RapidRoutine
                                                                      routine, @NotNull List<RapidArgument> arguments) {
        Map<RapidParameter, RapidArgument> map = getParameters(routine, arguments);
        Map<Integer, RapidArgument> sorted = new HashMap<>();
        Objects.requireNonNull(routine.getParameters());
        List<? extends RapidParameter> parameters = routine.getParameters().stream()
                .map(RapidParameterGroup::getParameters)
                .flatMap(List::stream)
                .toList();
        for (int i = 0; i < parameters.size(); i++) {
            RapidParameter parameter = parameters.get(i);
            sorted.put(i, map.get(parameter));
        }
        return sorted;
    }

    private @NotNull Map<RapidParameter, RapidArgument> getParameters(@NotNull RapidRoutine
                                                                              routine, @NotNull List<RapidArgument> arguments) {
        Map<RapidParameter, RapidArgument> map = new HashMap<>();
        int index = 0;
        for (RapidArgument argument : arguments) {
            RapidParameter parameter;
            RapidReferenceExpression referenceExpression = argument.getParameter();
            if (referenceExpression != null) {
                if (!(referenceExpression.getSymbol() instanceof RapidParameter symbol)) {
                    throw new IllegalStateException();
                }
                parameter = symbol;
            } else {
                List<? extends RapidParameterGroup> parameters = routine.getParameters();
                if (parameters == null) {
                    throw new IllegalStateException();
                }
                RapidParameterGroup group = parameters.stream()
                        .filter(parameterGroup -> !(parameterGroup.isOptional()))
                        .toList().get(index);
                parameter = group.getParameters().get(0);
            }
            map.put(parameter, argument);
            if (!(parameter.getParameterGroup().isOptional())) {
                index += 1;
            }
        }
        return map;
    }

    @Override
    public void visitIndexExpression(@NotNull RapidIndexExpression expression) {
        RapidArray array = expression.getArray();
        Variable computed = computeExpression(expression.getExpression());
        int size = computed.type().getDimensions();
        for (int i = 1; i < array.getDimensions().size(); i++) {
            builder.pushVariable(VariableKey.createVariable());
        }
        RapidType type = expression.getType();
        if (type == null) {
            builder.failScope();
            return;
        }
        List<RapidExpression> dimensions = array.getDimensions();
        for (int i = 0; i < dimensions.size(); i++) {
            RapidExpression dimension = dimensions.get(i);
            Value value = getValue(dimension);
            if (value == null) {
                builder.failScope();
                return;
            }
            computed = builder.popVariable(type.createArrayType(size - (i + 1)), null);
            Expression.Index index = new Expression.Index(new Value.Variable.Local(computed.index()), value);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(computed.index()), index));
        }
        super.visitIndexExpression(expression);
    }

    @Override
    public void visitParenthesisedExpression(@NotNull RapidParenthesisedExpression expression) {
        RapidExpression internal = expression.getExpression();
        if (internal == null) {
            builder.failScope();
            return;
        }
        internal.accept(this);
        super.visitParenthesisedExpression(expression);
    }

    @Override
    public void visitBinaryExpression(@NotNull RapidBinaryExpression expression) {
        Value left = getValue(expression.getLeft());
        RapidType type = expression.getType();
        RapidExpression expressionRight = expression.getRight();
        if (expressionRight == null || type == null) {
            builder.failScope();
            return;
        }
        Value right = getValue(expressionRight);
        if (left == null || right == null) {
            builder.failScope();
            return;
        }
        Variable variable = builder.popVariable(type, null);
        Operator.BinaryOperator operator;
        IElementType elementType = expression.getSign().getNode().getElementType();
        if (elementType == RapidTokenTypes.PLUS) {
            operator = Operator.BinaryOperator.ADD;
        } else if (elementType == RapidTokenTypes.MINUS) {
            operator = Operator.BinaryOperator.SUBTRACT;
        } else if (elementType == RapidTokenTypes.ASTERISK) {
            operator = Operator.BinaryOperator.MULTIPLY;
        } else if (elementType == RapidTokenTypes.DIV) {
            operator = Operator.BinaryOperator.DIVIDE;
        } else if (elementType == RapidTokenTypes.DIV_KEYWORD) {
            operator = Operator.BinaryOperator.DIVIDE;
        } else if (elementType == RapidTokenTypes.MOD_KEYWORD) {
            operator = Operator.BinaryOperator.MODULO;
        } else if (elementType == RapidTokenTypes.LT) {
            operator = Operator.BinaryOperator.LESS_THAN;
        } else if (elementType == RapidTokenTypes.LE) {
            operator = Operator.BinaryOperator.LESS_THAN_OR_EQUAL_TO;
        } else if (elementType == RapidTokenTypes.EQ) {
            operator = Operator.BinaryOperator.EQUAL_TO;
        } else if (elementType == RapidTokenTypes.GE) {
            operator = Operator.BinaryOperator.GREATER_THAN_OR_EQUAL_TO;
        } else if (elementType == RapidTokenTypes.GT) {
            operator = Operator.BinaryOperator.GREATER_THAN;
        } else if (elementType == RapidTokenTypes.LTGT) {
            operator = Operator.BinaryOperator.NOT_EQUAL_TO;
        } else if (elementType == RapidTokenTypes.AND_KEYWORD) {
            operator = Operator.BinaryOperator.AND;
        } else if (elementType == RapidTokenTypes.XOR_KEYWORD) {
            operator = Operator.BinaryOperator.EXLUSIVE_OR;
        } else if (elementType == RapidTokenTypes.OR_KEYWORD) {
            operator = Operator.BinaryOperator.OR;
        } else if (elementType == RapidTokenTypes.NOT_KEYWORD) {
            operator = Operator.BinaryOperator.NOT;
        } else {
            throw new IllegalStateException();
        }
        Expression.Binary binary = new Expression.Binary(operator, left, right);
        LinearInstruction instruction = new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(variable.index()), binary);
        builder.continueScope(instruction);
        super.visitBinaryExpression(expression);
    }

    @Override
    public void visitReferenceExpression(@NotNull RapidReferenceExpression expression) {
        RapidType type = expression.getType();
        if (type == null) {
            builder.failScope();
            return;
        }
        Variable variable = builder.popVariable(type, null);
        Value value = getVariable(expression);
        if (value == null) {
            builder.failScope();
            return;
        }
        builder.continueScope(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(variable.index()), new Expression.Variable(value)));
        super.visitReferenceExpression(expression);
    }

    @Override
    public void visitUnaryExpression(@NotNull RapidUnaryExpression expression) {
        RapidExpression internal = expression.getExpression();
        RapidType type = expression.getType();
        if (type == null || internal == null) {
            builder.failScope();
            return;
        }
        Value value = getValue(internal);
        if (value == null) {
            builder.failScope();
            return;
        }
        Variable variable = builder.popVariable(type, null);
        IElementType elementType = expression.getSign().getNode().getElementType();
        Operator.UnaryOperator operator;
        if (elementType == RapidTokenTypes.MINUS) {
            operator = Operator.UnaryOperator.NEGATE;
        } else if (elementType == RapidTokenTypes.NOT_KEYWORD) {
            operator = Operator.UnaryOperator.NOT;
        } else {
            builder.continueScope(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(variable.index()), new Expression.Variable(value)));
            return;
        }
        builder.continueScope(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(variable.index()), new Expression.Unary(operator, value)));
        super.visitUnaryExpression(expression);
    }

    @Override
    public void visitLiteralExpression(@NotNull RapidLiteralExpression expression) {
        Object value = expression.getValue();
        RapidType type = expression.getType();
        if (type == null || value == null) {
            builder.failScope();
            return;
        }
        builder.popVariable(type, value);
        super.visitLiteralExpression(expression);
    }
}

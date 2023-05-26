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
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ControlFlowVisitor extends RapidElementVisitor {

    private final @NotNull ControlFlow controlFlow;

    /**
     * The block which is currently being processed.
     */
    private Block currentBlock;

    /**
     * The current scope to put possible instructions for an expression.
     */
    private Scope currentScope;

    private Map<String, Scope> currentLabels;

    /**
     * A stack of fields to put the result of an expression.
     * <p>
     * The expression: {@code A*(B*C)} would need to be converted into:
     * <pre>{@code
     *      D = B * C
     *      E = A * D
     * }</pre>
     * The parent of the above expression would put {@code E} into the stack and visit the expression. That method would
     * insert {@code D} into the stack and visit {@code B * C}.
     */
    private Deque<VariableKey> variableStack;

    private Variable lastVariable;

    public ControlFlowVisitor() {
        this.controlFlow = new ControlFlow();
    }

    public static @NotNull ControlFlow createControlFlow(@NotNull Module module) {
        ControlFlowVisitor analyzer = new ControlFlowVisitor();
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
        ControlFlowVisitor analyzer = new ControlFlowVisitor();
        routine.accept(analyzer);
        return analyzer.getControlFlow();
    }

    public @NotNull ControlFlow getControlFlow() {
        return controlFlow;
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
    public void visitRoutine(@NotNull PhysicalRoutine routine) {
        String moduleName = getModuleName(routine);
        String name = Objects.requireNonNull(routine.getName());
        if (routine.getRoutineType() == RoutineType.FUNCTION && routine.getType() == null) {
            throw new IllegalStateException();
        }
        if (routine.getRoutineType() != RoutineType.FUNCTION && routine.getType() != null) {
            throw new IllegalStateException();
        }
        if (routine.getRoutineType() != RoutineType.TRAP && routine.getParameterList() == null) {
            throw new IllegalStateException();
        }
        if (routine.getRoutineType() == RoutineType.TRAP && routine.getParameterList() != null) {
            throw new IllegalStateException();
        }
        List<ArgumentGroup> argumentGroups = routine.getRoutineType() != RoutineType.TRAP ? new ArrayList<>() : null;
        currentBlock = new Block.FunctionBlock(moduleName, name, routine.getType(), new ArrayList<>(), new HashMap<>(), new HashMap<>(), argumentGroups, routine.getRoutineType());
        for (RapidStatementList statementList : routine.getStatementLists()) {
            Scope scope;
            ScopeType scopeType = switch (statementList.getStatementListType()) {
                case STATEMENT_LIST -> ScopeType.REGULAR;
                case ERROR_CLAUSE -> ScopeType.ERROR;
                case UNDO_CLAUSE -> ScopeType.UNDO;
                case BACKWARD_CLAUSE -> ScopeType.BACKWARD;
            };
            if (statementList.getStatementListType() == StatementListType.ERROR_CLAUSE) {
                List<RapidExpression> expressions = statementList.getExpressions();
                List<Value> values = expressions != null ? expressions.stream().map(this::getValue).toList() : null;
                scope = currentBlock.newErrorScope(values);
            } else {
                scope = currentBlock.newEntryScope(scopeType);
            }
            currentBlock.entry().put(scopeType, scope);
        }
        currentScope = currentBlock.entry().get(ScopeType.REGULAR);
        currentLabels = new HashMap<>();
        List<PhysicalParameterGroup> parameters = routine.getParameters();
        if (parameters != null) {
            parameters.forEach(element -> element.accept(this));
        }
        routine.getFields().forEach(element -> element.accept(this));
        routine.getStatementLists().forEach(element -> element.accept(this));
        for (Scope value : currentBlock.entry().values()) {
            if (currentScope != null) {
                value.instructions().add(new BranchingInstruction.ReturnInstruction(null));
            }
        }
        controlFlow.insertBlock(currentBlock);
        currentBlock = null;
        currentLabels = null;
        currentScope = null;
        super.visitRoutine(routine);
    }

    @Override
    public void visitStatementList(@NotNull RapidStatementList statementList) {
        statementList.getStatements().forEach(element -> element.accept(this));
        super.visitStatementList(statementList);
    }

    @Override
    public void visitTestStatement(@NotNull RapidTestStatement statement) {
        Scope nextScope = currentBlock.newScope();
        RapidExpression completeExpression = Objects.requireNonNull(statement.getExpression());
        Value completeValue = getValue(completeExpression);
        for (RapidTestCaseStatement testCaseStatement : statement.getTestCaseStatements()) {
            if (testCaseStatement.isDefault()) {
                currentScope.instructions().add(new BranchingInstruction.UnconditionalBranchingInstruction(nextScope));
                currentScope = nextScope;
                return;
            }
            Scope onSuccess = currentBlock.newScope();
            Scope onFailure = currentBlock.newScope();
            Variable conditionVariable = currentBlock.newVariable(new Value.Constant(false), RapidType.BOOLEAN);
            for (RapidExpression expression : Objects.requireNonNull(testCaseStatement.getExpressions())) {
                Variable checkValue = currentBlock.newVariable(null, RapidType.BOOLEAN);
                Expression equalExpression = new Expression.Binary(Operator.BinaryOperator.EQUAL_TO, completeValue, getValue(expression));
                currentScope.instructions().add(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(checkValue.index()), equalExpression));
                Expression conditionExpression = new Expression.Binary(Operator.BinaryOperator.OR, new Value.Variable.Local(conditionVariable.index()), new Value.Variable.Local(checkValue.index()));
                currentScope.instructions().add(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(conditionVariable.index()), conditionExpression));
            }
            currentScope.instructions().add(new BranchingInstruction.ConditionalBranchingInstruction(new Value.Variable.Local(conditionVariable.index()), onSuccess, onFailure));
            currentScope = onSuccess;
            testCaseStatement.getStatements().accept(this);
            if (currentScope != null) {
                currentScope.instructions().add(new BranchingInstruction.UnconditionalBranchingInstruction(nextScope));
            }
            currentScope = onFailure;
        }
        super.visitTestStatement(statement);
    }

    @Override
    public void visitForStatement(@NotNull RapidForStatement statement) {
        RapidTargetVariable targetVariable = Objects.requireNonNull(statement.getVariable());
        RapidExpression fromExpression = Objects.requireNonNull(statement.getFromExpression());
        Variable indexVariable = computeExpressioninVariable(fromExpression, Objects.requireNonNull(targetVariable.getName()), null);
        Value toValue = getValue(Objects.requireNonNull(statement.getToExpression()));
        Value stepValue;
        Scope loopScope = currentBlock.newScope();
        Scope nextScope = currentBlock.newScope();
        if (statement.getStepExpression() == null) {
            // Compute default step value -> +1 if from < to or -1 if from > to
            Variable stepVariable = currentBlock.newVariable(null, RapidType.NUMBER);
            stepValue = new Value.Variable.Local(stepVariable.index());
            Variable directionVariable = currentBlock.newVariable(null, RapidType.BOOLEAN); // TRUE = ASCENDING FALSE = DESCENDING
            Expression directionExpression = new Expression.Binary(Operator.BinaryOperator.LESS_THAN, new Value.Variable.Local(indexVariable.index()), toValue);
            currentScope.instructions().add(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(directionVariable.index()), directionExpression));
            Scope ascending = currentBlock.newScope();
            Scope descending = currentBlock.newScope();
            currentScope.instructions().add(new BranchingInstruction.ConditionalBranchingInstruction(new Value.Variable.Local(directionVariable.index()), ascending, descending));
            currentScope = ascending;
            ascending.instructions().add(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(stepVariable.index()), new Expression.Variable(new Value.Constant(1))));
            ascending.instructions().add(new BranchingInstruction.UnconditionalBranchingInstruction(loopScope));
            currentScope = descending;
            descending.instructions().add(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(stepVariable.index()), new Expression.Variable(new Value.Constant(-1))));
            descending.instructions().add(new BranchingInstruction.UnconditionalBranchingInstruction(loopScope));
        } else {
            stepValue = getValue(statement.getStepExpression());
            currentScope.instructions().add(new BranchingInstruction.UnconditionalBranchingInstruction(loopScope));
        }
        currentScope = loopScope;
        Objects.requireNonNull(statement.getStatementList()).accept(this);
        if (currentScope != null) {
            Expression indexExpression = new Expression.Binary(Operator.BinaryOperator.ADD, new Value.Variable.Local(indexVariable.index()), stepValue);
            currentScope.instructions().add(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(indexVariable.index()), indexExpression));

            Variable conditionVariable = currentBlock.newVariable(null, RapidType.BOOLEAN);
            Expression conditionExpression = new Expression.Binary(Operator.BinaryOperator.EQUAL_TO, new Value.Variable.Local(indexVariable.index()), toValue);
            currentScope.instructions().add(new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(conditionVariable.index()), conditionExpression));
            currentScope.instructions().add(new BranchingInstruction.ConditionalBranchingInstruction(new Value.Variable.Local(conditionVariable.index()), nextScope, loopScope));
        }
        currentScope = nextScope;
        super.visitForStatement(statement);
    }

    @Override
    public void visitWhileStatement(@NotNull RapidWhileStatement statement) {
        RapidExpression condition = Objects.requireNonNull(statement.getCondition());
        Scope conditionScope = currentBlock.newScope();
        Scope nextScope = currentBlock.newScope();
        Scope loopScope = currentBlock.newScope();
        currentScope = conditionScope;
        Value conditionValue = getValue(condition);
        currentScope.instructions().add(new BranchingInstruction.ConditionalBranchingInstruction(conditionValue, loopScope, nextScope));
        currentScope = loopScope;
        Objects.requireNonNull(statement.getStatementList()).accept(this);
        if (currentScope != null) {
            currentScope.instructions().add(new BranchingInstruction.UnconditionalBranchingInstruction(conditionScope));
        }
        currentScope = nextScope;
        super.visitWhileStatement(statement);
    }

    @Override
    public void visitIfStatement(@NotNull RapidIfStatement statement) {
        RapidStatementList thenBranch = Objects.requireNonNull(statement.getThenBranch());
        RapidStatementList elseBranch = statement.getElseBranch();
        Scope thenScope = currentBlock.newScope();
        Scope elseScope = currentBlock.newScope();
        RapidExpression condition = Objects.requireNonNull(statement.getCondition());
        Value value = getValue(condition);
        currentScope.instructions().add(new BranchingInstruction.ConditionalBranchingInstruction(value, thenScope, elseScope));
        currentScope = thenScope;
        thenBranch.accept(this);
        currentScope = elseScope;
        if (elseBranch != null) {
            elseBranch.accept(this);
            currentScope = currentBlock.newScope();
        }
        super.visitIfStatement(statement);
    }

    @Override
    public void visitProcedureCallStatement(@NotNull RapidProcedureCallStatement statement) {
        RapidExpression expression = statement.getReferenceExpression();
        Map<Integer, RapidArgument> argumentMap = new HashMap<>();
        if (expression instanceof RapidReferenceExpression referenceExpression) {
            if (!(referenceExpression.getSymbol() instanceof RapidRoutine routine)) {
                throw new IllegalStateException();
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
        Value value = getValue(expression);
        Scope onSuccess = currentBlock.newScope();
        buildFunctionCall(null, onSuccess, value, argumentMap);
        super.visitProcedureCallStatement(statement);
    }

    @Override
    public void visitConnectStatement(@NotNull RapidConnectStatement statement) {
        if (!(statement.getLeft() instanceof RapidReferenceExpression referenceExpression)) {
            throw new IllegalStateException();
        }
        Value.Variable variable = getVariable(referenceExpression);
        if (!(statement.getRight() instanceof RapidReferenceExpression trapReference)) {
            throw new IllegalStateException();
        }
        if (!(trapReference instanceof PhysicalRoutine routine) || routine.getRoutineType() != RoutineType.TRAP) {
            throw new IllegalStateException();
        }
        String name = getModuleName(routine) + ":" + Objects.requireNonNull(routine.getName());
        currentScope.instructions().add(new LinearInstruction.ConnectInstruction(variable, new Value.Constant(name)));
        super.visitConnectStatement(statement);
    }

    @Override
    public void visitReturnStatement(@NotNull RapidReturnStatement statement) {
        RapidExpression expression = Objects.requireNonNull(statement.getExpression());
        Value value = getValue(expression);
        currentScope.instructions().add(new BranchingInstruction.ReturnInstruction(value));
        currentScope = null;
        super.visitReturnStatement(statement);
    }

    @Override
    public void visitGotoStatement(@NotNull RapidGotoStatement statement) {
        RapidReferenceExpression referenceExpression = Objects.requireNonNull(statement.getReferenceExpression());
        if (!(referenceExpression instanceof RapidLabelStatement labelStatement)) {
            throw new IllegalStateException();
        }
        String name = Objects.requireNonNull(labelStatement.getName());
        Scope next;
        if (currentLabels.containsKey(name)) {
            next = currentLabels.get(name);
        } else {
            next = currentBlock.newScope();
            currentLabels.put(name, next);
        }
        currentScope.instructions().add(new BranchingInstruction.UnconditionalBranchingInstruction(next));
        currentScope = next;
        super.visitGotoStatement(statement);
    }

    @Override
    public void visitLabel(@NotNull RapidLabelStatement statement) {
        String name = Objects.requireNonNull(statement.getName());
        if (currentLabels.containsKey(name)) {
            currentScope.instructions().add(new BranchingInstruction.UnconditionalBranchingInstruction(currentLabels.get(name)));
            currentScope = currentLabels.get(name);
        } else {
            Scope scope = currentBlock.newScope();
            currentLabels.put(name, scope);
            currentScope.instructions().add(new BranchingInstruction.UnconditionalBranchingInstruction(scope));
            currentScope = scope;
        }
        super.visitLabel(statement);
    }

    @Override
    public void visitAssignmentStatement(@NotNull RapidAssignmentStatement statement) {
        if (!(statement.getLeft() instanceof RapidReferenceExpression referenceExpression)) {
            throw new IllegalStateException();
        }
        RapidExpression right = Objects.requireNonNull(statement.getRight());
        Value.Variable variable = getVariable(referenceExpression);
        Value value = getValue(right);
        currentScope.instructions().add(new LinearInstruction.AssignmentInstruction(variable, new Expression.Variable(value)));
        super.visitAssignmentStatement(statement);
    }

    @Override
    public void visitExitStatement(@NotNull RapidExitStatement statement) {
        currentScope.instructions().add(new BranchingInstruction.ExitInstruction());
        currentScope = null;
        super.visitExitStatement(statement);
    }

    @Override
    public void visitRaiseStatement(@NotNull RapidRaiseStatement statement) {
        Value exception = statement.getExpression() != null ? getValue(statement.getExpression()) : null;
        currentScope.instructions().add(new BranchingInstruction.ThrowInstruction(exception));
        currentScope = null;
        super.visitRaiseStatement(statement);
    }

    @Override
    public void visitTryNextStatement(@NotNull RapidTryNextStatement statement) {
        currentScope.instructions().add(new BranchingInstruction.TryNextInstruction());
        currentScope = null;
        super.visitTryNextStatement(statement);
    }

    @Override
    public void visitRetryStatement(@NotNull RapidRetryStatement statement) {
        currentScope.instructions().add(new BranchingInstruction.RetryInstruction());
        currentScope = null;
        super.visitRetryStatement(statement);
    }

    @Override
    public void visitField(@NotNull PhysicalField field) {
        String name = Objects.requireNonNull(field.getName());
        RapidType type = Objects.requireNonNull(field.getType());
        RapidExpression initializer = field.getInitializer();
        if (field.getFieldType() == FieldType.CONSTANT && initializer == null) {
            throw new IllegalArgumentException();
        }
        if (currentBlock != null) {
            if (initializer != null) {
                computeExpressioninVariable(initializer, name, field.getFieldType());
            } else {
                currentBlock.newVariable(field.getName(), null, type, field.getFieldType());
            }
        } else {
            String moduleName = getModuleName(field);
            currentBlock = new Block.FieldBlock(moduleName, name, type, new ArrayList<>(), new HashMap<>(), new HashMap<>(), field.getFieldType());
            if (initializer != null) {
                computeExpressionInVariable(initializer, 0);
            }
            controlFlow.insertBlock(currentBlock);
            currentBlock = null;
        }
        super.visitField(field);
    }

    @Override
    public void visitParameterGroup(@NotNull PhysicalParameterGroup parameterGroup) {
        if (!(currentBlock instanceof Block.FunctionBlock)) {
            throw new IllegalStateException();
        }
        ArgumentGroup argumentGroup = new ArgumentGroup(parameterGroup.isOptional(), new ArrayList<>());
        for (PhysicalParameter parameter : parameterGroup.getParameters()) {
            String name = Objects.requireNonNull(parameter.getName());
            RapidType type = Objects.requireNonNull(parameter.getType());
            Argument argument = currentBlock.newArgument(name, type, parameter.getParameterType());
            argumentGroup.arguments().add(argument);
        }
        List<ArgumentGroup> argumentGroups = Objects.requireNonNull(currentBlock.arguments());
        argumentGroups.add(argumentGroup);
        super.visitParameterGroup(parameterGroup);
    }

    private @NotNull String getModuleName(@NotNull PhysicalVisibleSymbol symbol) {
        PhysicalModule module = PsiTreeUtil.getParentOfType(symbol, PhysicalModule.class);
        Objects.requireNonNull(module);
        return Objects.requireNonNull(module.getName());
    }

    /**
     * Computes the value of the specific expression and stores the result in a new variable.
     *
     * @param expression the expression.
     * @param name the name of the variable.
     * @param fieldType the type of the variable.
     * @return the variable.
     */
    public @NotNull Variable computeExpressioninVariable(@NotNull RapidExpression expression, @NotNull String name, @Nullable FieldType fieldType) {
        variableStack.addLast(new VariableKey.LocalVariable(name, fieldType));
        expression.accept(this);
        Variable variable = lastVariable;
        lastVariable = null;
        return variable;
    }

    /**
     * Computes the value of the specific expression and stores the result in the specific variable.
     *
     * @param expression the expression.
     * @param variableIndex the variable.
     * @return the variable.
     */
    public @NotNull Variable computeExpressionInVariable(@NotNull RapidExpression expression, int variableIndex) {
        variableStack.addLast(new VariableKey.SpecificVariable(variableIndex));
        expression.accept(this);
        Variable variable = lastVariable;
        lastVariable = null;
        return variable;
    }

    /**
     * Computes the value of the specific expression and stores the result in a new variable.
     *
     * @param expression the expression.
     * @return the variable.
     */
    public @NotNull Variable computeExpression(@NotNull RapidExpression expression) {
        variableStack.addLast(new VariableKey.Intermediate());
        expression.accept(this);
        Variable variable = lastVariable;
        lastVariable = null;
        return variable;
    }

    public @NotNull Value getValue(@NotNull RapidExpression expression) {
        if (expression instanceof RapidLiteralExpression literalExpression) {
            Object value = literalExpression.getValue();
            if (value == null) {
                throw new IllegalStateException();
            }
            return new Value.Constant(value);
        }
        if (expression instanceof RapidReferenceExpression referenceExpression) {
            return getVariable(referenceExpression);
        }
        Variable child = computeExpression(expression);
        return new Value.Variable.Local(child.index());
    }

    private @NotNull Value.Variable getVariable(@NotNull RapidReferenceExpression referenceExpression) {
        RapidSymbol symbol = referenceExpression.getSymbol();
        if (symbol == null) {
            throw new IllegalStateException();
        }
        if (symbol instanceof PhysicalField field) {
            PhysicalRoutine routine = PsiTreeUtil.findChildOfType(field, PhysicalRoutine.class);
            if (routine == null) {
                String moduleName = getModuleName(field);
                return new Value.Variable.Field(moduleName, Objects.requireNonNull(field.getName()));
            } else {
                Variable variable = currentBlock.findVariable(Objects.requireNonNull(field.getName()));
                if (variable == null) {
                    throw new IllegalStateException();
                }
                return new Value.Variable.Local(variable.index());
            }
        }
        if (symbol instanceof RapidField field) {
            return new Value.Variable.Field(null, Objects.requireNonNull(field.getName()));
        }
        if (symbol instanceof RapidParameter parameter) {
            Argument argument = currentBlock.findArgument(Objects.requireNonNull(parameter.getName()));
            if (argument == null) {
                throw new IllegalStateException();
            }
            return new Value.Variable.Local(argument.index());
        }
        throw new IllegalStateException();
    }

    @Override
    public void visitAggregateExpression(@NotNull RapidAggregateExpression expression) {
        List<RapidExpression> expressions = expression.getExpressions();
        VariableKey variableKey = variableStack.removeLast();
        RapidType type = Objects.requireNonNull(expression.getType());
        Variable variable = variableKey.create(currentBlock, type, null);
        Expression.Aggregate value = new Expression.Aggregate(expressions.stream().map(this::getValue).toList());
        LinearInstruction instruction = new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(variable.index()), value);
        currentScope.instructions().add(instruction);
        super.visitAggregateExpression(expression);
    }

    @Override
    public void visitFunctionCallExpression(@NotNull RapidFunctionCallExpression expression) {
        RapidReferenceExpression referenceExpression = Objects.requireNonNull(expression.getReferenceExpression());
        RapidSymbol symbol = Objects.requireNonNull(referenceExpression.getSymbol());
        if (!(symbol instanceof RapidRoutine routine)) {
            throw new IllegalStateException();
        }
        String moduleName = symbol instanceof PhysicalVisibleSymbol physicalSymbol ? getModuleName(physicalSymbol) : null;
        String functionName = (moduleName != null ? moduleName + ":" : "") + symbol.getName();
        RapidType type = Objects.requireNonNull(expression.getType());
        Map<Integer, RapidArgument> arguments = getArguments(routine, expression.getArgumentList().getArguments());
        VariableKey variableKey = variableStack.removeLast();
        Variable returnVariable = variableKey.create(currentBlock, type, null);
        Scope onSuccess = currentBlock.newScope();
        buildFunctionCall(new Value.Variable.Local(returnVariable.index()), onSuccess, new Value.Constant(functionName), arguments);
        super.visitFunctionCallExpression(expression);
    }

    /**
     * If the function call has conditional arguments, they are replaced with if statements.
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
    private void buildFunctionCall(@Nullable Value.Variable returnVariable, @NotNull Scope onSuccess, @NotNull Value routine, @NotNull Map<Integer, RapidArgument> presentArguments) {
        List<Integer> integers = presentArguments.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof RapidConditionalArgument)
                .map(Map.Entry::getKey)
                .toList();
        Map<Integer, Value> values = buildArguments(presentArguments);
        if (integers.isEmpty()) {
            currentScope.instructions().add(new BranchingInstruction.CallInstruction(routine, values, returnVariable, onSuccess));
        } else {
            Integer argument = integers.get(0);
            Variable variable = currentBlock.newVariable(null, RapidType.BOOLEAN);
            Scope ifStatement = currentBlock.newScope();
            currentScope.instructions().add(new BranchingInstruction.CallInstruction(new Value.Constant("Present"), Map.of(1, new Value.Variable.Local(argument)), new Value.Variable.Local(variable.index()), ifStatement));
            Scope onPresent = currentBlock.newScope();
            Scope onMissing = currentBlock.newScope();
            ifStatement.instructions().add(new BranchingInstruction.ConditionalBranchingInstruction(new Value.Variable.Local(argument), onPresent, onMissing));
            currentScope = onPresent;
            Map<Integer, RapidArgument> onPresentCopy = new HashMap<>(presentArguments);
            onPresentCopy.put(argument, createArgument((RapidConditionalArgument) presentArguments.get(argument)));
            buildFunctionCall(returnVariable, onSuccess, routine, onPresentCopy);
            currentScope = onMissing;
            Map<Integer, RapidArgument> onFailureCopy = new HashMap<>(presentArguments);
            onFailureCopy.remove(argument);
            buildFunctionCall(returnVariable, onSuccess, routine, onFailureCopy);
        }
        currentScope = onSuccess;
    }

    private @NotNull RapidArgument createArgument(@NotNull RapidConditionalArgument argument) {
        RapidElementFactory elementFactory = RapidElementFactory.getInstance(argument.getProject());
        RapidExpression value = Objects.requireNonNull(argument.getArgument());
        RapidExpression expression = elementFactory.createExpressionFromText("DUMMY(\\" + argument.getParameter().getText() + ":=" + value.getText() + ")");
        return ((RapidFunctionCallExpression) expression).getArgumentList().getArguments().get(0);
    }

    private @NotNull Map<Integer, Value> buildArguments(@NotNull Map<Integer, RapidArgument> arguments) {
        Map<Integer, Value> values = new HashMap<>();
        arguments.forEach((index, argument) -> {
            if (argument instanceof RapidRequiredArgument requiredArgument) {
                values.put(index, getValue(requiredArgument.getArgument()));
            } else if (argument instanceof RapidOptionalArgument) {
                values.put(index, null);
            } else if (argument instanceof RapidConditionalArgument conditionalArgument) {
                RapidExpression expression = conditionalArgument.getArgument();
                if (!(expression instanceof RapidReferenceExpression referenceExpression)) {
                    throw new IllegalStateException();
                }
                RapidSymbol symbol = Objects.requireNonNull(referenceExpression.getSymbol());
                String name = Objects.requireNonNull(symbol.getName());
                Argument variable = Objects.requireNonNull(currentBlock.findArgument(name));
                values.put(index, new Value.Variable.Local(variable.index()));
            }
        });
        return values;
    }

    private @NotNull Map<Integer, RapidArgument> getArguments(@NotNull RapidRoutine routine, @NotNull List<RapidArgument> arguments) {
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

    private @NotNull Map<RapidParameter, RapidArgument> getParameters(@NotNull RapidRoutine routine, @NotNull List<RapidArgument> arguments) {
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
                        .filter(RapidParameterGroup::isOptional)
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
        int originalDimensions = computed.type().getDimensions();
        for (int i = 1; i < array.getDimensions().size(); i++) {
            variableStack.addLast(new VariableKey.Intermediate());
        }
        RapidType type = Objects.requireNonNull(expression.getType());
        List<RapidExpression> dimensions = array.getDimensions();
        for (int i = 0; i < dimensions.size(); i++) {
            RapidExpression dimension = dimensions.get(i);
            Value value = getValue(dimension);
            VariableKey receipt = variableStack.removeLast();
            Expression.Index index = new Expression.Index(new Value.Variable.Local(computed.index()), value);
            computed = receipt.create(currentBlock, type.createArrayType(originalDimensions - (i + 1)), null);
            LinearInstruction instruction = new LinearInstruction.AssignmentInstruction(new Value.Variable.Local(computed.index()), index);
            currentScope.instructions().add(instruction);
        }
        lastVariable = computed;
        super.visitIndexExpression(expression);
    }

    @Override
    public void visitParenthesisedExpression(@NotNull RapidParenthesisedExpression expression) {
        Objects.requireNonNull(expression.getExpression()).accept(this);
        super.visitParenthesisedExpression(expression);
    }

    @Override
    public void visitBinaryExpression(@NotNull RapidBinaryExpression expression) {
        Value left = getValue(expression.getLeft());
        Value right = getValue(Objects.requireNonNull(expression.getRight()));
        VariableKey receipt = variableStack.removeLast();
        Variable variable = receipt.create(currentBlock, Objects.requireNonNull(expression.getType()), null);
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
        currentScope.instructions().add(instruction);
        lastVariable = variable;
        super.visitBinaryExpression(expression);
    }

    @Override
    public void visitReferenceExpression(@NotNull RapidReferenceExpression expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitUnaryExpression(@NotNull RapidUnaryExpression expression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitLiteralExpression(@NotNull RapidLiteralExpression expression) {
        throw new UnsupportedOperationException();
    }

    /**
     * A {@code VariableKey} represents a specific variable.
     */
    private sealed interface VariableKey {

        @NotNull Variable create(@NotNull Block block, @NotNull RapidType type, @Nullable Object value);

        record SpecificVariable(int index) implements VariableKey {

            @Override
            public @NotNull Variable create(@NotNull Block block, @NotNull RapidType type, @Nullable Object value) {
                Variable variable = new Variable(index, value, null, type, null);
                block.variables().put(variable.index(), variable);
                return variable;
            }
        }

        record LocalVariable(@NotNull String name, @Nullable FieldType fieldType) implements VariableKey {
            @Override
            public @NotNull Variable create(@NotNull Block block, @NotNull RapidType type, @Nullable Object value) {
                return block.newVariable(name(), value, type, fieldType);
            }
        }

        record Intermediate() implements VariableKey {
            @Override
            public @NotNull Variable create(@NotNull Block block, @NotNull RapidType type, @Nullable Object value) {
                return block.newVariable(value, type);
            }
        }
    }
}

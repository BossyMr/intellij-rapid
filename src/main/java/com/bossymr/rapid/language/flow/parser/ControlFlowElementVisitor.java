package com.bossymr.rapid.language.flow.parser;

import com.bossymr.rapid.language.flow.Argument;
import com.bossymr.rapid.language.flow.ArgumentGroup;
import com.bossymr.rapid.language.flow.BasicBlock;
import com.bossymr.rapid.language.flow.ControlFlow;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.flow.value.*;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.RapidLabelStatement;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.physical.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ControlFlowElementVisitor extends RapidElementVisitor {

    private final @NotNull ControlFlowBuilder builder;

    public ControlFlowElementVisitor(@NotNull Project project) {
        this.builder = new ControlFlowBuilder(project);
    }

    public static @Nullable String getModuleName(@NotNull PhysicalVisibleSymbol symbol) {
        PhysicalModule module = PsiTreeUtil.getParentOfType(symbol, PhysicalModule.class);
        if (module == null) {
            return null;
        }
        return module.getName();
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
    }

    @Override
    public void visitModule(@NotNull PhysicalModule module) {
        for (PhysicalVisibleSymbol symbol : module.getSymbols()) {
            symbol.accept(this);
        }
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
                ControlFlowExpressionVisitor.computeExpression(builder, field, name, fieldType, initializer);
            } else {
                builder.createVariable(VariableKey.createField(field, name, fieldType), type);
            }
        } else {
            String moduleName = getModuleName(field);
            if (moduleName == null) {
                return;
            }
            builder.enterField(field, moduleName);
            builder.enterBasicBlock(StatementListType.STATEMENT_LIST);
            Value variable;
            if (initializer != null) {
                variable = ControlFlowExpressionVisitor.computeExpression(builder, initializer);
            } else {
                variable = builder.createVariable(VariableKey.createVariable(null), type);
            }
            if (builder.isInsideScope()) {
                builder.exitBasicBlock(new BranchingInstruction.ReturnInstruction(null, variable));
            }
            builder.exitBlock();
        }
    }

    @Override
    public void visitRoutine(@NotNull PhysicalRoutine routine) {
        String moduleName = getModuleName(routine);
        String name = routine.getName();
        RapidType type = routine.getType();
        RoutineType routineType = routine.getRoutineType();
        if (moduleName == null || name == null) {
            return;
        }
        if (type != null) {
            if (routineType != RoutineType.FUNCTION) {
                return;
            }
        } else {
            if (routineType == RoutineType.FUNCTION) {
                return;
            }
        }
        if (routine.getParameterList() != null) {
            if (routineType == RoutineType.TRAP) {
                return;
            }
        } else {
            if (routineType != RoutineType.TRAP) {
                return;
            }
        }
        builder.enterFunction(routine, moduleName);
        List<PhysicalParameterGroup> parameters = routine.getParameters();
        if (parameters != null) {
            for (PhysicalParameterGroup parameterGroup : parameters) {
                parameterGroup.accept(this);
            }
        }
        builder.enterBasicBlock(StatementListType.STATEMENT_LIST);
        for (PhysicalField field : routine.getFields()) {
            field.accept(this);
        }
        routine.getStatementList().accept(this);
        if (builder.isInsideScope()) {
            if (type != null) {
                builder.failScope(null);
            } else {
                builder.exitBasicBlock(new BranchingInstruction.ReturnInstruction(null, null));
            }
        }
        for (RapidStatementList statementList : routine.getStatementLists()) {
            if (statementList.getStatementListType() == StatementListType.STATEMENT_LIST) {
                continue;
            }
            if (statementList.getStatementListType() == StatementListType.ERROR_CLAUSE) {
                List<RapidExpression> expressions = statementList.getExpressions();
                List<Integer> values;
                if (expressions == null) {
                    values = null;
                } else {
                    values = expressions.stream()
                            .filter(expression -> expression instanceof RapidLiteralExpression)
                            .map(expression -> (RapidLiteralExpression) expression)
                            .map(RapidLiteralExpression::getValue)
                            .filter(value -> value instanceof Integer)
                            .map(value -> (Integer) value)
                            .toList();
                }
                builder.enterBasicBlock(values);
            } else {
                builder.enterBasicBlock(statementList.getStatementListType());
            }
            statementList.accept(this);
            if (builder.isInsideScope()) {
                if (type != null) {
                    builder.failScope(null);
                } else {
                    builder.exitBasicBlock(new BranchingInstruction.ReturnInstruction(null, null));
                }
            }
        }
        builder.exitBlock();
    }

    @Override
    public void visitStatementList(@NotNull RapidStatementList statementList) {
        for (RapidStatement element : statementList.getStatements()) {
            element.accept(this);
        }
    }

    @Override
    public void visitTestStatement(@NotNull RapidTestStatement statement) {
        RapidExpression completeExpression = statement.getExpression();
        if (completeExpression == null) {
            builder.failScope(statement);
            return;
        }
        Value completeValue = ControlFlowExpressionVisitor.computeValue(builder, completeExpression);
        BasicBlock nextBasicBlock = null; // The scope which comes after this test statement
        List<RapidTestCaseStatement> testCaseStatements = statement.getTestCaseStatements();
        for (int i = 0; i < testCaseStatements.size(); i++) {
            RapidTestCaseStatement testCaseStatement = testCaseStatements.get(i);
            if (testCaseStatement.isDefault()) {
                BasicBlock defaultBasicBlock = builder.createBasicBlock();
                // A default test case statement should be the last case.
                // If it isn't, it will be called regardless of if any statements below match the expression.
                builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(null, defaultBasicBlock));
                builder.enterBasicBlock(defaultBasicBlock);
                testCaseStatement.getStatements().accept(this);
                if (builder.isInsideScope()) {
                    if (nextBasicBlock == null) {
                        nextBasicBlock = builder.createBasicBlock();
                    }
                    builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(null, nextBasicBlock));
                    builder.enterBasicBlock(nextBasicBlock);
                }
                return;
            }
            List<RapidExpression> expressions = testCaseStatement.getExpressions();
            if (expressions == null) {
                builder.failScope(testCaseStatement);
                continue;
            }
            BasicBlock onSuccess = builder.createBasicBlock();
            BasicBlock onFailure;
            if (testCaseStatements.size() == (i + 1)) {
                if (nextBasicBlock != null) {
                    onFailure = nextBasicBlock;
                } else {
                    onFailure = nextBasicBlock = builder.createBasicBlock();
                }
            } else {
                onFailure = builder.createBasicBlock();
            }
            ReferenceValue conditionVariable = builder.createVariable(VariableKey.createVariable(null), RapidType.BOOLEAN);
            for (int j = 0; j < expressions.size(); j++) {
                RapidExpression expression = expressions.get(j);
                Value value = ControlFlowExpressionVisitor.computeValue(builder, expression);
                Expression equalExpression = new BinaryExpression(BinaryOperator.EQUAL_TO, completeValue, value);
                if (j > 0) {
                    ReferenceValue checkValue = builder.createVariable(VariableKey.createVariable(null), RapidType.BOOLEAN);
                    builder.continueScope(new LinearInstruction.AssignmentInstruction(statement, checkValue, equalExpression));
                    Expression conditionExpression = new BinaryExpression(BinaryOperator.OR, conditionVariable, checkValue);
                    ReferenceValue tempValue = builder.createVariable(VariableKey.createVariable(null), RapidType.BOOLEAN);
                    builder.continueScope(new LinearInstruction.AssignmentInstruction(statement, tempValue, conditionExpression));
                    builder.continueScope(new LinearInstruction.AssignmentInstruction(statement, conditionVariable, new VariableExpression(tempValue)));
                } else {
                    builder.continueScope(new LinearInstruction.AssignmentInstruction(statement, conditionVariable, equalExpression));
                }
            }
            builder.exitBasicBlock(new BranchingInstruction.ConditionalBranchingInstruction(testCaseStatement, conditionVariable, onSuccess, onFailure));
            builder.enterBasicBlock(onSuccess);
            testCaseStatement.getStatements().accept(this);
            if (builder.isInsideScope()) {
                if (nextBasicBlock == null) {
                    nextBasicBlock = builder.createBasicBlock();
                }
                builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(null, nextBasicBlock));
            }
            builder.enterBasicBlock(onFailure);
        }
    }

    @Override
    public void visitForStatement(@NotNull RapidForStatement statement) {
        RapidTargetVariable targetVariable = statement.getVariable();
        RapidExpression fromExpression = statement.getFromExpression();
        RapidExpression toExpression = statement.getToExpression();
        RapidStatementList statementList = statement.getStatementList();
        String variableName;
        if (targetVariable == null || (variableName = targetVariable.getName()) == null || fromExpression == null || toExpression == null || statementList == null) {
            builder.failScope(statement);
            return;
        }
        Value toValue = ControlFlowExpressionVisitor.computeValue(builder, toExpression);
        ReferenceValue indexVariable = ControlFlowExpressionVisitor.computeExpression(builder, targetVariable, variableName, null, fromExpression);
        Value stepValue;
        BasicBlock loopBasicBlock = builder.createBasicBlock();
        BasicBlock nextBasicBlock = builder.createBasicBlock();
        RapidExpression stepExpression = statement.getStepExpression();
        if (stepExpression != null) {
            stepValue = ControlFlowExpressionVisitor.computeValue(builder, stepExpression);
            builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(statement, loopBasicBlock));
        } else {
            // Compute default step value -> +1 if from < to or -1 if from > t
            ReferenceValue stepVariable = builder.createVariable(VariableKey.createVariable(null), RapidType.NUMBER);
            stepValue = stepVariable;
            ReferenceValue directionVariable = builder.createVariable(VariableKey.createVariable(null), RapidType.BOOLEAN); // TRUE = ASCENDING FALSE = DESCENDING
            Expression directionExpression = new BinaryExpression(BinaryOperator.LESS_THAN, indexVariable, toValue);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(statement, directionVariable, directionExpression));
            BasicBlock ascending = builder.createBasicBlock();
            BasicBlock descending = builder.createBasicBlock();
            builder.exitBasicBlock(new BranchingInstruction.ConditionalBranchingInstruction(statement, directionVariable, ascending, descending));
            builder.enterBasicBlock(ascending);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(statement, stepVariable, new VariableExpression(new ConstantValue(RapidType.NUMBER, 1))));
            builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(statement, loopBasicBlock));
            builder.enterBasicBlock(descending);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(statement, stepVariable, new VariableExpression(new ConstantValue(RapidType.NUMBER, -1))));
            builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(statement, loopBasicBlock));
        }
        builder.enterBasicBlock(loopBasicBlock);
        statementList.accept(this);
        if (builder.isInsideScope()) {
            // Add the step to the variable.
            Expression indexExpression = new BinaryExpression(BinaryOperator.ADD, indexVariable, stepValue);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(statement, indexVariable, indexExpression));
            ReferenceValue conditionVariable = builder.createVariable(VariableKey.createVariable(null), RapidType.BOOLEAN);
            // Check if the variable is equal to the result, in which case, do not loop.
            Expression conditionExpression = new BinaryExpression(BinaryOperator.EQUAL_TO, indexVariable, toValue);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(statement, conditionVariable, conditionExpression));
            builder.exitBasicBlock(new BranchingInstruction.ConditionalBranchingInstruction(statement, conditionVariable, nextBasicBlock, loopBasicBlock));
        }
        builder.enterBasicBlock(nextBasicBlock);
    }

    @Override
    public void visitWhileStatement(@NotNull RapidWhileStatement statement) {
        RapidExpression condition = statement.getCondition();
        RapidStatementList statementList = statement.getStatementList();
        if (condition == null || statementList == null) {
            builder.failScope(statement);
            return;
        }
        BasicBlock conditionBasicBlock = builder.createBasicBlock();
        BasicBlock nextBasicBlock = builder.createBasicBlock();
        BasicBlock loopBasicBlock = builder.createBasicBlock();
        builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(statement, conditionBasicBlock));
        builder.enterBasicBlock(conditionBasicBlock);
        ReferenceValue conditionValue = ControlFlowExpressionVisitor.computeExpression(builder, condition);
        builder.exitBasicBlock(new BranchingInstruction.ConditionalBranchingInstruction(statement, conditionValue, loopBasicBlock, nextBasicBlock));
        builder.enterBasicBlock(loopBasicBlock);
        statementList.accept(this);
        if (builder.isInsideScope()) {
            builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(statement, conditionBasicBlock));
        }
        builder.enterBasicBlock(nextBasicBlock);
    }

    @Override
    public void visitIfStatement(@NotNull RapidIfStatement statement) {
        visitIfStatement(statement, new Supplier<>() {
            private BasicBlock value;

            @Override
            public @NotNull BasicBlock get() {
                if (value == null) {
                    value = builder.createBasicBlock();
                }
                return value;
            }
        });
    }

    public void visitIfStatement(@NotNull RapidIfStatement statement, @NotNull Supplier<BasicBlock> nextBasicBlock) {
        RapidStatementList thenBranch = statement.getThenBranch();
        RapidExpression condition = statement.getCondition();
        if (thenBranch == null || condition == null) {
            builder.failScope(statement);
            return;
        }
        RapidStatementList elseBranch = statement.getElseBranch();
        BasicBlock thenBasicBlock = builder.createBasicBlock();
        // If the scope has no else branch, go to the next scope instead.
        // If the scope has an else branch, which doesn't fall through, don't create a fall through block by calling the supplier.
        BasicBlock elseBasicBlock = elseBranch != null ? builder.createBasicBlock() : nextBasicBlock.get();
        ReferenceValue value = ControlFlowExpressionVisitor.computeExpression(builder, condition);
        builder.exitBasicBlock(new BranchingInstruction.ConditionalBranchingInstruction(statement, value, thenBasicBlock, elseBasicBlock));
        builder.enterBasicBlock(thenBasicBlock);
        thenBranch.accept(this);
        if (builder.isInsideScope()) {
            builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(null, nextBasicBlock.get()));
            if (elseBranch == null) {
                builder.enterBasicBlock(nextBasicBlock.get());
            }
        }
        if (elseBranch != null) {
            builder.enterBasicBlock(elseBasicBlock);
            if (elseBranch.getStatements().size() == 1) {
                if (elseBranch.getStatements().get(0) instanceof RapidIfStatement ifStatement) {
                    visitIfStatement(ifStatement, nextBasicBlock);
                    return;
                }
            }
            elseBranch.accept(this);
            if (builder.isInsideScope()) {
                builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(null, nextBasicBlock.get()));
                builder.enterBasicBlock(nextBasicBlock.get());
            }
        }
    }

    @Override
    public void visitProcedureCallStatement(@NotNull RapidProcedureCallStatement statement) {
        RapidExpression expression = statement.getReferenceExpression();
        List<RapidArgument> arguments = statement.getArgumentList().getArguments();
        Value routineValue = ControlFlowExpressionVisitor.computeValue(builder, expression);
        if (!(routineValue instanceof ConstantValue || routineValue instanceof VariableValue)) {
            ReferenceValue variable = builder.createVariable(VariableKey.createVariable(null), routineValue.getType());
            builder.continueScope(new LinearInstruction.AssignmentInstruction(statement, variable, new VariableExpression(routineValue)));
            routineValue = variable;
        }
        BasicBlock nextBlock = builder.createBasicBlock();
        ControlFlowExpressionVisitor.buildFunctionCall(statement, builder, arguments, routineValue, null, nextBlock);
        builder.enterBasicBlock(nextBlock);
    }

    @Override
    public void visitConnectStatement(@NotNull RapidConnectStatement statement) {
        if (!(statement.getLeft() instanceof RapidReferenceExpression referenceExpression)) {
            builder.failScope(statement);
            return;
        }
        ReferenceValue variable = ControlFlowExpressionVisitor.computeVariable(builder, referenceExpression);
        if (variable == null || !(statement.getRight() instanceof RapidReferenceExpression trapReference)) {
            builder.failScope(statement);
            return;
        }
        if (!(trapReference instanceof PhysicalRoutine routine) || routine.getRoutineType() != RoutineType.TRAP || routine.getName() == null) {
            builder.failScope(statement);
            return;
        }
        String routineName = routine.getName();
        String name = getModuleName(routine) + ":" + routineName;
        builder.continueScope(new LinearInstruction.ConnectInstruction(statement, variable, new ConstantValue(RapidType.ANYTYPE, name)));
    }

    @Override
    public void visitReturnStatement(@NotNull RapidReturnStatement statement) {
        RapidExpression expression = statement.getExpression();
        Value value = expression != null ? ControlFlowExpressionVisitor.computeValue(builder, expression) : null;
        builder.exitBasicBlock(new BranchingInstruction.ReturnInstruction(statement, value));
    }

    @Override
    public void visitGotoStatement(@NotNull RapidGotoStatement statement) {
        RapidReferenceExpression referenceExpression = statement.getReferenceExpression();
        if (!(referenceExpression.getSymbol() instanceof RapidLabelStatement labelStatement)) {
            builder.failScope(statement);
            return;
        }
        String name = labelStatement.getName();
        if (name == null) {
            builder.failScope(statement);
            return;
        }
        builder.enterLabel(statement, name);
    }

    @Override
    public void visitLabel(@NotNull RapidLabelStatement statement) {
        String name = statement.getName();
        if (name == null) {
            builder.failScope(statement);
            return;
        }
        builder.enterLabel(statement, name);
    }

    @Override
    public void visitAssignmentStatement(@NotNull RapidAssignmentStatement statement) {
        if (!(statement.getLeft() instanceof RapidReferenceExpression referenceExpression)) {
            builder.failScope(statement);
            return;
        }
        RapidExpression right = statement.getRight();
        ReferenceValue variable = ControlFlowExpressionVisitor.computeVariable(builder, referenceExpression);
        if (right == null || variable == null) {
            builder.failScope(statement);
            return;
        }
        Value value = ControlFlowExpressionVisitor.computeValue(builder, right);
        builder.continueScope(new LinearInstruction.AssignmentInstruction(statement, variable, new VariableExpression(value)));
    }

    @Override
    public void visitExitStatement(@NotNull RapidExitStatement statement) {
        builder.exitBasicBlock(new BranchingInstruction.ExitInstruction(statement));
    }

    @Override
    public void visitRaiseStatement(@NotNull RapidRaiseStatement statement) {
        Value exception;
        RapidExpression expression = statement.getExpression();
        exception = expression != null ? ControlFlowExpressionVisitor.computeValue(builder, expression) : null;
        builder.exitBasicBlock(new BranchingInstruction.ThrowInstruction(statement, exception));
    }

    @Override
    public void visitTryNextStatement(@NotNull RapidTryNextStatement statement) {
        builder.exitBasicBlock(new BranchingInstruction.TryNextInstruction(statement));
    }

    @Override
    public void visitRetryStatement(@NotNull RapidRetryStatement statement) {
        builder.exitBasicBlock(new BranchingInstruction.RetryInstruction(statement));
    }

    @Override
    public void visitParameterGroup(@NotNull PhysicalParameterGroup parameterGroup) {
        ArgumentGroup argumentGroup = new ArgumentGroup(parameterGroup.isOptional(), new ArrayList<>());
        builder.withArgumentGroup(argumentGroup);
        for (PhysicalParameter parameter : parameterGroup.getParameters()) {
            String name = parameter.getName();
            RapidType type = parameter.getType();
            if (name == null || type == null) {
                // Skip the parameter if it isn't valid
                return;
            }
            Argument argument = builder.createArgument(name, type, parameter.getParameterType());
            argumentGroup.arguments().add(argument);
        }
    }
}

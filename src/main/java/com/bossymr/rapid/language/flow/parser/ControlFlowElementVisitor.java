package com.bossymr.rapid.language.flow.parser;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.conditon.Expression;
import com.bossymr.rapid.language.flow.conditon.Operator;
import com.bossymr.rapid.language.flow.conditon.Value;
import com.bossymr.rapid.language.flow.instruction.BranchingInstruction;
import com.bossymr.rapid.language.flow.instruction.LinearInstruction;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.*;
import com.bossymr.rapid.language.symbol.virtual.VirtualRoutine;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ControlFlowElementVisitor extends RapidElementVisitor {

    private final @NotNull ControlFlowBuilder builder = new ControlFlowBuilder();

    public static @NotNull ControlFlow createControlFlow(@NotNull PsiElement element) {
        PsiFile file = element.getContainingFile();
        Module module = ModuleUtil.findModuleForFile(file);
        if (module == null) {
            if (element instanceof PhysicalRoutine || element instanceof VirtualRoutine) {
                ControlFlowElementVisitor analyzer = new ControlFlowElementVisitor();
                element.accept(analyzer);
                return analyzer.getControlFlow();
            } else {
                return new ControlFlow();
            }
        }
        return createControlFlow(module);
    }

    public static @NotNull ControlFlow createControlFlow(@NotNull Module module) {
        ControlFlowElementVisitor analyzer = new ControlFlowElementVisitor();
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
        ControlFlowElementVisitor analyzer = new ControlFlowElementVisitor();
        routine.accept(analyzer);
        return analyzer.getControlFlow();
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
            // Local Variable in a Function
            if (initializer != null) {
                ControlFlowExpressionVisitor.computeExpression(builder, name, fieldType, initializer);
            } else {
                builder.createVariable(VariableKey.createField(name, fieldType), type, null);
            }
        } else {
            CachedValuesManager.getCachedValue(field, () -> {
                String moduleName = getModuleName(field);
                if (moduleName == null) {
                    return CachedValueProvider.Result.createSingleDependency(null, PsiModificationTracker.MODIFICATION_COUNT);
                }
                Block block = builder.enterField(moduleName, name, type, fieldType);
                builder.enterBasicBlock(StatementListType.STATEMENT_LIST);
                Value.Variable variable;
                if (initializer != null) {
                    variable = ControlFlowExpressionVisitor.computeExpression(builder, initializer);
                } else {
                    variable = builder.createVariable(VariableKey.createVariable(), type, null);
                }
                if (builder.isInsideScope()) {
                    builder.exitBasicBlock(new BranchingInstruction.ReturnInstruction(variable));
                }
                builder.exitBlock();
                return CachedValueProvider.Result.createSingleDependency(block, PsiModificationTracker.MODIFICATION_COUNT);
            });
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
            builder.enterBasicBlock(StatementListType.STATEMENT_LIST);
            for (PhysicalField field : routine.getFields()) {
                field.accept(this);
            }
            routine.getStatementList().accept(this);
            if (builder.isInsideScope()) {
                builder.exitBasicBlock(new BranchingInstruction.ReturnInstruction(null));
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
                                .map(expression -> ControlFlowExpressionVisitor.computeValue(builder, expression))
                                .toList();
                    }
                    builder.enterBasicBlock(values);
                } else {
                    builder.enterBasicBlock(statementList.getStatementListType());
                }
                statementList.accept(this);
                if (builder.isInsideScope()) {
                    builder.exitBasicBlock(new BranchingInstruction.ReturnInstruction(null));
                }
            }
            builder.exitBlock();
            return CachedValueProvider.Result.createSingleDependency(block, PsiModificationTracker.MODIFICATION_COUNT);
        });
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
            builder.failScope();
            return;
        }
        Value completeValue = ControlFlowExpressionVisitor.computeValue(builder, completeExpression);
        BasicBlock nextBasicBlock = null; // The scope which comes after this test statement
        List<RapidTestCaseStatement> testCaseStatements = statement.getTestCaseStatements();
        for (int i = 0; i < testCaseStatements.size(); i++) {
            RapidTestCaseStatement testCaseStatement = testCaseStatements.get(i);
            if (testCaseStatement.isDefault()) {
                BasicBlock defaultBasicBlock = builder.createBasicBlock();
                builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(defaultBasicBlock));
                builder.enterBasicBlock(defaultBasicBlock);
                testCaseStatement.getStatements().accept(this);
                if (builder.isInsideScope()) {
                    if (nextBasicBlock == null) {
                        nextBasicBlock = builder.createBasicBlock();
                    }
                    builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(nextBasicBlock));
                    builder.enterBasicBlock(nextBasicBlock);
                }
                return;
            }
            List<RapidExpression> expressions = testCaseStatement.getExpressions();
            if (expressions == null) {
                builder.failScope();
                continue;
            }
            BasicBlock onSuccess = builder.createBasicBlock();
            BasicBlock onFailure = i + 1 == testCaseStatements.size() ? (nextBasicBlock != null ? nextBasicBlock = builder.createBasicBlock() : nextBasicBlock) : builder.createBasicBlock();
            Value.Variable conditionVariable = builder.createVariable(VariableKey.createVariable(), RapidType.BOOLEAN, false);
            for (RapidExpression expression : expressions) {
                Value value = ControlFlowExpressionVisitor.computeValue(builder, expression);
                Value.Variable checkValue = builder.createVariable(VariableKey.createVariable(), RapidType.BOOLEAN, null);
                Expression equalExpression = new Expression.Binary(Operator.BinaryOperator.EQUAL_TO, completeValue, value);
                builder.continueScope(new LinearInstruction.AssignmentInstruction(checkValue, equalExpression));
                Expression conditionExpression = new Expression.Binary(Operator.BinaryOperator.OR, conditionVariable, checkValue);
                builder.continueScope(new LinearInstruction.AssignmentInstruction(conditionVariable, conditionExpression));
            }
            builder.exitBasicBlock(new BranchingInstruction.ConditionalBranchingInstruction(conditionVariable, onSuccess, onFailure));
            builder.enterBasicBlock(onSuccess);
            testCaseStatement.getStatements().accept(this);
            if (builder.isInsideScope()) {
                if (nextBasicBlock == null) {
                    nextBasicBlock = builder.createBasicBlock();
                }
                builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(nextBasicBlock));
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
            builder.failScope();
            return;
        }
        Value toValue = ControlFlowExpressionVisitor.computeValue(builder, toExpression);
        Value.Variable indexVariable = ControlFlowExpressionVisitor.computeExpression(builder, variableName, null, fromExpression);
        Value stepValue;
        BasicBlock loopBasicBlock = builder.createBasicBlock();
        BasicBlock nextBasicBlock = builder.createBasicBlock();
        if (statement.getStepExpression() == null) {
            // Compute default step value -> +1 if from < to or -1 if from > to
            Value.Variable stepVariable = builder.createVariable(VariableKey.createVariable(), RapidType.NUMBER, 0);
            stepValue = stepVariable;
            Value.Variable directionVariable = builder.createVariable(VariableKey.createVariable(), RapidType.BOOLEAN, false); // TRUE = ASCENDING FALSE = DESCENDING
            Expression directionExpression = new Expression.Binary(Operator.BinaryOperator.LESS_THAN, indexVariable, toValue);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(directionVariable, directionExpression));
            BasicBlock ascending = builder.createBasicBlock();
            BasicBlock descending = builder.createBasicBlock();
            builder.exitBasicBlock(new BranchingInstruction.ConditionalBranchingInstruction(directionVariable, ascending, descending));
            builder.enterBasicBlock(ascending);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(stepVariable, new Expression.Variable(new Value.Constant(RapidType.NUMBER, 1))));
            builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(loopBasicBlock));
            builder.enterBasicBlock(descending);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(stepVariable, new Expression.Variable(new Value.Constant(RapidType.NUMBER, -1))));
            builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(loopBasicBlock));
        } else {
            stepValue = ControlFlowExpressionVisitor.computeValue(builder, statement.getStepExpression());
            builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(loopBasicBlock));
        }
        builder.enterBasicBlock(loopBasicBlock);
        statementList.accept(this);
        if (builder.isInsideScope()) {
            Expression indexExpression = new Expression.Binary(Operator.BinaryOperator.ADD, indexVariable, stepValue);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(indexVariable, indexExpression));
            Value.Variable conditionVariable = builder.createVariable(VariableKey.createVariable(), RapidType.BOOLEAN, false);
            Expression conditionExpression = new Expression.Binary(Operator.BinaryOperator.EQUAL_TO, indexVariable, toValue);
            builder.continueScope(new LinearInstruction.AssignmentInstruction(conditionVariable, conditionExpression));
            builder.exitBasicBlock(new BranchingInstruction.ConditionalBranchingInstruction(conditionVariable, nextBasicBlock, loopBasicBlock));
        }
        builder.enterBasicBlock(nextBasicBlock);
    }

    @Override
    public void visitWhileStatement(@NotNull RapidWhileStatement statement) {
        RapidExpression condition = statement.getCondition();
        RapidStatementList statementList = statement.getStatementList();
        if (condition == null || statementList == null) {
            builder.failScope();
            return;
        }
        BasicBlock conditionBasicBlock = builder.createBasicBlock();
        BasicBlock nextBasicBlock = builder.createBasicBlock();
        BasicBlock loopBasicBlock = builder.createBasicBlock();
        builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(conditionBasicBlock));
        builder.enterBasicBlock(conditionBasicBlock);
        Value conditionValue = ControlFlowExpressionVisitor.computeValue(builder, condition);
        builder.exitBasicBlock(new BranchingInstruction.ConditionalBranchingInstruction(conditionValue, loopBasicBlock, nextBasicBlock));
        builder.enterBasicBlock(loopBasicBlock);
        statementList.accept(this);
        if (builder.isInsideScope()) {
            builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(conditionBasicBlock));
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
            builder.failScope();
            return;
        }
        RapidStatementList elseBranch = statement.getElseBranch();
        BasicBlock thenBasicBlock = builder.createBasicBlock();
        // If the scope has no else branch, go to the next scope instead.
        // If the scope has an else branch, which doesn't fall through, don't create a fall through block by calling the supplier.
        BasicBlock elseBasicBlock = elseBranch != null ? builder.createBasicBlock() : nextBasicBlock.get();
        Value value = ControlFlowExpressionVisitor.computeValue(builder, condition);
        builder.exitBasicBlock(new BranchingInstruction.ConditionalBranchingInstruction(value, thenBasicBlock, elseBasicBlock));
        builder.enterBasicBlock(thenBasicBlock);
        thenBranch.accept(this);
        if (builder.isInsideScope()) {
            builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(nextBasicBlock.get()));
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
                builder.exitBasicBlock(new BranchingInstruction.UnconditionalBranchingInstruction(nextBasicBlock.get()));
                builder.enterBasicBlock(nextBasicBlock.get());
            }
        }
    }

    @Override
    public void visitProcedureCallStatement(@NotNull RapidProcedureCallStatement statement) {
        RapidExpression expression = statement.getReferenceExpression();
        RapidRoutine routine = null;
        if (expression instanceof RapidReferenceExpression referenceExpression) {
            if (referenceExpression.getSymbol() instanceof RapidRoutine) {
                routine = ((RapidRoutine) referenceExpression.getSymbol());
            }
        }
        List<RapidArgument> arguments = statement.getArgumentList().getArguments();
        Value routineValue = ControlFlowExpressionVisitor.computeValue(builder, expression);
        BasicBlock nextBlock = builder.createBasicBlock();
        ControlFlowExpressionVisitor.buildFunctionCall(builder, routine, arguments, routineValue, null, nextBlock);
        builder.enterBasicBlock(nextBlock);
    }

    @Override
    public void visitConnectStatement(@NotNull RapidConnectStatement statement) {
        if (!(statement.getLeft() instanceof RapidReferenceExpression referenceExpression)) {
            builder.failScope();
            return;
        }
        Value.Variable variable = ControlFlowExpressionVisitor.computeVariable(builder, referenceExpression);
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
        builder.continueScope(new LinearInstruction.ConnectInstruction(variable, new Value.Constant(RapidType.ANYTYPE, name)));
    }

    @Override
    public void visitReturnStatement(@NotNull RapidReturnStatement statement) {
        RapidExpression expression = statement.getExpression();
        Value value = expression != null ? ControlFlowExpressionVisitor.computeValue(builder, expression) : null;
        builder.exitBasicBlock(new BranchingInstruction.ReturnInstruction(value));
    }

    @Override
    public void visitGotoStatement(@NotNull RapidGotoStatement statement) {
        RapidReferenceExpression referenceExpression = statement.getReferenceExpression();
        if (!(referenceExpression.getSymbol() instanceof RapidLabelStatement labelStatement)) {
            builder.failScope();
            return;
        }
        String name = labelStatement.getName();
        if (name == null) {
            builder.failScope();
            return;
        }
        builder.enterLabel(name);
    }

    @Override
    public void visitLabel(@NotNull RapidLabelStatement statement) {
        String name = statement.getName();
        if (name == null) {
            builder.failScope();
            return;
        }
        builder.enterLabel(name);
    }

    @Override
    public void visitAssignmentStatement(@NotNull RapidAssignmentStatement statement) {
        if (!(statement.getLeft() instanceof RapidReferenceExpression referenceExpression)) {
            builder.failScope();
            return;
        }
        RapidExpression right = statement.getRight();
        Value.Variable variable = ControlFlowExpressionVisitor.computeVariable(builder, referenceExpression);
        if (right == null || variable == null) {
            builder.failScope();
            return;
        }
        Value value = ControlFlowExpressionVisitor.computeValue(builder, right);
        builder.continueScope(new LinearInstruction.AssignmentInstruction(variable, new Expression.Variable(value)));
    }

    @Override
    public void visitExitStatement(@NotNull RapidExitStatement statement) {
        builder.exitBasicBlock(new BranchingInstruction.ExitInstruction());
    }

    @Override
    public void visitRaiseStatement(@NotNull RapidRaiseStatement statement) {
        Value exception;
        RapidExpression expression = statement.getExpression();
        exception = expression != null ? ControlFlowExpressionVisitor.computeValue(builder, expression) : null;
        builder.exitBasicBlock(new BranchingInstruction.ThrowInstruction(exception));
    }

    @Override
    public void visitTryNextStatement(@NotNull RapidTryNextStatement statement) {
        builder.exitBasicBlock(new BranchingInstruction.TryNextInstruction());
    }

    @Override
    public void visitRetryStatement(@NotNull RapidRetryStatement statement) {
        builder.exitBasicBlock(new BranchingInstruction.RetryInstruction());
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
    }
}

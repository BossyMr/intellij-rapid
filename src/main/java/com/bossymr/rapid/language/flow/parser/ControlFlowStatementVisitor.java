package com.bossymr.rapid.language.flow.parser;

import com.bossymr.rapid.language.builder.Label;
import com.bossymr.rapid.language.builder.RapidCodeBlockBuilder;
import com.bossymr.rapid.language.flow.value.BinaryOperator;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class ControlFlowStatementVisitor extends RapidElementVisitor {

    private final @NotNull PhysicalRoutine routine;
    private final @NotNull RapidCodeBlockBuilder builder;

    public ControlFlowStatementVisitor(@NotNull RapidCodeBlockBuilder builder, @NotNull PhysicalRoutine routine) {
        this.builder = builder;
        this.routine = routine;
    }

    @Override
    public void visitAssignmentStatement(@NotNull RapidAssignmentStatement statement) {
        if (statement.getLeft() == null || statement.getRight() == null) {
            builder.error(statement);
            return;
        }
        Expression left = ControlFlowExpressionVisitor.getExpression(statement.getLeft(), builder);
        if (!(left instanceof ReferenceExpression referenceExpression)) {
            builder.error(statement);
            return;
        }
        Expression right = ControlFlowExpressionVisitor.getExpression(statement.getRight(), builder);
        if (!(left.getType().isAssignable(right.getType()))) {
            builder.assign(statement, referenceExpression, builder.error(statement.getRight(), left.getType()));
        } else {
            builder.assign(statement, referenceExpression, right);
        }
    }

    @Override
    public void visitConnectStatement(@NotNull RapidConnectStatement statement) {
        if (statement.getLeft() == null) {
            builder.error(statement);
            return;
        }
        if (!(statement.getRight() instanceof RapidReferenceExpression trapReference)) {
            builder.error(statement);
            return;
        }
        if (!(trapReference instanceof PhysicalRoutine trap) || trap.getRoutineType() != RoutineType.TRAP || trap.getName() == null) {
            builder.error(statement);
            return;
        }
        Expression left = ControlFlowExpressionVisitor.getExpression(statement.getLeft(), builder);
        if (!(left instanceof ReferenceExpression referenceExpression)) {
            builder.error(statement);
            return;
        }
        Expression right = ControlFlowExpressionVisitor.getExpression(statement.getRight(), builder);
        builder.connect(statement, referenceExpression, right);
    }

    @Override
    public void visitProcedureCallStatement(@NotNull RapidProcedureCallStatement statement) {
        Expression expression;
        if(statement.getReferenceExpression() instanceof RapidReferenceExpression referenceExpression) {
            RapidSymbol symbol = referenceExpression.getSymbol();
            if(!(symbol instanceof RapidRoutine target) || target.getName() == null) {
                expression = builder.error(referenceExpression, RapidPrimitiveType.STRING);
            } else {
                expression = builder.literal(target.getName());
            }
        } else {
            expression = ControlFlowExpressionVisitor.getExpression(statement.getReferenceExpression(), builder);
        }
        if (!(expression.getType().isAssignable(RapidPrimitiveType.STRING))) {
            builder.error(statement);
            return;
        }
        builder.invoke(statement, expression, ControlFlowExpressionVisitor.getArgumentBuilder(builder, statement.getArgumentList()));
    }

    @Override
    public void visitGotoStatement(@NotNull RapidGotoStatement statement) {
        RapidReferenceExpression referenceExpression = statement.getReferenceExpression();
        if (referenceExpression == null) {
            builder.error(statement);
            return;
        }
        RapidSymbol symbol = referenceExpression.getSymbol();
        if (!(symbol instanceof RapidLabelStatement labelStatement) || labelStatement.getName() == null) {
            builder.error(statement);
            return;
        }
        Label label = builder.getLabel(labelStatement.getName());
        builder.goTo(label);
    }

    @Override
    public void visitReturnStatement(@NotNull RapidReturnStatement statement) {
        Expression expression;
        if (statement.getExpression() != null) {
            expression = ControlFlowExpressionVisitor.getExpression(statement.getExpression(), builder);
        } else {
            expression = null;
        }
        if (routine.getType() == null && expression != null) {
            expression = null;
        }
        if (routine.getType() != null && expression == null) {
            expression = builder.error(statement, routine.getType());
        }
        builder.returnValue(statement, expression);
    }

    @Override
    public void visitExitStatement(@NotNull RapidExitStatement statement) {
        builder.exit(statement);
    }

    @Override
    public void visitRaiseStatement(@NotNull RapidRaiseStatement statement) {
        Expression expression;
        if (statement.getExpression() != null) {
            expression = ControlFlowExpressionVisitor.getExpression(statement.getExpression(), builder);
        } else {
            expression = null;
        }
        RapidStatementList statementList = PsiTreeUtil.getParentOfType(statement, RapidStatementList.class);
        if (statementList == null) {
            builder.throwException(statement, expression);
            return;
        }
        if (expression != null && statementList.getStatementListType() == BlockType.ERROR_CLAUSE) {
            expression = null;
        }
        if (expression == null && statementList.getStatementListType() != BlockType.ERROR_CLAUSE) {
            expression = builder.error(statement, RapidPrimitiveType.NUMBER);
        }
        builder.throwException(statement, expression);
    }

    @Override
    public void visitRetryStatement(@NotNull RapidRetryStatement statement) {
        builder.retry(statement);
    }

    @Override
    public void visitTryNextStatement(@NotNull RapidTryNextStatement statement) {
        builder.tryNext(statement);
    }

    @Override
    public void visitIfStatement(@NotNull RapidIfStatement statement) {
        RapidExpression condition = statement.getCondition();
        Expression expression;
        if (condition != null) {
            expression = ControlFlowExpressionVisitor.getExpression(condition, builder);
        } else {
            expression = builder.error(statement, RapidPrimitiveType.BOOLEAN);
        }
        builder.ifThenElse(expression,
                codeBuilder -> {
                    if (statement.getThenBranch() != null) {
                        ControlFlowElementBuilder.processExpression(routine, statement.getThenBranch(), builder);
                    }
                }, codeBuilder -> {
                    if (statement.getElseBranch() != null) {
                        ControlFlowElementBuilder.processExpression(routine, statement.getElseBranch(), builder);
                    }
                });
    }

    @Override
    public void visitForStatement(@NotNull RapidForStatement statement) {
        RapidTargetVariable variable = statement.getVariable();
        if (variable == null || variable.getName() == null || statement.getFromExpression() == null || statement.getToExpression() == null) {
            builder.error(statement);
            return;
        }
        ReferenceExpression index = builder.createVariable(variable.getName(), FieldType.VARIABLE, RapidPrimitiveType.NUMBER);
        Expression fromExpression = ControlFlowExpressionVisitor.getExpression(statement.getFromExpression(), builder);
        Expression toExpression = ControlFlowExpressionVisitor.getExpression(statement.getToExpression(), builder);
        builder.assign(index, fromExpression);
        Expression stepExpression;
        if (statement.getStepExpression() != null) {
            stepExpression = ControlFlowExpressionVisitor.getExpression(statement.getStepExpression(), builder);
        } else {
            ReferenceExpression stepVariable = builder.createVariable(RapidPrimitiveType.NUMBER);
            stepExpression = stepVariable;
            builder.ifThenElse(builder.binary(BinaryOperator.LESS_THAN, index, toExpression),
                    codeBuilder -> codeBuilder.assign(stepVariable, codeBuilder.literal(1)),
                    codeBuilder -> codeBuilder.assign(stepVariable, codeBuilder.literal(-1)));
        }
        Label label = builder.createLabel();
        ReferenceExpression breakVariable = builder.createVariable(RapidPrimitiveType.BOOLEAN);
        builder.ifThenElse(builder.binary(BinaryOperator.LESS_THAN, stepExpression, builder.literal(0)),
                codeBuilder -> codeBuilder.assign(breakVariable, codeBuilder.binary(BinaryOperator.GREATER_THAN, index, toExpression)),
                codeBuilder -> codeBuilder.assign(breakVariable, codeBuilder.binary(BinaryOperator.LESS_THAN, index, toExpression)));
        builder.ifThen(breakVariable,
                codeBuilder -> {
                    if (statement.getStatementList() != null) {
                        ControlFlowElementBuilder.processExpression(routine, statement.getStatementList(), codeBuilder);
                    }
                    codeBuilder.assign(index, codeBuilder.binary(BinaryOperator.ADD, index, stepExpression));
                    codeBuilder.goTo(label);
                });
    }

    @Override
    public void visitWhileStatement(@NotNull RapidWhileStatement statement) {
        if (statement.getCondition() == null) {
            builder.error(statement);
            return;
        }
        Label label = builder.createLabel();
        Expression expression = ControlFlowExpressionVisitor.getExpression(statement.getCondition(), builder);
        builder.ifThen(expression,
                codeBuilder -> {
                    if (statement.getStatementList() != null) {
                        ControlFlowElementBuilder.processExpression(routine, statement.getStatementList(), codeBuilder);
                    }
                    codeBuilder.goTo(label);
                });
    }

    @Override
    public void visitTestStatement(@NotNull RapidTestStatement statement) {
        if (statement.getExpression() == null) {
            builder.error(statement);
            return;
        }
        List<RapidTestCaseStatement> statements = statement.getTestCaseStatements();
        if (statements.isEmpty()) {
            return;
        }
        Expression expression = ControlFlowExpressionVisitor.getExpression(statement.getExpression(), builder);
        visitTestCaseStatement(builder, expression, statements, 0);
    }

    private void visitTestCaseStatement(@NotNull RapidCodeBlockBuilder builder, @NotNull Expression expression, @NotNull List<RapidTestCaseStatement> cases, int index) {
        RapidTestCaseStatement statement = cases.get(index);
        if (statement.isDefault()) {
            ControlFlowElementBuilder.processExpression(routine, statement.getStatements(), builder);
        } else {
            List<RapidExpression> expressions = statement.getExpressions();
            if (expressions == null) {
                builder.error(statement);
                visitTestCaseStatement(builder, expression, cases, index + 1);
                return;
            }
            Expression equality = null;
            for (RapidExpression alternative : expressions) {
                Expression part = ControlFlowExpressionVisitor.getExpression(alternative, builder);
                if (equality == null) {
                    equality = builder.binary(BinaryOperator.EQUAL_TO, expression, part);
                } else {
                    equality = builder.binary(BinaryOperator.OR, equality, builder.binary(BinaryOperator.EQUAL_TO, expression, part));
                }
            }
            if (equality == null) {
                ControlFlowElementBuilder.processExpression(routine, statement.getStatements(), builder);
                return;
            }
            builder.ifThenElse(equality,
                    codeBuilder -> ControlFlowElementBuilder.processExpression(routine, statement.getStatements(), codeBuilder),
                    codeBuilder -> visitTestCaseStatement(codeBuilder, expression, cases, index + 1));
        }
    }

    @Override
    public void visitLabel(@NotNull RapidLabelStatement statement) {
        String name = statement.getName();
        if (name != null) {
            builder.createLabel(name);
        }
    }
}

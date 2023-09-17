package com.bossymr.rapid.ide.editor.insight;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.*;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RapidAnnotatorVisitor extends RapidElementVisitor {

    private final RapidValidator validator;

    public RapidAnnotatorVisitor(@NotNull AnnotationHolder annotationHolder) {
        this.validator = new RapidValidator(annotationHolder);
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {
        IElementType elementType = element.getNode().getElementType();
        if (elementType == RapidTokenTypes.IDENTIFIER) {
            validator.checkIdentifier(element);
        }
        super.visitElement(element);
    }

    @Override
    public void visitModule(@NotNull PhysicalModule module) {
        validator.checkModuleFile(module);
        super.visitModule(module);
    }

    @Override
    public void visitAlias(@NotNull PhysicalAlias alias) {
        validator.checkAfter(alias, alias.getTextRange(), PhysicalField.class, PhysicalRoutine.class);
        validator.checkAliasType(alias);
        super.visitAlias(alias);
    }

    @Override
    public void visitRecord(@NotNull PhysicalRecord record) {
        validator.checkAfter(record, SymbolUtil.getDeclaration(record), PhysicalField.class, PhysicalRoutine.class);
        super.visitRecord(record);
    }

    @Override
    public void visitField(@NotNull PhysicalField field) {
        validator.checkAfter(field, field.getTextRange(), PhysicalRoutine.class);
        validator.checkInitializer(field);
        validator.checkDimensions(field);
        validator.checkFieldStatementList(field);
        super.visitField(field);
    }

    @Override
    public void visitSymbol(@NotNull PhysicalSymbol symbol) {
        validator.checkDuplicateSymbol(symbol);
        super.visitSymbol(symbol);
    }

    @Override
    public void visitTypeElement(@NotNull RapidTypeElement typeElement) {
        validator.checkType(typeElement.getReferenceExpression(),
                (symbol) -> RapidBundle.message("annotation.reference.not.type", symbol.getName()),
                (symbol) -> symbol instanceof RapidStructure);
        super.visitTypeElement(typeElement);
    }

    @Override
    public void visitAttributeList(@NotNull RapidAttributeList attributeList) {
        validator.checkAttributeList(attributeList);
        super.visitAttributeList(attributeList);
    }

    @Override
    public void visitParameterList(@NotNull RapidParameterList parameterList) {
        validator.checkParameterDeclaration(parameterList);
        super.visitParameterList(parameterList);
    }

    @Override
    public void visitArray(@NotNull RapidArray array) {
        for (RapidExpression dimension : array.getDimensions()) {
            validator.checkReferenceExpressionType(dimension);
        }
        super.visitArray(array);
    }

    @Override
    public void visitAggregateExpression(@NotNull RapidAggregateExpression expression) {
        validator.checkAggregateExpression(expression);
        for (RapidExpression child : expression.getExpressions()) {
            validator.checkReferenceExpressionType(child);
        }
        super.visitAggregateExpression(expression);
    }

    @Override
    public void visitUnaryExpression(@NotNull RapidUnaryExpression expression) {
        validator.checkUnaryExpression(expression);
        validator.checkReferenceExpressionType(expression.getExpression());
        super.visitUnaryExpression(expression);
    }

    @Override
    public void visitReferenceExpression(@NotNull RapidReferenceExpression expression) {
        validator.checkReferenceExpression(expression);
        super.visitReferenceExpression(expression);
    }

    @Override
    public void visitBinaryExpression(@NotNull RapidBinaryExpression expression) {
        validator.checkBinaryExpression(expression);
        validator.checkReferenceExpressionType(expression.getLeft());
        validator.checkReferenceExpressionType(expression.getRight());
        super.visitBinaryExpression(expression);
    }

    @Override
    public void visitFunctionCallExpression(@NotNull RapidFunctionCallExpression expression) {
        RapidReferenceExpression referenceExpression = expression.getReferenceExpression();
        validator.checkType(referenceExpression,
                symbol -> RapidBundle.message("annotation.reference.not.function", symbol.getName()),
                symbol -> symbol instanceof RapidRoutine routine && routine.getRoutineType() == RoutineType.FUNCTION
        );
        RapidSymbol symbol = referenceExpression.getSymbol();
        if (symbol instanceof RapidRoutine routine) {
            validator.checkRoutineCall(routine, expression.getArgumentList());
        }
        super.visitFunctionCallExpression(expression);
    }

    @Override
    public void visitIndexExpression(@NotNull RapidIndexExpression expression) {
        validator.checkReferenceExpressionType(expression.getExpression());
        validator.checkIndexType(expression);
        super.visitIndexExpression(expression);
    }

    @Override
    public void visitParenthesisedExpression(@NotNull RapidParenthesisedExpression expression) {
        super.visitParenthesisedExpression(expression);
    }

    @Override
    public void visitAssignmentStatement(@NotNull RapidAssignmentStatement statement) {
        validator.checkType(statement.getLeft(),
                RapidBundle.message("annotation.expression.not.variable"),
                (symbol) -> RapidBundle.message("annotation.reference.not.variable", symbol.getName()),
                (symbol) -> symbol instanceof RapidVariable);
        validator.checkStatementType(statement);
        super.visitAssignmentStatement(statement);
    }

    @Override
    public void visitProcedureCallStatement(@NotNull RapidProcedureCallStatement statement) {
        RapidExpression expression = statement.getReferenceExpression();
        if (expression instanceof RapidReferenceExpression referenceExpression) {
            validator.checkType(referenceExpression,
                    symbol -> RapidBundle.message("annotation.reference.not.procedure", symbol.getName()),
                    symbol -> symbol instanceof RapidRoutine routine && routine.getRoutineType() == RoutineType.PROCEDURE
            );
            RapidSymbol symbol = referenceExpression.getSymbol();
            if (symbol instanceof RapidRoutine routine) {
                validator.checkRoutineCall(routine, statement.getArgumentList());
            }
        } else {
            validator.checkCompatibleType(RapidPrimitiveType.STRING, expression);
        }
        super.visitProcedureCallStatement(statement);
    }

    @Override
    public void visitConnectStatement(@NotNull RapidConnectStatement statement) {
        RapidExpression left = statement.getLeft();
        if (left != null) validator.checkConnectLeft(left);
        RapidExpression right = statement.getRight();
        if (right != null) validator.checkConnectRight(right);
        super.visitConnectStatement(statement);
    }

    @Override
    public void visitReturnStatement(@NotNull RapidReturnStatement statement) {
        validator.checkReturnStatement(statement);
        super.visitReturnStatement(statement);
    }

    @Override
    public void visitRaiseStatement(@NotNull RapidRaiseStatement statement) {
        if (statement.getExpression() != null) {
            validator.checkCompatibleType(RapidPrimitiveType.NUMBER, statement.getExpression());
            validator.checkOutsideErrorHandler(statement, RapidBundle.message("annotation.raise.inside.error.clause"));
        } else {
            validator.checkInsideErrorHandler(statement, RapidBundle.message("annotation.raise.outside.error.clause"));
        }
        super.visitRaiseStatement(statement);
    }

    @Override
    public void visitRetryStatement(@NotNull RapidRetryStatement statement) {
        validator.checkInsideErrorHandler(statement, RapidBundle.message("annotation.retry.outside.error.handler"));
        super.visitRetryStatement(statement);
    }

    @Override
    public void visitTryNextStatement(@NotNull RapidTryNextStatement statement) {
        validator.checkInsideErrorHandler(statement, RapidBundle.message("annotation.trynext.outside.error.handler"));
        super.visitTryNextStatement(statement);
    }

    @Override
    public void visitIfStatement(@NotNull RapidIfStatement statement) {
        RapidExpression condition = statement.getCondition();
        if (condition != null) {
            validator.checkCompatibleType(RapidPrimitiveType.BOOLEAN, condition);
        }
        super.visitIfStatement(statement);
    }

    @Override
    public void visitForStatement(@NotNull RapidForStatement statement) {
        validator.checkCompatibleType(RapidPrimitiveType.NUMBER, statement.getFromExpression());
        validator.checkCompatibleType(RapidPrimitiveType.NUMBER, statement.getToExpression());
        validator.checkCompatibleType(RapidPrimitiveType.NUMBER, statement.getStepExpression());
        super.visitForStatement(statement);
    }

    @Override
    public void visitWhileStatement(@NotNull RapidWhileStatement statement) {
        validator.checkCompatibleType(RapidPrimitiveType.BOOLEAN, statement.getCondition());
        super.visitWhileStatement(statement);
    }

    @Override
    public void visitTestStatement(@NotNull RapidTestStatement statement) {
        RapidExpression condition = statement.getExpression();
        if (condition != null) {
            for (RapidTestCaseStatement caseStatement : statement.getTestCaseStatements()) {
                List<RapidExpression> expressions = caseStatement.getExpressions();
                if (expressions != null) {
                    for (RapidExpression expression : expressions) {
                        validator.checkCompatibleType(condition.getType(), expression);
                    }
                }
            }
        }
        super.visitTestStatement(statement);
    }
}
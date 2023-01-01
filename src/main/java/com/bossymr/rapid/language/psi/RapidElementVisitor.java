package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.symbol.RapidLabelStatement;
import com.bossymr.rapid.language.symbol.physical.*;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public abstract class RapidElementVisitor extends PsiElementVisitor {

    public void visitModule(@NotNull PhysicalModule module) {
        visitSymbol(module);
    }

    public void visitAlias(@NotNull PhysicalAlias alias) {
        visitStructure(alias);
    }

    public void visitRecord(@NotNull PhysicalRecord record) {
        visitStructure(record);
    }

    public void visitComponent(@NotNull PhysicalComponent component) {
        visitSymbol(component);
    }

    public void visitStructure(@NotNull PhysicalStructure structure) {
        visitSymbol(structure);
    }

    public void visitField(@NotNull PhysicalField field) {
        visitVariable(field);
    }

    public void visitVariable(@NotNull PhysicalVariable variable) {
        visitSymbol(variable);
    }

    public void visitRoutine(@NotNull PhysicalRoutine routine) {
        visitSymbol(routine);
    }

    public void visitSymbol(@NotNull PhysicalSymbol symbol) {
        visitElement(symbol);
    }

    public void visitTypeElement(@NotNull RapidTypeElement typeElement) {
        visitElement(typeElement);
    }

    public void visitAttributeList(@NotNull RapidAttributeList attributeList) {
        visitElement(attributeList);
    }

    public void visitParameterList(@NotNull RapidParameterList parameterList) {
        visitElement(parameterList);
    }

    public void visitStatementList(@NotNull RapidStatementList statementList) {
        visitElement(statementList);
    }

    public void visitParameterGroup(@NotNull PhysicalParameterGroup parameterGroup) {
        visitElement(parameterGroup);
    }

    public void visitParameter(@NotNull PhysicalParameter parameter) {
        visitVariable(parameter);
    }

    public void visitArray(@NotNull RapidArray array) {
        visitElement(array);
    }

    public void visitArgumentList(@NotNull RapidArgumentList argumentList) {
        visitElement(argumentList);
    }

    public void visitExpressionList(@NotNull RapidExpressionList expressionList) {
        visitElement(expressionList);
    }

    public void visitArgument(@NotNull RapidArgument argument) {
        visitElement(argument);
    }

    public void visitRequiredArgument(@NotNull RapidRequiredArgument argument) {
        visitArgument(argument);
    }

    public void visitOptionalArgument(@NotNull RapidOptionalArgument argument) {
        visitArgument(argument);
    }

    public void visitConditionalArgument(@NotNull RapidConditionalArgument argument) {
        visitArgument(argument);
    }

    public void visitExpression(@NotNull RapidExpression expression) {
        visitElement(expression);
    }

    public void visitAggregateExpression(@NotNull RapidAggregateExpression expression) {
        visitExpression(expression);
    }

    public void visitUnaryExpression(@NotNull RapidUnaryExpression expression) {
        visitExpression(expression);
    }

    public void visitReferenceExpression(@NotNull RapidReferenceExpression expression) {
        visitExpression(expression);
    }

    public void visitBinaryExpression(@NotNull RapidBinaryExpression expression) {
        visitExpression(expression);
    }

    public void visitFunctionCallExpression(@NotNull RapidFunctionCallExpression expression) {
        visitExpression(expression);
    }

    public void visitIndexExpression(@NotNull RapidIndexExpression expression) {
        visitExpression(expression);
    }

    public void visitParenthesisedExpression(@NotNull RapidParenthesisedExpression expression) {
        visitExpression(expression);
    }

    public void visitLiteralExpression(@NotNull RapidLiteralExpression expression) {
        visitExpression(expression);
    }

    public void visitStatement(@NotNull RapidStatement statement) {
        visitElement(statement);
    }

    public void visitTestCaseStatement(@NotNull RapidTestCaseStatement statement) {
        visitStatement(statement);
    }

    public void visitAssignmentStatement(@NotNull RapidAssignmentStatement statement) {
        visitStatement(statement);
    }

    public void visitProcedureCallStatement(@NotNull RapidProcedureCallStatement statement) {
        visitStatement(statement);
    }

    public void visitGotoStatement(@NotNull RapidGotoStatement statement) {
        visitStatement(statement);
    }

    public void visitConnectStatement(@NotNull RapidConnectStatement statement) {
        visitStatement(statement);
    }

    public void visitReturnStatement(@NotNull RapidReturnStatement statement) {
        visitStatement(statement);
    }

    public void visitExitStatement(@NotNull RapidExitStatement statement) {
        visitStatement(statement);
    }

    public void visitRaiseStatement(@NotNull RapidRaiseStatement statement) {
        visitStatement(statement);
    }

    public void visitRetryStatement(@NotNull RapidRetryStatement statement) {
        visitStatement(statement);
    }

    public void visitTryNextStatement(@NotNull RapidTryNextStatement statement) {
        visitStatement(statement);
    }

    public void visitTargetVariable(@NotNull PhysicalTargetVariable variable) {
        visitVariable(variable);
    }

    public void visitIfStatement(@NotNull RapidIfStatement statement) {
        visitStatement(statement);
    }

    public void visitForStatement(@NotNull RapidForStatement statement) {
        visitStatement(statement);
    }

    public void visitWhileStatement(@NotNull RapidWhileStatement statement) {
        visitStatement(statement);
    }

    public void visitTestStatement(@NotNull RapidTestStatement statement) {
        visitStatement(statement);
    }

    public void visitLabel(@NotNull RapidLabelStatement statement) {
        visitStatement(statement);
    }

    public void visitFieldList(RapidFieldList element) {
        visitElement(element);
    }
}

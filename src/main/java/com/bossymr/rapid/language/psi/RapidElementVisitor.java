package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.psi.impl.statement.RapidTryNextStatementImpl;
import com.intellij.psi.PsiElementVisitor;

public abstract class RapidElementVisitor extends PsiElementVisitor {

    public void visitModule(RapidModule module) {
        visitSymbol(module);
    }

    public void visitAlias(RapidAlias alias) {
        visitStructure(alias);
    }

    public void visitRecord(RapidRecord record) {
        visitStructure(record);
    }

    public void visitComponent(RapidComponent component) {
        visitSymbol(component);
    }

    public void visitStructure(RapidStructure structure) {
        visitSymbol(structure);
    }

    public void visitField(RapidField field) {
        visitVariable(field);
    }

    public void visitVariable(RapidVariable variable) {
        visitSymbol(variable);
    }

    public void visitRoutine(RapidRoutine routine) {
        visitSymbol(routine);
    }

    public void visitSymbol(RapidSymbol symbol) {
        visitElement(symbol);
    }

    public void visitTypeElement(RapidTypeElement typeElement) {
        visitElement(typeElement);
    }

    public void visitAttributeList(RapidAttributeList attributeList) {
        visitElement(attributeList);
    }

    public void visitParameterList(RapidParameterList parameterList) {
        visitElement(parameterList);
    }

    public void visitStatementList(RapidStatementList statementList) {
        visitElement(statementList);
    }

    public void visitFieldList(RapidFieldList fieldList) {
        visitElement(fieldList);
    }

    public void visitParameterGroup(RapidParameterGroup parameterGroup) {
        visitElement(parameterGroup);
    }

    public void visitParameter(RapidParameter parameter) {
        visitVariable(parameter);
    }

    public void visitArray(RapidArray array) {
        visitElement(array);
    }

    public void visitArgument(RapidArgument argument) {
        visitElement(argument);
    }

    public void visitRequiredArgument(RapidRequiredArgument argument) {
        visitArgument(argument);
    }

    public void visitOptionalArgument(RapidOptionalArgument argument) {
        visitArgument(argument);
    }

    public void visitConditionalArgument(RapidConditionalArgument argument) {
        visitArgument(argument);
    }

    public void visitExpression(RapidExpression expression) {
        visitElement(expression);
    }

    public void visitAggregateExpression(RapidAggregateExpression expression) {
        visitExpression(expression);
    }

    public void visitUnaryExpression(RapidUnaryExpression expression) {
        visitExpression(expression);
    }

    public void visitReferenceExpression(RapidReferenceExpression expression) {
        visitExpression(expression);
    }

    public void visitBinaryExpression(RapidBinaryExpression expression) {
        visitExpression(expression);
    }

    public void visitFunctionCallExpression(RapidFunctionCallExpression expression) {
        visitExpression(expression);
    }

    public void visitIndexExpression(RapidIndexExpression expression) {
        visitExpression(expression);
    }

    public void visitParenthesisedExpression(RapidParenthesisedExpression expression) {
        visitExpression(expression);
    }

    public void visitLiteralExpression(RapidLiteralExpression expression) {
        visitExpression(expression);
    }

    public void visitStatement(RapidStatement statement) {
        visitElement(statement);
    }

    public void visitTestCaseStatement(RapidTestCaseStatement statement) {
        visitStatement(statement);
    }

    public void visitAssignmentStatement(RapidAssignmentStatement statement) {
        visitStatement(statement);
    }

    public void visitProcedureCallStatement(RapidProcedureCallStatement statement) {
        visitStatement(statement);
    }

    public void visitGotoStatement(RapidGotoStatement statement) {
        visitStatement(statement);
    }

    public void visitConnectStatement(RapidConnectStatement statement) {
        visitStatement(statement);
    }

    public void visitReturnStatement(RapidReturnStatement statement) {
        visitStatement(statement);
    }

    public void visitExitStatement(RapidExitStatement statement) {
        visitStatement(statement);
    }

    public void visitRaiseStatement(RapidRaiseStatement statement) {
        visitStatement(statement);
    }

    public void visitRetryStatement(RapidRetryStatement statement) {
        visitStatement(statement);
    }

    public void visitTryNextStatement(RapidTryNextStatementImpl statement) {
        visitStatement(statement);
    }

    public void visitTargetVariable(RapidTargetVariable variable) {
        visitVariable(variable);
    }

    public void visitIfStatement(RapidIfStatement statement) {
        visitStatement(statement);
    }

    public void visitForStatement(RapidForStatement statement) {
        visitStatement(statement);
    }

    public void visitWhileStatement(RapidWhileStatement statement) {
        visitStatement(statement);
    }

    public void visitTestStatement(RapidTestStatement statement) {
        visitStatement(statement);
    }

    public void visitLabel(RapidLabelStatement statement) {
        visitStatement(statement);
    }
}

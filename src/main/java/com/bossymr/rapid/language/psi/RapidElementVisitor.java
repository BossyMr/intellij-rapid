package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.symbol.*;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public abstract class RapidElementVisitor extends PsiElementVisitor {

    public <T extends RapidModule & RapidElement> void visitModule(@NotNull T module) {
        visitSymbol(module);
    }

    public <T extends RapidAlias & RapidElement> void visitAlias(@NotNull T alias) {
        visitStructure(alias);
    }

    public <T extends RapidRecord & RapidElement> void visitRecord(@NotNull T record) {
        visitStructure(record);
    }

    public <T extends RapidComponent & RapidElement> void visitComponent(@NotNull T component) {
        visitSymbol(component);
    }

    public <T extends RapidStructure & RapidElement> void visitStructure(@NotNull T structure) {
        visitSymbol(structure);
    }

    public <T extends RapidField & RapidElement> void visitField(@NotNull T field) {
        visitVariable(field);
    }

    public <T extends RapidVariable & RapidElement> void visitVariable(@NotNull T variable) {
        visitSymbol(variable);
    }

    public <T extends RapidRoutine & RapidElement> void visitRoutine(@NotNull T routine) {
        visitSymbol(routine);
    }

    public <T extends RapidSymbol & RapidElement> void visitSymbol(@NotNull T symbol) {
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

    public void visitFieldList(@NotNull RapidFieldList fieldList) {
        visitElement(fieldList);
    }

    public <T extends RapidParameterGroup & RapidElement> void visitParameterGroup(@NotNull T parameterGroup) {
        visitElement(parameterGroup);
    }

    public <T extends RapidParameter & RapidElement> void visitParameter(@NotNull T parameter) {
        visitVariable(parameter);
    }

    public void visitArray(@NotNull RapidArray array) {
        visitElement(array);
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

    public <T extends RapidTargetVariable & RapidElement> void visitTargetVariable(@NotNull T variable) {
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
}

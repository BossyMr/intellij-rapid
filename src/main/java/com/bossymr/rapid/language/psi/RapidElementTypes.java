package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.RapidLanguage;
import com.bossymr.rapid.language.psi.impl.*;
import com.bossymr.rapid.language.psi.impl.expression.*;
import com.bossymr.rapid.language.psi.impl.statement.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalLabelStatement;
import com.bossymr.rapid.language.symbol.physical.PhysicalTargetVariable;
import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.tree.ICodeFragmentElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RapidElementTypes {

    IElementType MODULE = RapidStubElementTypes.MODULE;
    IElementType ATTRIBUTE_LIST = RapidStubElementTypes.ATTRIBUTE_LIST;
    IElementType ALIAS = RapidStubElementTypes.ALIAS;
    IElementType RECORD = RapidStubElementTypes.RECORD;
    IElementType COMPONENT = RapidStubElementTypes.COMPONENT;
    IElementType FIELD = RapidStubElementTypes.FIELD;
    IElementType ROUTINE = RapidStubElementTypes.ROUTINE;
    IElementType PARAMETER_LIST = RapidStubElementTypes.PARAMETER_LIST;
    IElementType PARAMETER_GROUP = RapidStubElementTypes.PARAMETER_GROUP;
    IElementType PARAMETER = RapidStubElementTypes.PARAMETER;

    RapidElementType ARRAY = new RapidElementType("ARRAY", RapidArrayImpl::new);
    RapidElementType TYPE_ELEMENT = new RapidElementType("TYPE_ELEMENT", RapidTypeElementImpl::new);
    RapidElementType ARGUMENT_LIST = new RapidElementType("ARGUMENT_LIST", RapidArgumentListImpl::new);
    RapidElementType REQUIRED_ARGUMENT = new RapidElementType("REQUIRED_ARGUMENT", RapidRequiredArgumentImpl::new);
    RapidElementType CONDITIONAL_ARGUMENT = new RapidElementType("CONDITIONAL_ARGUMENT", RapidConditionalArgumentImpl::new);
    RapidElementType OPTIONAL_ARGUMENT = new RapidElementType("OPTIONAL_ARGUMENT", RapidOptionalArgumentImpl::new);

    IElementType EXPRESSION_TEXT = new ICodeFragmentElementType("EXPRESSION_TEXT", RapidLanguage.getInstance()) {
        @Override
        public @Nullable ASTNode parseContents(@NotNull ASTNode chameleon) {
            return super.parseContents(chameleon);
        }
    };

    RapidElementType EXPRESSION_LIST = new RapidElementType("EXPRESSION_LIST", RapidExpressionListImpl::new);
    RapidElementType EMPTY_EXPRESSION = new RapidElementType("EMPTY_EXPRESSION", RapidEmptyExpressionImpl::new);
    RapidElementType AGGREGATE_EXPRESSION = new RapidElementType("AGGREGATE_EXPRESSION", RapidAggregateExpressionImpl::new);
    RapidElementType BINARY_EXPRESSION = new RapidElementType("BINARY_EXPRESSION", RapidBinaryExpressionImpl::new);
    RapidElementType FUNCTION_CALL_EXPRESSION = new RapidElementType("FUNCTION_CALL_EXPRESSION", RapidFunctionCallExpressionImpl::new);
    RapidElementType INDEX_EXPRESSION = new RapidElementType("INDEX_EXPRESSION", RapidIndexExpressionImpl::new);
    RapidElementType PARENTHESISED_EXPRESSION = new RapidElementType("PARENTHESISED_EXPRESSION", RapidParenthesisedExpressionImpl::new);
    RapidElementType REFERENCE_EXPRESSION = new RapidElementType("REFERENCE_EXPRESSION", RapidReferenceExpressionImpl::new);
    RapidElementType UNARY_EXPRESSION = new RapidElementType("UNARY_EXPRESSION", RapidUnaryExpressionImpl::new);
    RapidElementType LITERAL_EXPRESSION = new RapidElementType("LITERAL_EXPRESSION", RapidLiteralExpressionImpl::new);

    RapidElementType STATEMENT_LIST = new RapidElementType("STATEMENT_LIST", RapidStatementListImpl::new);
    RapidElementType TEST_CASE_STATEMENT = new RapidElementType("TEST_CASE_STATEMENT", RapidTestCaseStatementImpl::new);

    RapidElementType ASSIGNMENT_STATEMENT = new RapidElementType("ASSIGNMENT_STATEMENT", RapidAssignmentStatementImpl::new);
    RapidElementType PROCEDURE_CALL_STATEMENT = new RapidElementType("PROCEDURE_CALL_STATEMENT", RapidProcedureCallStatementImpl::new);
    RapidElementType CONNECT_STATEMENT = new RapidElementType("CONNECT_STATEMENT", RapidConnectStatementImpl::new);
    RapidElementType EXIT_STATEMENT = new RapidElementType("EXIT_STATEMENT", RapidExitStatementImpl::new);
    RapidElementType GOTO_STATEMENT = new RapidElementType("GOTO_STATEMENT", RapidGotoStatementImpl::new);
    RapidElementType RAISE_STATEMENT = new RapidElementType("RAISE_STATEMENT", RapidRaiseStatementImpl::new);
    RapidElementType RETRY_STATEMENT = new RapidElementType("RETRY_STATEMENT", RapidRetryStatementImpl::new);
    RapidElementType RETURN_STATEMENT = new RapidElementType("RETURN_STATEMENT", RapidReturnStatementImpl::new);
    RapidElementType TRY_NEXT_STATEMENT = new RapidElementType("TRY_NEXT_STATEMENT", RapidTryNextStatementImpl::new);

    RapidElementType LABEL_STATEMENT = new RapidElementType("LABEL_STATEMENT", PhysicalLabelStatement::new);
    RapidElementType TARGET_VARIABLE = new RapidElementType("TARGET_VARIABLE", PhysicalTargetVariable::new);

    RapidElementType IF_STATEMENT = new RapidElementType("IF_STATEMENT", RapidIfStatementImpl::new);
    RapidElementType FOR_STATEMENT = new RapidElementType("FOR_STATEMENT", RapidForStatementImpl::new);
    RapidElementType WHILE_STATEMENT = new RapidElementType("WHILE_STATEMENT", RapidWhileStatementImpl::new);
    RapidElementType TEST_STATEMENT = new RapidElementType("TEST_STATEMENT", RapidTestStatementImpl::new);

    TokenSet EXPRESSIONS = TokenSet.create(EMPTY_EXPRESSION, AGGREGATE_EXPRESSION, BINARY_EXPRESSION, FUNCTION_CALL_EXPRESSION, INDEX_EXPRESSION, PARENTHESISED_EXPRESSION, REFERENCE_EXPRESSION, UNARY_EXPRESSION, LITERAL_EXPRESSION);
    TokenSet STATEMENTS = TokenSet.create(ASSIGNMENT_STATEMENT, PROCEDURE_CALL_STATEMENT, CONNECT_STATEMENT, EXIT_STATEMENT, LABEL_STATEMENT, GOTO_STATEMENT, RAISE_STATEMENT, RETRY_STATEMENT, RETURN_STATEMENT, TRY_NEXT_STATEMENT, IF_STATEMENT, FOR_STATEMENT, WHILE_STATEMENT, TEST_STATEMENT);
    TokenSet ARGUMENTS = TokenSet.create(REQUIRED_ARGUMENT, OPTIONAL_ARGUMENT, CONDITIONAL_ARGUMENT);
}

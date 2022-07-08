package io.github.bossymr.language.psi;

import com.intellij.psi.tree.IElementType;

/**
 * Contains references to all element and token types in the Rapid language.
 */
public interface RapidElementTypes {
    /*
    TODO:
        1. Complete stub element types
        2. Complete element types
        3. Create visitor & override visitor implementations
        4. Override addInternal and deleteChildInternal (see Java CompositeElement implementations)
     */

    // Stub elements

    // Module
    // Attribute List
    // Alias
    // Record
    // Component List
    // Component
    // Field
    // Routine
    // Parameter List
    // Parameter Group
    // Parameter

    IElementType MODULE = RapidStubElementTypes.MODULE;
    IElementType ATTRIBUTE_LIST = RapidStubElementTypes.ATTRIBUTE_LIST;
    IElementType FIELD = new RapidElementType("FIELD");
    IElementType ALIAS = new RapidElementType("ALIAS");
    IElementType RECORD = new RapidElementType("RECORD");
    IElementType COMPONENT_LIST = new RapidElementType("COMPONENT_LIST");
    IElementType COMPONENT = new RapidElementType("COMPONENT");
    IElementType ROUTINE = new RapidElementType("ROUTINE");
    IElementType PARAMETER_LIST = new RapidElementType("PARAMETER_LIST");
    IElementType PARAMETER_GROUP = new RapidElementType("PARAMETER_GROUP");
    IElementType PARAMETER = new RapidElementType("PARAMETER");
    IElementType ARRAY = new RapidElementType("ARRAY");

    RapidElementType TYPE_ELEMENT = new RapidElementType("TYPE_ELEMENT");
    RapidElementType ARGUMENT_LIST = new RapidElementType("ARGUMENT_LIST");
    RapidElementType REQUIRED_ARGUMENT = new RapidElementType("REQUIRED_ARGUMENT");
    RapidElementType CONDITIONAL_ARGUMENT = new RapidElementType("CONDITIONAL_ARGUMENT");
    RapidElementType OPTIONAL_ARGUMENT = new RapidElementType("OPTIONAL_ARGUMENT");

    RapidElementType EXPRESSION_LIST = new RapidElementType("EXPRESSION_LIST");
    RapidElementType EMPTY_EXPRESSION = new RapidElementType("EMPTY_EXPRESSION");
    RapidElementType AGGREGATE_EXPRESSION = new RapidElementType("AGGREGATE_EXPRESSION");
    RapidElementType BINARY_EXPRESSION = new RapidElementType("BINARY_EXPRESSION");
    RapidElementType FUNCTION_CALL_EXPRESSION = new RapidElementType("FUNCTION_CALL_EXPRESSION");
    RapidElementType INDEX_EXPRESSION = new RapidElementType("INDEX_EXPRESSION");
    RapidElementType PARENTHESISED_EXPRESSION = new RapidElementType("PARENTHESISED_EXPRESSION");
    RapidElementType REFERENCE_EXPRESSION = new RapidElementType("REFERENCE_EXPRESSION");
    RapidElementType UNARY_EXPRESSION = new RapidElementType("UNARY_EXPRESSION");

    RapidElementType BOOL_LITERAL_EXPRESSION = new RapidElementType("BOOL_LITERAL_EXPRESSION");
    RapidElementType INTEGER_LITERAL_EXPRESSION = new RapidElementType("INTEGER_LITERAL_EXPRESSION");
    RapidElementType STRING_LITERAL_EXPRESSION = new RapidElementType("STRING_LITERAL_EXPRESSION");

    RapidElementType FIELD_LIST = new RapidElementType("FIELD_LIST");
    RapidElementType STATEMENT_LIST = new RapidElementType("STATEMENT_LIST");
    RapidElementType BACKWARD_CLAUSE = new RapidElementType("BACKWARD_STATEMENT_LIST");
    RapidElementType UNDO_CLAUSE = new RapidElementType("UNDO_STATEMENT_LIST");
    RapidElementType ERROR_CLAUSE = new RapidElementType("ERROR_STATEMENT_LIST");
    RapidElementType ERROR_LIST = new RapidElementType("ERROR_LIST");
    RapidElementType CASE_SECTION = new RapidElementType("CASE_SECTION");
    RapidElementType DEFAULT_SECTION = new RapidElementType("DEFAULT_SECTION");

    RapidElementType ASSIGNMENT_STATEMENT = new RapidElementType("ASSIGNMENT_STATEMENT");
    RapidElementType PROCEDURE_CALL_STATEMENT = new RapidElementType("PROCEDURE_CALL_STATEMENT");
    RapidElementType CONNECT_STATEMENT = new RapidElementType("CONNECT_STATEMENT");
    RapidElementType EXIT_STATEMENT = new RapidElementType("EXIT_STATEMENT");
    RapidElementType GOTO_STATEMENT = new RapidElementType("GOTO_STATEMENT");
    RapidElementType RAISE_STATEMENT = new RapidElementType("RAISE_STATEMENT");
    RapidElementType RETRY_STATEMENT = new RapidElementType("RETRY_STATEMENT");
    RapidElementType RETURN_STATEMENT = new RapidElementType("RETURN_STATEMENT");
    RapidElementType TRY_NEXT_STATEMENT = new RapidElementType("TRY_NEXT_STATEMENT");

    RapidElementType TARGET_VARIABLE = new RapidElementType("TARGET_VARIABLE");

    RapidElementType IF_STATEMENT = new RapidElementType("IF_STATEMENT");
    RapidElementType FOR_STATEMENT = new RapidElementType("FOR_STATEMENT");
    RapidElementType WHILE_STATEMENT = new RapidElementType("WHILE_STATEMENT");
    RapidElementType TEST_STATEMENT = new RapidElementType("TEST_STATEMENT");
}

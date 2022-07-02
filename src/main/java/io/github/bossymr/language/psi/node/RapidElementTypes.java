package io.github.bossymr.language.psi.node;

import com.intellij.psi.tree.IElementType;
import io.github.bossymr.language.psi.RapidTokenType;
import io.github.bossymr.language.psi.impl.RapidModuleImpl;

/**
 * Contains references to all element and token types in the Rapid language.
 */
public interface RapidElementTypes {

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

    IElementType ATTRIBUTE_LIST = new RapidElementType("ATTRIBUTE_LIST");    // TODO: 2022-07-01 Convert to stubs
    IElementType ALIAS = new RapidElementType("ALIAS");
    IElementType MODULE = new RapidCompositeElementType("MODULE", RapidModuleImpl::new);
    IElementType FIELD = new RapidElementType("FIELD");
    IElementType RECORD = new RapidElementType("RECORD");
    IElementType COMPONENT_LIST = new RapidElementType("COMPONENT_LIST");
    IElementType COMPONENT = new RapidElementType("COMPONENT");
    IElementType PROCEDURE = new RapidElementType("PROCEDURE");
    IElementType FUNCTION = new RapidElementType("FUNCTION");
    IElementType TRAP = new RapidElementType("TRAP");
    IElementType PARAMETER_LIST = new RapidElementType("PARAMETER_LIST");
    IElementType PARAMETER_GROUP = new RapidElementType("PARAMETER_GROUP");
    IElementType PARAMETER = new RapidElementType("PARAMETER");
    IElementType ARRAY = new RapidElementType("ARRAY");
    // Elements
    IElementType TYPE_ELEMENT = new RapidElementType("TYPE_ELEMENT");
    IElementType ARGUMENT_LIST = new RapidElementType("ARGUMENT_LIST");
    IElementType REQUIRED_ARGUMENT = new RapidElementType("REQUIRED_ARGUMENT");
    IElementType CONDITIONAL_ARGUMENT = new RapidElementType("CONDITIONAL_ARGUMENT");
    IElementType OPTIONAL_ARGUMENT = new RapidElementType("OPTIONAL_ARGUMENT");
    IElementType AGGREGATE_EXPRESSION = new RapidElementType("AGGREGATE_EXPRESSION");
    IElementType BINARY_EXPRESSION = new RapidElementType("BINARY_EXPRESSION");
    IElementType FUNCTION_CALL_EXPRESSION = new RapidElementType("FUNCTION_CALL_EXPRESSION");
    IElementType INDEX_EXPRESSION = new RapidElementType("INDEX_EXPRESSION");
    IElementType LITERAL_EXPRESSION = new RapidElementType("LITERAL_EXPRESSION");
    IElementType PARENTHESISED_EXPRESSION = new RapidElementType("PARENTHESISED_EXPRESSION");
    IElementType REFERENCE_EXPRESSION = new RapidElementType("REFERENCE_EXPRESSION");
    IElementType UNARY_EXPRESSION = new RapidElementType("UNARY_EXPRESSION");
    IElementType FIELD_LIST = new RapidElementType("FIELD_LIST");
    IElementType STATEMENT_LIST = new RapidElementType("STATEMENT_LIST");
    IElementType BACKWARD_STATEMENT_LIST = new RapidElementType("BACKWARD_STATEMENT_LIST");
    IElementType UNDO_STATEMENT_LIST = new RapidElementType("UNDO_STATEMENT_LIST");
    IElementType ERROR_STATEMENT_LIST = new RapidElementType("ERROR_STATEMENT_LIST");
    IElementType ERROR_LIST = new RapidElementType("ERROR_LIST");
    IElementType ERROR = new RapidElementType("ERROR");
    IElementType CASE_SECTION = new RapidElementType("CASE_SECTION");
    IElementType DEFAULT_SECTION = new RapidElementType("DEFAULT_SECTION");
    IElementType TEST_EXPRESSION_LIST = new RapidElementType("TEST_EXPRESSION_LIST");
    IElementType TARGET_VARIABLE = new RapidElementType("TARGET_VARIABLE");
    IElementType ASSIGNMENT_STATEMENT = new RapidElementType("ASSIGNMENT_STATEMENT");
    IElementType CONNECT_STATEMENT = new RapidElementType("CONNECT_STATEMENT");
    IElementType EXIT_STATEMENT = new RapidElementType("EXIT_STATEMENT");
    IElementType FOR_STATEMENT = new RapidElementType("FOR_STATEMENT");
    IElementType GOTO_STATEMENT = new RapidElementType("GOTO_STATEMENT");
    IElementType IF_STATEMENT = new RapidElementType("IF_STATEMENT");
    IElementType PROCEDURE_CALL_STATEMENT = new RapidElementType("PROCEDURE_CALL_STATEMENT");
    IElementType RAISE_STATEMENT = new RapidElementType("RAISE_STATEMENT");
    IElementType RETRY_STATEMENT = new RapidElementType("RETRY_STATEMENT");
    IElementType RETURN_STATEMENT = new RapidElementType("RETURN_STATEMENT");
    IElementType TEST_STATEMENT = new RapidElementType("TEST_STATEMENT");
    IElementType TRY_NEXT_STATEMENT = new RapidElementType("TRY_NEXT_STATEMENT");
    IElementType WHILE_STATEMENT = new RapidElementType("WHILE_STATEMENT");
    IElementType ALIAS_KEYWORD = new RapidTokenType("ALIAS");
    // Token elements
    IElementType AND_KEYWORD = new RapidTokenType("AND");
    IElementType BACKWARD_KEYWORD = new RapidTokenType("BACKWARD");
    IElementType CASE_KEYWORD = new RapidTokenType("CASE");
    IElementType CONNECT_KEYWORD = new RapidTokenType("CONNECT");
    IElementType CONST_KEYWORD = new RapidTokenType("CONST");
    IElementType DEFAULT_KEYWORD = new RapidTokenType("DEFAULT");
    IElementType DIV_KEYWORD = new RapidTokenType("DIV");
    IElementType DO_KEYWORD = new RapidTokenType("DO");
    IElementType ELSEIF_KEYWORD = new RapidTokenType("ELSEIF");
    IElementType ELSE_KEYWORD = new RapidTokenType("ELSE");
    IElementType ENDFOR_KEYWORD = new RapidTokenType("ENDFOR");
    IElementType ENDFUNC_KEYWORD = new RapidTokenType("ENDFUNC");
    IElementType ENDIF_KEYWORD = new RapidTokenType("ENDIF");
    IElementType ENDMODULE_KEYWORD = new RapidTokenType("ENDMODULE");
    IElementType ENDPROC_KEYWORD = new RapidTokenType("ENDPROC");
    IElementType ENDRECORD_KEYWORD = new RapidTokenType("ENDRECORD");
    IElementType ENDTEST_KEYWORD = new RapidTokenType("ENDTEST");
    IElementType ENDTRAP_KEYWORD = new RapidTokenType("ENDTRAP");
    IElementType ENDWHILE_KEYWORD = new RapidTokenType("ENDWHILE");
    IElementType ERROR_KEYWORD = new RapidTokenType("ERROR");
    IElementType EXIT_KEYWORD = new RapidTokenType("EXIT");
    IElementType FALSE_KEYWORD = new RapidTokenType("FALSE");
    IElementType FOR_KEYWORD = new RapidTokenType("FOR");
    IElementType FROM_KEYWORD = new RapidTokenType("FROM");
    IElementType FUNC_KEYWORD = new RapidTokenType("FUNC");
    IElementType GOTO_KEYWORD = new RapidTokenType("GOTO");
    IElementType IF_KEYWORD = new RapidTokenType("IF");
    IElementType INOUT_KEYWORD = new RapidTokenType("INOUT");
    IElementType LOCAL_KEYWORD = new RapidTokenType("LOCAL");
    IElementType MODULE_KEYWORD = new RapidTokenType("MODULE");
    IElementType MOD_KEYWORD = new RapidTokenType("MOD");
    IElementType NOSTEPIN_KEYWORD = new RapidTokenType("NOSTEPIN");
    IElementType NOT_KEYWORD = new RapidTokenType("NOT");
    IElementType NOVIEW_KEYWORD = new RapidTokenType("NOVIEW");
    IElementType OR_KEYWORD = new RapidTokenType("OR");
    IElementType PERS_KEYWORD = new RapidTokenType("PERS");
    IElementType PROC_KEYWORD = new RapidTokenType("PROC");
    IElementType RAISE_KEYWORD = new RapidTokenType("RAISE");
    IElementType READONLY_KEYWORD = new RapidTokenType("READONLY");
    IElementType RECORD_KEYWORD = new RapidTokenType("RECORD");
    IElementType RETRY_KEYWORD = new RapidTokenType("RETRY");
    IElementType RETURN_KEYWORD = new RapidTokenType("RETURN");
    IElementType STEP_KEYWORD = new RapidTokenType("STEP");
    IElementType SYSMODULE_KEYWORD = new RapidTokenType("SYSMODULE");
    IElementType TASK_KEYWORD = new RapidTokenType("TASK");
    IElementType TEST_KEYWORD = new RapidTokenType("TEST");
    IElementType THEN_KEYWORD = new RapidTokenType("THEN");
    IElementType TO_KEYWORD = new RapidTokenType("TO");
    IElementType TRAP_KEYWORD = new RapidTokenType("TRAP");
    IElementType TRUE_KEYWORD = new RapidTokenType("TRUE");
    IElementType TRYNEXT_KEYWORD = new RapidTokenType("TRYNEXT");
    IElementType UNDO_KEYWORD = new RapidTokenType("UNDO");
    IElementType VAR_KEYWORD = new RapidTokenType("VAR");
    IElementType VIEWONLY_KEYWORD = new RapidTokenType("VIEWONLY");
    IElementType WHILE_KEYWORD = new RapidTokenType("WHILE");
    IElementType WITH_KEYWORD = new RapidTokenType("WITH");
    IElementType XOR_KEYWORD = new RapidTokenType("XOR");
    IElementType TDN_PLACEHOLDER = new RapidTokenType("<TDN>");
    IElementType DDN_PLACEHOLDER = new RapidTokenType("<DDN>");
    IElementType RDN_PLACEHOLDER = new RapidTokenType("<RDN>");
    IElementType PAR_PLACEHOLDER = new RapidTokenType("<PAR>");
    IElementType ALT_PLACEHOLDER = new RapidTokenType("<ALT>");
    IElementType DIM_PLACEHOLDER = new RapidTokenType("<DIM>");
    IElementType SMT_PLACEHOLDER = new RapidTokenType("<SMT>");
    IElementType VAR_PLACEHOLDER = new RapidTokenType("<VAR>");
    IElementType EIT_PLACEHOLDER = new RapidTokenType("<EIT>");
    IElementType CSE_PLACEHOLDER = new RapidTokenType("<CSE>");
    IElementType EXP_PLACEHOLDER = new RapidTokenType("<EXP>");
    IElementType ARG_PLACEHOLDER = new RapidTokenType("<ARG>");
    IElementType ID_PLACEHOLDER = new RapidTokenType("<ID>");
    IElementType LBRACE = new RapidTokenType("{");
    IElementType RBRACE = new RapidTokenType("}");
    IElementType LPARENTH = new RapidTokenType("(");
    IElementType RPARENTH = new RapidTokenType(")");
    IElementType LBRACKET = new RapidTokenType("[");
    IElementType RBRACKET = new RapidTokenType("]");
    IElementType COMMA = new RapidTokenType(",");
    IElementType DOT = new RapidTokenType(".");
    IElementType SEMICOLON = new RapidTokenType(";");
    IElementType EQ = new RapidTokenType("=");
    IElementType LT = new RapidTokenType("<");
    IElementType LE = new RapidTokenType("<=");
    IElementType LTGT = new RapidTokenType("<>");
    IElementType GE = new RapidTokenType(">=");
    IElementType GT = new RapidTokenType(">");
    IElementType ASTERISK = new RapidTokenType("*");
    IElementType DIV = new RapidTokenType("/");
    IElementType PLUS = new RapidTokenType("+");
    IElementType MINUS = new RapidTokenType("-");
    IElementType CEQ = new RapidTokenType(":=");
    IElementType BACKSLASH = new RapidTokenType("\\");
    IElementType COLON = new RapidTokenType(":");
    IElementType LINE = new RapidTokenType("|");
    IElementType PERCENT = new RapidTokenType("%");
    IElementType QUESTION = new RapidTokenType("?");
    IElementType COMMENT = new RapidTokenType("COMMENT");
    IElementType IDENTIFIER = new RapidTokenType("IDENTIFIER");
    IElementType INTEGER = new RapidTokenType("INTEGER");
    IElementType STRING = new RapidTokenType("STRING");


}

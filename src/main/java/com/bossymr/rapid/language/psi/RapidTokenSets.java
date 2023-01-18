package com.bossymr.rapid.language.psi;

import com.intellij.psi.tree.TokenSet;

import static com.bossymr.rapid.language.psi.RapidElementTypes.*;
import static com.bossymr.rapid.language.psi.RapidTokenTypes.*;

public interface RapidTokenSets {

    TokenSet STRUCTURES = TokenSet.create(ALIAS, RECORD);

    TokenSet ASSIGNMENT_OPERATORS = TokenSet.create(CEQ);
    TokenSet EQUALITY_OPERATORS = TokenSet.create(EQ, LTGT);
    TokenSet RELATIONAL_OPERATORS = TokenSet.create(LT, GT, LE, GE);
    TokenSet ADDITIVE_OPERATORS = TokenSet.create(PLUS, MINUS);

    TokenSet ATTRIBUTES =
            TokenSet.create(SYSMODULE_KEYWORD, NOVIEW_KEYWORD, NOSTEPIN_KEYWORD, VIEWONLY_KEYWORD, READONLY_KEYWORD);

    TokenSet KEYWORDS = TokenSet.create(ALIAS_KEYWORD, AND_KEYWORD, BACKWARD_KEYWORD, CASE_KEYWORD, CONNECT_KEYWORD,
            CONST_KEYWORD, DEFAULT_KEYWORD, DIV_KEYWORD, DO_KEYWORD, ELSEIF_KEYWORD, ELSE_KEYWORD, ENDFOR_KEYWORD,
            ENDFUNC_KEYWORD, ENDIF_KEYWORD, ENDMODULE_KEYWORD, ENDPROC_KEYWORD, ENDRECORD_KEYWORD, ENDTEST_KEYWORD,
            ENDTRAP_KEYWORD, ENDWHILE_KEYWORD, ERROR_KEYWORD, EXIT_KEYWORD, FALSE_KEYWORD, FOR_KEYWORD, FROM_KEYWORD,
            FUNC_KEYWORD, GOTO_KEYWORD, IF_KEYWORD, INOUT_KEYWORD, LOCAL_KEYWORD, MODULE_KEYWORD, MOD_KEYWORD,
            NOSTEPIN_KEYWORD, NOT_KEYWORD, NOVIEW_KEYWORD, OR_KEYWORD, PERS_KEYWORD, PROC_KEYWORD, RAISE_KEYWORD,
            READONLY_KEYWORD, RECORD_KEYWORD, RETRY_KEYWORD, RETURN_KEYWORD, STEP_KEYWORD, SYSMODULE_KEYWORD, TASK_KEYWORD,
            TEST_KEYWORD, THEN_KEYWORD, TO_KEYWORD, TRAP_KEYWORD, TRUE_KEYWORD, TRYNEXT_KEYWORD, UNDO_KEYWORD, VAR_KEYWORD,
            VIEWONLY_KEYWORD, WHILE_KEYWORD, WITH_KEYWORD, XOR_KEYWORD);

    TokenSet OPERATORS = TokenSet.create(NOT_KEYWORD, OR_KEYWORD, XOR_KEYWORD, AND_KEYWORD, EQ, LT, LE, LTGT, GE, GT,
            ASTERISK, DIV, DIV_KEYWORD, MOD_KEYWORD, MINUS, PLUS, CEQ, BACKSLASH, COLON, PERCENT, QUESTION);

    TokenSet VISIBILITY = TokenSet.create(TASK_KEYWORD, LOCAL_KEYWORD);
    TokenSet SYMBOLS = TokenSet.create(ALIAS, RECORD, FIELD, ROUTINE);

    TokenSet EXPRESSIONS = TokenSet.create(EMPTY_EXPRESSION, AGGREGATE_EXPRESSION, BINARY_EXPRESSION,
            FUNCTION_CALL_EXPRESSION, INDEX_EXPRESSION, PARENTHESISED_EXPRESSION, REFERENCE_EXPRESSION,
            UNARY_EXPRESSION, LITERAL_EXPRESSION);
    TokenSet STATEMENTS = TokenSet.create(ASSIGNMENT_STATEMENT, PROCEDURE_CALL_STATEMENT, CONNECT_STATEMENT,
            EXIT_STATEMENT, GOTO_STATEMENT, RAISE_STATEMENT, RETRY_STATEMENT, RETURN_STATEMENT, TRY_NEXT_STATEMENT,
            IF_STATEMENT, FOR_STATEMENT, WHILE_STATEMENT, TEST_STATEMENT);
    TokenSet ARGUMENTS = TokenSet.create(REQUIRED_ARGUMENT, OPTIONAL_ARGUMENT, CONDITIONAL_ARGUMENT);
}

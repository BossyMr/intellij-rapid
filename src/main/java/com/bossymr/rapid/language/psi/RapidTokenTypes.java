package com.bossymr.rapid.language.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

public interface RapidTokenTypes {
    IElementType ALIAS_KEYWORD = new RapidElementType("ALIAS");
    IElementType AND_KEYWORD = new RapidElementType("AND");
    IElementType BACKWARD_KEYWORD = new RapidElementType("BACKWARD");
    IElementType CASE_KEYWORD = new RapidElementType("CASE");
    IElementType CONNECT_KEYWORD = new RapidElementType("CONNECT");
    IElementType CONST_KEYWORD = new RapidElementType("CONST");
    IElementType DEFAULT_KEYWORD = new RapidElementType("DEFAULT");
    IElementType DIV_KEYWORD = new RapidElementType("DIV");
    IElementType DO_KEYWORD = new RapidElementType("DO");
    IElementType ELSEIF_KEYWORD = new RapidElementType("ELSEIF");
    IElementType ELSE_KEYWORD = new RapidElementType("ELSE");
    IElementType ENDFOR_KEYWORD = new RapidElementType("ENDFOR");
    IElementType ENDFUNC_KEYWORD = new RapidElementType("ENDFUNC");
    IElementType ENDIF_KEYWORD = new RapidElementType("ENDIF");
    IElementType ENDMODULE_KEYWORD = new RapidElementType("ENDMODULE");
    IElementType ENDPROC_KEYWORD = new RapidElementType("ENDPROC");
    IElementType ENDRECORD_KEYWORD = new RapidElementType("ENDRECORD");
    IElementType ENDTEST_KEYWORD = new RapidElementType("ENDTEST");
    IElementType ENDTRAP_KEYWORD = new RapidElementType("ENDTRAP");
    IElementType ENDWHILE_KEYWORD = new RapidElementType("ENDWHILE");
    IElementType ERROR_KEYWORD = new RapidElementType("ERROR");
    IElementType EXIT_KEYWORD = new RapidElementType("EXIT");
    IElementType FALSE_KEYWORD = new RapidElementType("FALSE");
    IElementType FOR_KEYWORD = new RapidElementType("FOR");
    IElementType FROM_KEYWORD = new RapidElementType("FROM");
    IElementType FUNC_KEYWORD = new RapidElementType("FUNC");
    IElementType GOTO_KEYWORD = new RapidElementType("GOTO");
    IElementType IF_KEYWORD = new RapidElementType("IF");
    IElementType INOUT_KEYWORD = new RapidElementType("INOUT");
    IElementType LOCAL_KEYWORD = new RapidElementType("LOCAL");
    IElementType MODULE_KEYWORD = new RapidElementType("MODULE");
    IElementType MOD_KEYWORD = new RapidElementType("MOD");
    IElementType NOSTEPIN_KEYWORD = new RapidElementType("NOSTEPIN");
    IElementType NOT_KEYWORD = new RapidElementType("NOT");
    IElementType NOVIEW_KEYWORD = new RapidElementType("NOVIEW");
    IElementType OR_KEYWORD = new RapidElementType("OR");
    IElementType PERS_KEYWORD = new RapidElementType("PERS");
    IElementType PROC_KEYWORD = new RapidElementType("PROC");
    IElementType RAISE_KEYWORD = new RapidElementType("RAISE");
    IElementType READONLY_KEYWORD = new RapidElementType("READONLY");
    IElementType RECORD_KEYWORD = new RapidElementType("RECORD");
    IElementType RETRY_KEYWORD = new RapidElementType("RETRY");
    IElementType RETURN_KEYWORD = new RapidElementType("RETURN");
    IElementType STEP_KEYWORD = new RapidElementType("STEP");
    IElementType SYSMODULE_KEYWORD = new RapidElementType("SYSMODULE");
    IElementType TASK_KEYWORD = new RapidElementType("TASK");
    IElementType TEST_KEYWORD = new RapidElementType("TEST");
    IElementType THEN_KEYWORD = new RapidElementType("THEN");
    IElementType TO_KEYWORD = new RapidElementType("TO");
    IElementType TRAP_KEYWORD = new RapidElementType("TRAP");
    IElementType TRUE_KEYWORD = new RapidElementType("TRUE");
    IElementType TRYNEXT_KEYWORD = new RapidElementType("TRYNEXT");
    IElementType UNDO_KEYWORD = new RapidElementType("UNDO");
    IElementType VAR_KEYWORD = new RapidElementType("VAR");
    IElementType VIEWONLY_KEYWORD = new RapidElementType("VIEWONLY");
    IElementType WHILE_KEYWORD = new RapidElementType("WHILE");
    IElementType WITH_KEYWORD = new RapidElementType("WITH");
    IElementType XOR_KEYWORD = new RapidElementType("XOR");

    IElementType TDN_PLACEHOLDER = new RapidElementType("<TDN>");
    IElementType DDN_PLACEHOLDER = new RapidElementType("<DDN>");
    IElementType RDN_PLACEHOLDER = new RapidElementType("<RDN>");
    IElementType PAR_PLACEHOLDER = new RapidElementType("<PAR>");
    IElementType ALT_PLACEHOLDER = new RapidElementType("<ALT>");
    IElementType DIM_PLACEHOLDER = new RapidElementType("<DIM>");
    IElementType SMT_PLACEHOLDER = new RapidElementType("<SMT>");
    IElementType VAR_PLACEHOLDER = new RapidElementType("<VAR>");
    IElementType EIT_PLACEHOLDER = new RapidElementType("<EIT>");
    IElementType CSE_PLACEHOLDER = new RapidElementType("<CSE>");
    IElementType EXP_PLACEHOLDER = new RapidElementType("<EXP>");
    IElementType ARG_PLACEHOLDER = new RapidElementType("<ARG>");
    IElementType ID_PLACEHOLDER = new RapidElementType("<ID>");

    IElementType LBRACE = new RapidElementType("{");
    IElementType RBRACE = new RapidElementType("}");
    IElementType LPARENTH = new RapidElementType("(");
    IElementType RPARENTH = new RapidElementType(")");
    IElementType LBRACKET = new RapidElementType("[");
    IElementType RBRACKET = new RapidElementType("]");

    IElementType COMMA = new RapidElementType(",");
    IElementType DOT = new RapidElementType(".");
    IElementType SEMICOLON = new RapidElementType(";");

    IElementType EQ = new RapidElementType("=");
    IElementType LT = new RapidElementType("<");
    IElementType LE = new RapidElementType("<=");
    IElementType LTGT = new RapidElementType("<>");
    IElementType GE = new RapidElementType(">=");
    IElementType GT = new RapidElementType(">");

    IElementType ASTERISK = new RapidElementType("*");
    IElementType DIV = new RapidElementType("/");
    IElementType PLUS = new RapidElementType("+");
    IElementType MINUS = new RapidElementType("-");
    IElementType CEQ = new RapidElementType(":=");
    IElementType BACKSLASH = new RapidElementType("\\");
    IElementType COLON = new RapidElementType(":");
    IElementType LINE = new RapidElementType("|");
    IElementType PERCENT = new RapidElementType("%");
    IElementType QUESTION = new RapidElementType("?");

    IElementType COMMENT = new RapidElementType("COMMENT");
    IElementType IDENTIFIER = new RapidElementType("IDENTIFIER");

    IElementType INTEGER_LITERAL = new RapidElementType("INTEGER_LITERAL");
    IElementType STRING_LITERAL = new RapidElementType("STRING_LITERAL");

    TokenSet ATTRIBUTES = TokenSet.create(SYSMODULE_KEYWORD, NOVIEW_KEYWORD, NOSTEPIN_KEYWORD, VIEWONLY_KEYWORD, READONLY_KEYWORD);
    TokenSet KEYWORDS = TokenSet.create(ALIAS_KEYWORD, AND_KEYWORD, BACKWARD_KEYWORD, CASE_KEYWORD, CONNECT_KEYWORD,
            CONST_KEYWORD, DEFAULT_KEYWORD, DIV_KEYWORD, DO_KEYWORD, ELSEIF_KEYWORD, ELSE_KEYWORD, ENDFOR_KEYWORD,
            ENDFUNC_KEYWORD, ENDIF_KEYWORD, ENDMODULE_KEYWORD, ENDPROC_KEYWORD, ENDRECORD_KEYWORD, ENDTEST_KEYWORD,
            ENDTRAP_KEYWORD, ENDWHILE_KEYWORD, ERROR_KEYWORD, EXIT_KEYWORD, FALSE_KEYWORD, FOR_KEYWORD, FROM_KEYWORD,
            FUNC_KEYWORD, GOTO_KEYWORD, IF_KEYWORD, INOUT_KEYWORD, LOCAL_KEYWORD, MODULE_KEYWORD, MOD_KEYWORD,
            NOSTEPIN_KEYWORD, NOT_KEYWORD, NOVIEW_KEYWORD, OR_KEYWORD, PERS_KEYWORD, PROC_KEYWORD, RAISE_KEYWORD,
            READONLY_KEYWORD, RECORD_KEYWORD, RETRY_KEYWORD, RETURN_KEYWORD, STEP_KEYWORD, SYSMODULE_KEYWORD, TASK_KEYWORD,
            TEST_KEYWORD, THEN_KEYWORD, TO_KEYWORD, TRAP_KEYWORD, TRUE_KEYWORD, TRYNEXT_KEYWORD, UNDO_KEYWORD, VAR_KEYWORD,
            VIEWONLY_KEYWORD, WHILE_KEYWORD, WITH_KEYWORD, XOR_KEYWORD);

    TokenSet OPERATIONS = TokenSet.create(NOT_KEYWORD, OR_KEYWORD, XOR_KEYWORD, AND_KEYWORD, EQ, LT, LE, LTGT, GE, GT,
            ASTERISK, DIV, DIV_KEYWORD, MOD_KEYWORD, MINUS, PLUS, CEQ, BACKSLASH, COLON, PERCENT, QUESTION);
}

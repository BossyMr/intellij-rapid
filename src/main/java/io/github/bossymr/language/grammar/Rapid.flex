package io.github.bossymr.language.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import io.github.bossymr.language.psi.RapidTokenTypes;
import com.intellij.lexer.FlexLexer;

%%

%{
    public _RapidLexer() {
      this((java.io.Reader) null);
    }
%}

%class _RapidLexer
%implements FlexLexer
%function advance
%type IElementType

%unicode
%ignorecase

CHARACTER = [^\r\n]

DIGIT = \d
HEX_DIGIT = {DIGIT} | [A-F] | [a-f]
OCTAL_DIGIT = [0-7]
BINARY_DIGIT = [0-1]

INTEGER = {DIGIT} {DIGIT}*
DECIMAL_INTEGER = 0[Dd] {INTEGER}
HEX_INTEGER = 0[Xx] {HEX_DIGIT} {HEX_DIGIT}*
OCTAL_INTEGER = 0[Oo] {OCTAL_DIGIT} {OCTAL_DIGIT}*
BINARY_INTEGER = 0[Bb] {BINARY_DIGIT} {BINARY_DIGIT}*
EXPONENT = [Ee] [+-]? {INTEGER}

INTEGER_LITERAL = ({INTEGER} {EXPONENT}?)
                  | ({DECIMAL_INTEGER} {EXPONENT}?)
                  | {HEX_INTEGER}
                  | {OCTAL_INTEGER}
                  | {BINARY_INTEGER}
                  | ({INTEGER} \. {INTEGER}? {EXPONENT}?)
                  | ({INTEGER}? \. {INTEGER} {EXPONENT}?)

WHITESPACE = (\s)+

IDENTIFIER = [:jletter:] [:jletterdigit:]*

STRING_LITERAL = \"([^\r\n\"]|(\"\"))*\"

COMMENT = \! {CHARACTER}*

%%

<YYINITIAL> {

    {INTEGER_LITERAL}      { return RapidTokenTypes.INTEGER_LITERAL; }

    {WHITESPACE}           { return TokenType.WHITE_SPACE; }

    {STRING_LITERAL}       { return RapidTokenTypes.STRING_LITERAL; }

    {COMMENT}              { return RapidTokenTypes.COMMENT; }

    "ALIAS"                { return RapidTokenTypes.ALIAS_KEYWORD; }
    "AND"                  { return RapidTokenTypes.AND_KEYWORD; }
    "BACKWARD"             { return RapidTokenTypes.BACKWARD_KEYWORD; }
    "CASE"                 { return RapidTokenTypes.CASE_KEYWORD; }
    "CONNECT"              { return RapidTokenTypes.CONNECT_KEYWORD; }
    "CONST"                { return RapidTokenTypes.CONST_KEYWORD; }
    "DEFAULT"              { return RapidTokenTypes.DEFAULT_KEYWORD; }
    "DO"                   { return RapidTokenTypes.DO_KEYWORD; }
    "ELSE"                 { return RapidTokenTypes.ELSE_KEYWORD; }
    "ELSEIF"               { return RapidTokenTypes.ELSEIF_KEYWORD; }
    "ENDFOR"               { return RapidTokenTypes.ENDFOR_KEYWORD; }
    "ENDFUNC"              { return RapidTokenTypes.ENDFUNC_KEYWORD; }
    "ENDIF"                { return RapidTokenTypes.ENDIF_KEYWORD; }
    "ENDMODULE"            { return RapidTokenTypes.ENDMODULE_KEYWORD; }
    "ENDPROC"              { return RapidTokenTypes.ENDPROC_KEYWORD; }
    "ENDRECORD"            { return RapidTokenTypes.ENDRECORD_KEYWORD; }
    "ENDTEST"              { return RapidTokenTypes.ENDTEST_KEYWORD; }
    "ENDTRAP"              { return RapidTokenTypes.ENDTRAP_KEYWORD; }
    "ENDWHILE"             { return RapidTokenTypes.ENDWHILE_KEYWORD; }
    "ERROR"                { return RapidTokenTypes.ERROR_KEYWORD; }
    "EXIT"                 { return RapidTokenTypes.EXIT_KEYWORD; }
    "FALSE"                { return RapidTokenTypes.FALSE_KEYWORD; }
    "FOR"                  { return RapidTokenTypes.FOR_KEYWORD; }
    "FROM"                 { return RapidTokenTypes.FROM_KEYWORD; }
    "FUNC"                 { return RapidTokenTypes.FUNC_KEYWORD; }
    "GOTO"                 { return RapidTokenTypes.GOTO_KEYWORD; }
    "IF"                   { return RapidTokenTypes.IF_KEYWORD; }
    "INOUT"                { return RapidTokenTypes.INOUT_KEYWORD; }
    "LOCAL"                { return RapidTokenTypes.LOCAL_KEYWORD; }
    "MOD"                  { return RapidTokenTypes.MOD_KEYWORD; }
    "MODULE"               { return RapidTokenTypes.MODULE_KEYWORD; }
    "NOSTEPIN"             { return RapidTokenTypes.NOSTEPIN_KEYWORD; }
    "NOT"                  { return RapidTokenTypes.NOT_KEYWORD; }
    "NOVIEW"               { return RapidTokenTypes.NOVIEW_KEYWORD; }
    "OR"                   { return RapidTokenTypes.OR_KEYWORD; }
    "PERS"                 { return RapidTokenTypes.PERS_KEYWORD; }
    "PROC"                 { return RapidTokenTypes.PROC_KEYWORD; }
    "RAISE"                { return RapidTokenTypes.RAISE_KEYWORD; }
    "READONLY"             { return RapidTokenTypes.READONLY_KEYWORD; }
    "RECORD"               { return RapidTokenTypes.RECORD_KEYWORD; }
    "RETRY"                { return RapidTokenTypes.RETRY_KEYWORD; }
    "RETURN"               { return RapidTokenTypes.RETURN_KEYWORD; }
    "STEP"                 { return RapidTokenTypes.STEP_KEYWORD; }
    "SYSMODULE"            { return RapidTokenTypes.SYSMODULE_KEYWORD; }
    "TASK"                 { return RapidTokenTypes.TASK_KEYWORD; }
    "TEST"                 { return RapidTokenTypes.TEST_KEYWORD; }
    "THEN"                 { return RapidTokenTypes.THEN_KEYWORD; }
    "TO"                   { return RapidTokenTypes.TO_KEYWORD; }
    "TRAP"                 { return RapidTokenTypes.TRAP_KEYWORD; }
    "TRUE"                 { return RapidTokenTypes.TRUE_KEYWORD; }
    "TRYNEXT"              { return RapidTokenTypes.TRYNEXT_KEYWORD; }
    "UNDO"                 { return RapidTokenTypes.UNDO_KEYWORD; }
    "VAR"                  { return RapidTokenTypes.VAR_KEYWORD; }
    "VIEWONLY"             { return RapidTokenTypes.VIEWONLY_KEYWORD; }
    "WHILE"                { return RapidTokenTypes.WHILE_KEYWORD; }
    "WITH"                 { return RapidTokenTypes.WITH_KEYWORD; }
    "XOR"                  { return RapidTokenTypes.XOR_KEYWORD; }

    "<TDN>"                { return RapidTokenTypes.TDN_PLACEHOLDER; }
    "<DDN>"                { return RapidTokenTypes.DDN_PLACEHOLDER; }
    "<RDN>"                { return RapidTokenTypes.RDN_PLACEHOLDER; }
    "<PAR>"                { return RapidTokenTypes.PAR_PLACEHOLDER; }
    "<ALT>"                { return RapidTokenTypes.ALT_PLACEHOLDER; }
    "<DIM>"                { return RapidTokenTypes.DIM_PLACEHOLDER; }
    "<SMT>"                { return RapidTokenTypes.SMT_PLACEHOLDER; }
    "<VAR>"                { return RapidTokenTypes.VAR_PLACEHOLDER; }
    "<EIT>"                { return RapidTokenTypes.EIT_PLACEHOLDER; }
    "<CSE>"                { return RapidTokenTypes.CSE_PLACEHOLDER; }
    "<EXP>"                { return RapidTokenTypes.EXP_PLACEHOLDER; }
    "<ARG>"                { return RapidTokenTypes.ARG_PLACEHOLDER; }
    "<ID>"                 { return RapidTokenTypes.ID_PLACEHOLDER; }

    {IDENTIFIER}           { return RapidTokenTypes.IDENTIFIER; }

    "<"                    { return RapidTokenTypes.LT; }
    "<="                   { return RapidTokenTypes.LE; }
    "="                    { return RapidTokenTypes.EQ; }
    ">="                   { return RapidTokenTypes.GE; }
    ">"                    { return RapidTokenTypes.GT; }

    "{"                    { return RapidTokenTypes.LBRACE; }
    "}"                    { return RapidTokenTypes.RBRACE; }
    "("                    { return RapidTokenTypes.LPARENTH; }
    ")"                    { return RapidTokenTypes.RPARENTH; }
    "["                    { return RapidTokenTypes.LBRACKET; }
    "]"                    { return RapidTokenTypes.RBRACKET; }

    ","                    { return RapidTokenTypes.COMMA; }
    ";"                    { return RapidTokenTypes.SEMICOLON; }
    "."                    { return RapidTokenTypes.DOT; }

    "+"                    { return RapidTokenTypes.PLUS; }
    "-"                    { return RapidTokenTypes.MINUS; }
    "*"                    { return RapidTokenTypes.ASTERISK; }
    "/"                    { return RapidTokenTypes.DIV; }
    ":"                    { return RapidTokenTypes.COLON; }
    "\\"                   { return RapidTokenTypes.BACKSLASH; }
    "|"                    { return RapidTokenTypes.LINE; }
    "?"                    { return RapidTokenTypes.QUESTION; }
    "%"                    { return RapidTokenTypes.PERCENT; }

    ":="                   { return RapidTokenTypes.CEQ; }
    "<>"                   { return RapidTokenTypes.LTGT; }
}

[^] { return TokenType.BAD_CHARACTER; }

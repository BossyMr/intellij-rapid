package io.github.bossymr.language.core.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import io.github.bossymr.psi.RapidElementTypes;
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

DIGIT = [0-9]
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

LOWERCASE = [abcdefghijklmnopqrstuvwxyzßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿ]
UPPERCASE = [ABCDEFGHIJKLMNOPQRSTUVWXYZÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞß]

LETTER = {LOWERCASE} | {UPPERCASE}

WHITESPACE = (\s)+

IDENTIFIER = (ANYTYPE#) | ({LETTER} ({LETTER}|{DIGIT}|_){0, 31})

STRING_LITERAL = \"([^\r\n\"]|(\"\"))*\"

COMMENT = \! {CHARACTER}*

%%
<YYINITIAL> {
  {WHITESPACE}           { return TokenType.WHITE_SPACE; }

  "ALIAS"                { return RapidElementTypes.ALIAS_KEYWORD; }
  "AND"                  { return RapidElementTypes.AND_KEYWORD; }
  "BACKWARD"             { return RapidElementTypes.BACKWARD_KEYWORD; }
  "CASE"                 { return RapidElementTypes.CASE_KEYWORD; }
  "CONNECT"              { return RapidElementTypes.CONNECT_KEYWORD; }
  "CONST"                { return RapidElementTypes.CONST_KEYWORD; }
  "DEFAULT"              { return RapidElementTypes.DEFAULT_KEYWORD; }
  "DO"                   { return RapidElementTypes.DO_KEYWORD; }
  "ELSE"                 { return RapidElementTypes.ELSE_KEYWORD; }
  "ELSEIF"               { return RapidElementTypes.ELSEIF_KEYWORD; }
  "ENDFOR"               { return RapidElementTypes.ENDFOR_KEYWORD; }
  "ENDFUNC"              { return RapidElementTypes.ENDFUNC_KEYWORD; }
  "ENDIF"                { return RapidElementTypes.ENDIF_KEYWORD; }
  "ENDMODULE"            { return RapidElementTypes.ENDMODULE_KEYWORD; }
  "ENDPROC"              { return RapidElementTypes.ENDPROC_KEYWORD; }
  "ENDRECORD"            { return RapidElementTypes.ENDRECORD_KEYWORD; }
  "ENDTEST"              { return RapidElementTypes.ENDTEST_KEYWORD; }
  "ENDTRAP"              { return RapidElementTypes.ENDTRAP_KEYWORD; }
  "ENDWHILE"             { return RapidElementTypes.ENDWHILE_KEYWORD; }
  "ERROR"                { return RapidElementTypes.ERROR_KEYWORD; }
  "EXIT"                 { return RapidElementTypes.EXIT_KEYWORD; }
  "FALSE"                { return RapidElementTypes.FALSE_KEYWORD; }
  "FOR"                  { return RapidElementTypes.FOR_KEYWORD; }
  "FROM"                 { return RapidElementTypes.FROM_KEYWORD; }
  "FUNC"                 { return RapidElementTypes.FUNC_KEYWORD; }
  "GOTO"                 { return RapidElementTypes.GOTO_KEYWORD; }
  "IF"                   { return RapidElementTypes.IF_KEYWORD; }
  "INOUT"                { return RapidElementTypes.INOUT_KEYWORD; }
  "LOCAL"                { return RapidElementTypes.LOCAL_KEYWORD; }
  "MOD"                  { return RapidElementTypes.MOD_KEYWORD; }
  "MODULE"               { return RapidElementTypes.MODULE_KEYWORD; }
  "NOSTEPIN"             { return RapidElementTypes.NOSTEPIN_KEYWORD; }
  "NOT"                  { return RapidElementTypes.NOT_KEYWORD; }
  "NOVIEW"               { return RapidElementTypes.NOVIEW_KEYWORD; }
  "OR"                   { return RapidElementTypes.OR_KEYWORD; }
  "PERS"                 { return RapidElementTypes.PERS_KEYWORD; }
  "PROC"                 { return RapidElementTypes.PROC_KEYWORD; }
  "RAISE"                { return RapidElementTypes.RAISE_KEYWORD; }
  "READONLY"             { return RapidElementTypes.READONLY_KEYWORD; }
  "RECORD"               { return RapidElementTypes.RECORD_KEYWORD; }
  "RETRY"                { return RapidElementTypes.RETRY_KEYWORD; }
  "RETURN"               { return RapidElementTypes.RETURN_KEYWORD; }
  "STEP"                 { return RapidElementTypes.STEP_KEYWORD; }
  "SYSMODULE"            { return RapidElementTypes.SYSMODULE_KEYWORD; }
  "TASK"                 { return RapidElementTypes.TASK_KEYWORD; }
  "TEST"                 { return RapidElementTypes.TEST_KEYWORD; }
  "THEN"                 { return RapidElementTypes.THEN_KEYWORD; }
  "TO"                   { return RapidElementTypes.TO_KEYWORD; }
  "TRAP"                 { return RapidElementTypes.TRAP_KEYWORD; }
  "TRUE"                 { return RapidElementTypes.TRUE_KEYWORD; }
  "TRYNEXT"              { return RapidElementTypes.TRYNEXT_KEYWORD; }
  "UNDO"                 { return RapidElementTypes.UNDO_KEYWORD; }
  "VAR"                  { return RapidElementTypes.VAR_KEYWORD; }
  "VIEWONLY"             { return RapidElementTypes.VIEWONLY_KEYWORD; }
  "WHILE"                { return RapidElementTypes.WHILE_KEYWORD; }
  "WITH"                 { return RapidElementTypes.WITH_KEYWORD; }
  "XOR"                  { return RapidElementTypes.XOR_KEYWORD; }

  "<TDN>"                { return RapidElementTypes.TDN_PLACEHOLDER; }
  "<DDN>"                { return RapidElementTypes.DDN_PLACEHOLDER; }
  "<RDN>"                { return RapidElementTypes.RDN_PLACEHOLDER; }
  "<PAR>"                { return RapidElementTypes.PAR_PLACEHOLDER; }
  "<ALT>"                { return RapidElementTypes.ALT_PLACEHOLDER; }
  "<DIM>"                { return RapidElementTypes.DIM_PLACEHOLDER; }
  "<SMT>"                { return RapidElementTypes.SMT_PLACEHOLDER; }
  "<VAR>"                { return RapidElementTypes.VAR_PLACEHOLDER; }
  "<EIT>"                { return RapidElementTypes.EIT_PLACEHOLDER; }
  "<CSE>"                { return RapidElementTypes.CSE_PLACEHOLDER; }
  "<EXP>"                { return RapidElementTypes.EXP_PLACEHOLDER; }
  "<ARG>"                { return RapidElementTypes.ARG_PLACEHOLDER; }
  "<ID>"                 { return RapidElementTypes.ID_PLACEHOLDER; }

  "{"                    { return RapidElementTypes.LBRACE; }
  "}"                    { return RapidElementTypes.RBRACE; }
  "("                    { return RapidElementTypes.LPARENTH; }
  ")"                    { return RapidElementTypes.RPARENTH; }
  "["                    { return RapidElementTypes.LBRACKET; }
  "]"                    { return RapidElementTypes.RBRACKET; }
  ","                    { return RapidElementTypes.COMMA; }
  "."                    { return RapidElementTypes.DOT; }
  "="                    { return RapidElementTypes.EQ; }
  "<"                    { return RapidElementTypes.LT; }
  ">"                    { return RapidElementTypes.GT; }
  "+"                    { return RapidElementTypes.PLUS; }
  "-"                    { return RapidElementTypes.MINUS; }
  "*"                    { return RapidElementTypes.ASTERISK; }
  ":"                    { return RapidElementTypes.COLON; }
  ";"                    { return RapidElementTypes.SEMICOLON; }
  "!"                    { return RapidElementTypes.EXLAMATION; }
  "\\"                   { return RapidElementTypes.BACKSLASH; }
  "/"                    { return RapidElementTypes.DIV; }
  "|"                    { return RapidElementTypes.LINE; }
  "?"                    { return RapidElementTypes.QUESTION; }
  "%"                    { return RapidElementTypes.PERCENT; }
  ":="                   { return RapidElementTypes.CEQ; }
  "<>"                   { return RapidElementTypes.LTGT; }
  ">="                   { return RapidElementTypes.GE; }
  "<="                   { return RapidElementTypes.LE; }

  {IDENTIFIER}           { return RapidElementTypes.IDENTIFIER; }
  {INTEGER_LITERAL}      { return RapidElementTypes.INTEGER_LITERAL; }
  {STRING_LITERAL}       { return RapidElementTypes.STRING_LITERAL; }
  {COMMENT}              { return RapidElementTypes.COMMENT; }

}

[^] { return TokenType.BAD_CHARACTER; }

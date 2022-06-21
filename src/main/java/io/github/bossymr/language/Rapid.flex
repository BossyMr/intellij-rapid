package io.github.bossymr.language;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import io.github.bossymr.psi.RapidElementTypes;
import com.intellij.lexer.FlexLexer;

%%

%class RapidLexer
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

  "ALIAS"                { return RapidElementTypes.ALIAS; }
  "AND"                  { return RapidElementTypes.AND; }
  "BACKWARD"             { return RapidElementTypes.BACKWARD; }
  "CASE"                 { return RapidElementTypes.CASE; }
  "CONNECT"              { return RapidElementTypes.CONNECT; }
  "CONST"                { return RapidElementTypes.CONST; }
  "DEFAULT"              { return RapidElementTypes.DEFAULT; }
  "DO"                   { return RapidElementTypes.DO; }
  "ELSE"                 { return RapidElementTypes.ELSE; }
  "ELSEIF"               { return RapidElementTypes.ELSEIF; }
  "ENDFOR"               { return RapidElementTypes.ENDFOR; }
  "ENDFUNC"              { return RapidElementTypes.ENDFUNC; }
  "ENDIF"                { return RapidElementTypes.ENDIF; }
  "ENDMODULE"            { return RapidElementTypes.ENDMODULE; }
  "ENDPROC"              { return RapidElementTypes.ENDPROC; }
  "ENDRECORD"            { return RapidElementTypes.ENDRECORD; }
  "ENDTEST"              { return RapidElementTypes.ENDTEST; }
  "ENDTRAP"              { return RapidElementTypes.ENDTRAP; }
  "ENDWHILE"             { return RapidElementTypes.ENDWHILE; }
  "ERROR"                { return RapidElementTypes.ERROR; }
  "EXIT"                 { return RapidElementTypes.EXIT; }
  "FALSE"                { return RapidElementTypes.FALSE; }
  "FOR"                  { return RapidElementTypes.FOR; }
  "FROM"                 { return RapidElementTypes.FROM; }
  "FUNC"                 { return RapidElementTypes.FUNC; }
  "GOTO"                 { return RapidElementTypes.GOTO; }
  "IF"                   { return RapidElementTypes.IF; }
  "INOUT"                { return RapidElementTypes.INOUT; }
  "LOCAL"                { return RapidElementTypes.LOCAL; }
  "MOD"                  { return RapidElementTypes.MOD; }
  "MODULE"               { return RapidElementTypes.MODULE; }
  "NOSTEPIN"             { return RapidElementTypes.NOSTEPIN; }
  "NOT"                  { return RapidElementTypes.NOT; }
  "NOVIEW"               { return RapidElementTypes.NOVIEW; }
  "OR"                   { return RapidElementTypes.OR; }
  "PERS"                 { return RapidElementTypes.PERS; }
  "PROC"                 { return RapidElementTypes.PROC; }
  "RAISE"                { return RapidElementTypes.RAISE; }
  "READONLY"             { return RapidElementTypes.READONLY; }
  "RECORD"               { return RapidElementTypes.RECORD; }
  "RETRY"                { return RapidElementTypes.RETRY; }
  "RETURN"               { return RapidElementTypes.RETURN; }
  "STEP"                 { return RapidElementTypes.STEP; }
  "SYSMODULE"            { return RapidElementTypes.SYSMODULE; }
  "TASK"                 { return RapidElementTypes.TASK; }
  "TEST"                 { return RapidElementTypes.TEST; }
  "THEN"                 { return RapidElementTypes.THEN; }
  "TO"                   { return RapidElementTypes.TO; }
  "TRAP"                 { return RapidElementTypes.TRAP; }
  "TRUE"                 { return RapidElementTypes.TRUE; }
  "TRYNEXT"              { return RapidElementTypes.TRYNEXT; }
  "UNDO"                 { return RapidElementTypes.UNDO; }
  "VAR"                  { return RapidElementTypes.VAR; }
  "VIEWONLY"             { return RapidElementTypes.VIEWONLY; }
  "WHILE"                { return RapidElementTypes.WHILE; }
  "WITH"                 { return RapidElementTypes.WITH; }
  "XOR"                  { return RapidElementTypes.XOR; }

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

package io.github.bossymr.language.core.psi;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import io.github.bossymr.language.RapidLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import static io.github.bossymr.language.core.psi.RapidElementTypes.*;

public class RapidTokenType extends IElementType {

    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
    public static final TokenSet STRING_LITERALS = TokenSet.create(STRING_LITERAL);
    public static final TokenSet COMMENTS = TokenSet.create(COMMENT);

    public static final TokenSet KEYWORDS = TokenSet.create(ALIAS_KEYWORD, AND_KEYWORD, BACKWARD_KEYWORD, CASE_KEYWORD,
            CONNECT_KEYWORD, CONST_KEYWORD, DEFAULT_KEYWORD, DIV_KEYWORD, DO_KEYWORD, ELSE_KEYWORD, ELSEIF_KEYWORD,
            ENDFOR_KEYWORD, ENDFUNC_KEYWORD, ENDIF_KEYWORD, ENDMODULE_KEYWORD, ENDPROC_KEYWORD, ENDRECORD_KEYWORD,
            ENDTEST_KEYWORD, ENDTRAP_KEYWORD, ENDWHILE_KEYWORD, ERROR_KEYWORD, EXIT_KEYWORD, FALSE_KEYWORD, FOR_KEYWORD,
            FROM_KEYWORD, FUNC_KEYWORD, GOTO_KEYWORD, IF_KEYWORD, INOUT_KEYWORD, LOCAL_KEYWORD, MOD_KEYWORD,
            MODULE_KEYWORD, NOSTEPIN_KEYWORD, NOT_KEYWORD, NOVIEW_KEYWORD, OR_KEYWORD, PERS_KEYWORD, PROC_KEYWORD,
            RAISE_KEYWORD, READONLY_KEYWORD, RECORD_KEYWORD, RETRY_KEYWORD, RETURN_KEYWORD, STEP_KEYWORD,
            SYSMODULE_KEYWORD, TEST_KEYWORD, THEN_KEYWORD, TO_KEYWORD, TRAP_KEYWORD, TRUE_KEYWORD, TRYNEXT_KEYWORD,
            UNDO_KEYWORD, VAR_KEYWORD, VIEWONLY_KEYWORD, WHILE_KEYWORD, WITH_KEYWORD, XOR_KEYWORD);

    public static final TokenSet LITERALS = TokenSet.create(TRUE_KEYWORD, FALSE_KEYWORD);

    public static final TokenSet OPERATIONS = TokenSet.create(LT, LE, EQ, GE, GT, PLUS, MINUS, ASTERISK, DIV, COLON, LINE, QUESTION, PERCENT, LTGT, CEQ);

    public static final TokenSet MODIFIERS = TokenSet.create(LOCAL_KEYWORD, TASK_KEYWORD);

    public static final TokenSet EXPRESSIONS = TokenSet.create(AGGREGATE_EXPRESSION, BINARY_EXPRESSION, FUNCTION_CALL_EXPRESSION, INDEX_EXPRESSION,
            LITERAL_EXPRESSION, PARENTHESISED_EXPRESSION, REFERENCE_EXPRESSION, UNARY_EXPRESSION);

    public RapidTokenType(@NonNls @NotNull String debugName) {
        super(debugName, RapidLanguage.INSTANCE);
    }
}

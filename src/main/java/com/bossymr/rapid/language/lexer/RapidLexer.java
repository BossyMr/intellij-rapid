package com.bossymr.rapid.language.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class RapidLexer extends FlexAdapter {

    public static final Set<String> KEYWORDS = Set.of("ALIAS", "AND", "BACKWARD", "CASE", "CONNECT", "CONST", "DEFAULT",
            "DIV", "DO", "ELSEIF", "ELSE", "ENDFOR", "ENDFUNC", "ENDIF", "ENDMODULE", "ENDPROC", "ENDRECORD", "ENDTEST",
            "ENDTRAP", "ENDWHILE", "ERROR", "EXIT", "FALSE", "FOR", "FROM", "FUNC", "GOTO", "IF", "INOUT", "LOCAL",
            "MODULE", "MOD", "NOSTEPIN", "NOT", "NOVIEW", "OR", "PERS", "PROC", "RAISE", "READONLY", "RECORD", "RETRY",
            "RETURN", "STEP", "SYSMODULE", "TASK", "TEST", "THEN", "TO", "TRAP", "TRUE", "TRYNEXT", "UNDO", "VAR",
            "VIEWONLY", "WHILE", "WITH", "XOR");

    public RapidLexer() {
        super(new _RapidLexer());
    }

    public static boolean isKeyword(@NotNull String element) {
        return KEYWORDS.contains(element.toUpperCase());
    }

    public static boolean isIdentifier(@NotNull String name) {
        return StringUtil.isJavaIdentifier(name) && name.length() <= 32 && !(isKeyword(name));
    }
}
